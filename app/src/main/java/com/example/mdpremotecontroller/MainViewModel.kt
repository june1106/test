package com.example.mdpremotecontroller

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mdpremotecontroller.core.bluetooth.AndroidBluetoothRepository
import com.example.mdpremotecontroller.core.bluetooth.MessageParser
import com.example.mdpremotecontroller.core.map.MapRepositoryImpl
import com.example.mdpremotecontroller.core.map.MapState
import com.example.mdpremotecontroller.core.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    context: Context
) : ViewModel() {

    private val bluetoothRepository = AndroidBluetoothRepository(context)
    private val mapRepository = MapRepositoryImpl()
    private val messageParser = MessageParser()

    // UI State
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Bluetooth state
    private val _bluetoothState = MutableStateFlow<BtState>(BtState.Idle)
    val bluetoothState: StateFlow<BtState> = _bluetoothState.asStateFlow()

    // Messages
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    // Robot position
    private val _robotPosition = MutableStateFlow("(10, 10) N")
    val robotPosition: StateFlow<String> = _robotPosition.asStateFlow()

    // Target captured
    private val _targetCaptured = MutableStateFlow("0")
    val targetCaptured: StateFlow<String> = _targetCaptured.asStateFlow()

    // Latest command
    private val _latestCommand = MutableStateFlow("")
    val latestCommand: StateFlow<String> = _latestCommand.asStateFlow()

    init {
        // Collect bluetooth state
        viewModelScope.launch {
            bluetoothRepository.connectionState.collect { state: BtState ->
                _bluetoothState.value = state
                _uiState.value = _uiState.value.copy(
                    bluetoothState = state,
                    isConnected = state is BtState.Connected
                )
            }
        }

        // Collect map state
        viewModelScope.launch {
            mapRepository.mapState.collect { mapState ->
                _uiState.value = _uiState.value.copy(mapState = mapState)
                _robotPosition.value = "(${mapState.robotPose.x}, ${mapState.robotPose.y}) ${mapState.robotPose.facing}"
            }
        }

        // Process incoming messages
        viewModelScope.launch {
            bluetoothRepository.inboundMessages.collect { message: BtMessage ->
                processIncomingMessage(message)
            }
        }
    }

    // Bluetooth functions
    suspend fun isBluetoothEnabled(): Boolean {
        return bluetoothRepository.isBluetoothEnabled()
    }

    fun startScanning() {
        viewModelScope.launch {
            bluetoothRepository.startScanning()
        }
    }

    fun stopScanning() {
        viewModelScope.launch {
            bluetoothRepository.stopScanning()
        }
    }

    fun connect(device: BtDevice) {
        viewModelScope.launch {
            bluetoothRepository.connect(device).fold(
                onSuccess = { _: Unit ->
                    addMessage("Connected to ${device.name}")
                },
                onFailure = { error: Throwable ->
                    addMessage("Connection failed: ${error.message}")
                }
            )
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            bluetoothRepository.disconnect()
            addMessage("Disconnected")
        }
    }

    // Robot movement commands
    fun sendForward() {
        sendCommand(Commands.FORWARD)
    }

    fun sendLeft() {
        sendCommand(Commands.LEFT)
    }

    fun sendRight() {
        sendCommand(Commands.RIGHT)
    }

    fun sendReverse() {
        sendCommand(Commands.REVERSE)
    }

    fun sendStop() {
        sendCommand("STOP")
    }

    private fun sendCommand(command: String) {
        viewModelScope.launch {
            bluetoothRepository.send(command).fold(
                onSuccess = { _: Unit ->
                    _latestCommand.value = command
                    addMessage("Sent: $command")
                },
                onFailure = { error: Throwable ->
                    addMessage("Send failed: ${error.message}")
                }
            )
        }
    }

    // Map functions
    fun addObstacle(obstacle: Obstacle) {
        viewModelScope.launch {
            mapRepository.addObstacle(obstacle)
            addMessage("Added obstacle ${obstacle.obstacleId.id} at (${obstacle.x}, ${obstacle.y})")
            // Send command to robot
            val command = Commands.addObstacle(obstacle.obstacleId, obstacle.x, obstacle.y)
            bluetoothRepository.send(command).fold(
                onSuccess = { _: Unit ->
                    Log.d(
                        "MDP_DEBUG",
                        "Sent addObstacle → ID=${obstacle.obstacleId.id}, x=${obstacle.x}, y=${obstacle.y}, cmd=$command"
                    )
                    addMessage("Sent: $command")
                },
                onFailure = { error: Throwable ->
                    Log.e("MDP_DEBUG", "Send failed: ${error.message}")
                    addMessage("Send failed: ${error.message}")
                }
            )
        }
    }

    fun removeObstacle(obstacleId: ObstacleId) {
        viewModelScope.launch {
            mapRepository.removeObstacle(obstacleId)
            addMessage("Removed obstacle ${obstacleId.id}")
            // Send command to robot
            val command = Commands.removeObstacle(obstacleId)
            bluetoothRepository.send(command).fold(
                onSuccess = { _: Unit ->
                    Log.d("MDP_DEBUG", "Sent removeObstacle → ID=${obstacleId.id}, cmd=$command")
                    addMessage("Sent: $command")
                },
                onFailure = { error: Throwable ->
                    Log.e("MDP_DEBUG", "Send failed: ${error.message}")
                    addMessage("Send failed: ${error.message}")
                }
            )
        }
    }

    fun setObstacleFace(obstacleId: ObstacleId, face: Facing) {
        viewModelScope.launch {
            mapRepository.updateObstacleFace(obstacleId, face)
            addMessage("Set obstacle ${obstacleId.id} face to ${face}")
            // Send command to robot
            val command = Commands.setFace(obstacleId, face)
            bluetoothRepository.send(command).fold(
                onSuccess = { _: Unit ->
                    Log.d("MDP_DEBUG", "Sent setObstacleFace → ID=${obstacleId.id}, face=$face, cmd=$command")
                    addMessage("Sent: $command")
                },
                onFailure = { error: Throwable ->
                    Log.e("MDP_DEBUG", "Send failed: ${error.message}")
                    addMessage("Send failed: ${error.message}")
                }
            )
        }
    }

    fun updateObstaclePosition(obstacle: Obstacle) {
        viewModelScope.launch {
            // Replace obstacle in one atomic update to avoid duplicates
            Log.d(
                "MDP_DEBUG",
                "updateObstaclePosition called → ID=${obstacle.obstacleId.id}, x=${obstacle.x}, y=${obstacle.y}"
            )
            mapRepository.addObstacle(obstacle)
            addMessage("Moved obstacle ${obstacle.obstacleId.id} to (${obstacle.x}, ${obstacle.y})")
            // Send command to robot
            val command = Commands.addObstacle(obstacle.obstacleId, obstacle.x, obstacle.y)
            bluetoothRepository.send(command).fold(
                onSuccess = { _: Unit ->
                    Log.d(
                        "MDP_DEBUG",
                        "Sent updateObstaclePosition → ID=${obstacle.obstacleId.id}, x=${obstacle.x}, y=${obstacle.y}, cmd=$command"
                    )
                    addMessage("Sent: $command")
                },
                onFailure = { error: Throwable ->
                    Log.e("MDP_DEBUG", "Send failed: ${error.message}") // ❌ Error log
                    addMessage("Send failed: ${error.message}")
                }
            )
        }
    }

    fun updateRobotPose(pose: RobotPose) {
        viewModelScope.launch {
            mapRepository.updateRobotPose(pose)
            addMessage("Robot moved to (${pose.x}, ${pose.y}) facing ${pose.facing}")
        }
    }

    fun clearMap() {
        viewModelScope.launch {
            mapRepository.clearMap()
            addMessage("Map cleared - all obstacles and robot reset")
        }
    }

    fun undo() {
        viewModelScope.launch {
            mapRepository.undo()
        }
    }

    fun redo() {
        viewModelScope.launch {
            mapRepository.redo()
        }
    }

    // Message processing
    private fun processIncomingMessage(message: BtMessage) {
        val parsedMessage = messageParser.parseMessage(message)

        when (parsedMessage) {
            is MessageParser.ParsedMessage.Target -> {
                viewModelScope.launch {
                    mapRepository.updateObstacleTarget(
                        parsedMessage.obstacleId,
                        parsedMessage.targetId,
                        parsedMessage.face
                    )
                    _targetCaptured.value = parsedMessage.targetId.toString()
                    addMessage("Target: ${parsedMessage.obstacleId} -> ${parsedMessage.targetId}")
                }
            }
            is MessageParser.ParsedMessage.Robot -> {
                viewModelScope.launch {
                    val robotPose = RobotPose(
                        x = parsedMessage.x,
                        y = parsedMessage.y,
                        facing = parsedMessage.direction
                    )
                    mapRepository.updateRobotPose(robotPose)
                    addMessage("Robot: (${parsedMessage.x}, ${parsedMessage.y}) ${parsedMessage.direction}")
                }
            }
            is MessageParser.ParsedMessage.Message -> {
                addMessage(parsedMessage.content)
            }
            is MessageParser.ParsedMessage.Unknown -> {
                addMessage("Unknown: ${parsedMessage.raw}")
            }
        }
    }

    private fun addMessage(message: String) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add("${System.currentTimeMillis()}: $message")
        if (currentMessages.size > 50) {
            currentMessages.removeAt(0)
        }
        _messages.value = currentMessages
    }
}

data class UiState(
    val bluetoothState: BtState = BtState.Idle,
    val mapState: MapState = MapState(),
    val isConnected: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)
