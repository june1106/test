package com.example.mdpremotecontroller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mdpremotecontroller.core.bluetooth.BluetoothRepository
import com.example.mdpremotecontroller.core.bluetooth.MessageParser
import com.example.mdpremotecontroller.core.map.MapRepository
import com.example.mdpremotecontroller.core.map.MapState
import com.example.mdpremotecontroller.core.model.BtDevice
import com.example.mdpremotecontroller.core.model.BtMessage
import com.example.mdpremotecontroller.core.model.BtState
import com.example.mdpremotecontroller.core.model.Commands
import com.example.mdpremotecontroller.core.model.Facing
import com.example.mdpremotecontroller.core.model.Obstacle
import com.example.mdpremotecontroller.core.model.ObstacleId
import com.example.mdpremotecontroller.core.model.RobotPose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val bluetoothState: BtState = BtState.Idle,
    val messages: List<BtMessage> = emptyList(),
    val mapState: MapState = MapState(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothRepository: BluetoothRepository,
    private val mapRepository: MapRepository,
    private val messageParser: MessageParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                bluetoothRepository.connectionState,
                bluetoothRepository.inboundMessages,
                mapRepository.mapState,
                mapRepository.canUndo,
                mapRepository.canRedo
            ) { btState, message, mapState, canUndo, canRedo ->
                UiState(
                    bluetoothState = btState,
                    messages = if (message != null) {
                        _uiState.value.messages + message
                    } else {
                        _uiState.value.messages
                    },
                    mapState = mapState,
                    canUndo = canUndo,
                    canRedo = canRedo
                )
            }.collect { state ->
                _uiState.value = state
                
                // Process incoming messages
                if (state.messages.isNotEmpty() && state.messages.size > _uiState.value.messages.size) {
                    val newMessage = state.messages.last()
                    processIncomingMessage(newMessage)
                }
            }
        }
    }

    fun startScanning() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                bluetoothRepository.startScanning()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to start scanning: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun stopScanning() {
        viewModelScope.launch {
            bluetoothRepository.stopScanning()
        }
    }

    fun connectToDevice(device: BtDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = bluetoothRepository.connect(device)
                if (result.isFailure) {
                    _uiState.update { it.copy(error = "Failed to connect: ${result.exceptionOrNull()?.message}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Connection error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            bluetoothRepository.disconnect()
        }
    }

    fun sendRobotCommand(command: String) {
        viewModelScope.launch {
            try {
                val result = bluetoothRepository.send(command)
                if (result.isFailure) {
                    _uiState.update { it.copy(error = "Failed to send command: ${result.exceptionOrNull()?.message}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Command error: ${e.message}") }
            }
        }
    }

    fun sendForward() = sendRobotCommand(Commands.FORWARD)
    fun sendLeft() = sendRobotCommand(Commands.LEFT)
    fun sendRight() = sendRobotCommand(Commands.RIGHT)
    fun sendReverse() = sendRobotCommand(Commands.REVERSE)

    fun addObstacle(obstacle: Obstacle) {
        viewModelScope.launch {
            mapRepository.addObstacle(obstacle)
            val command = Commands.addObstacle(obstacle.obstacleId, obstacle.x, obstacle.y)
            bluetoothRepository.send(command)
        }
    }

    fun removeObstacle(obstacleId: ObstacleId) {
        viewModelScope.launch {
            mapRepository.removeObstacle(obstacleId)
            val command = Commands.removeObstacle(obstacleId)
            bluetoothRepository.send(command)
        }
    }

    fun setObstacleFace(obstacleId: ObstacleId, face: Facing) {
        viewModelScope.launch {
            mapRepository.updateObstacleFace(obstacleId, face)
            val command = Commands.setFace(obstacleId, face)
            bluetoothRepository.send(command)
        }
    }

    fun updateRobotPose(pose: RobotPose) {
        viewModelScope.launch {
            mapRepository.updateRobotPose(pose)
        }
    }

    fun clearMap() {
        viewModelScope.launch {
            mapRepository.clearMap()
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

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
                }
            }
            is MessageParser.ParsedMessage.Message -> {
                // Message is already added to the UI state
            }
            is MessageParser.ParsedMessage.Unknown -> {
                // Unknown message, just log it
            }
        }
    }
}
