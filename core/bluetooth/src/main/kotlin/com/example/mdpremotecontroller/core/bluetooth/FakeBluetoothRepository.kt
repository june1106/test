package com.example.mdpremotecontroller.core.bluetooth

import com.example.mdpremotecontroller.core.model.BtDevice
import com.example.mdpremotecontroller.core.model.BtMessage
import com.example.mdpremotecontroller.core.model.BtState
import com.example.mdpremotecontroller.core.model.Commands
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeBluetoothRepository : BluetoothRepository {
    
    private val _connectionState = MutableStateFlow<BtState>(BtState.Idle)
    override val connectionState: Flow<BtState> = _connectionState.asStateFlow()
    
    private val _inboundMessages = MutableStateFlow<BtMessage?>(null)
    override val inboundMessages: Flow<BtMessage> = _inboundMessages.asStateFlow().filterNotNull()
    
    private val fakeDevices = listOf(
        BtDevice(
            device = null, // Fake device - will be handled in AndroidBluetoothRepository
            name = "HC-05",
            address = "00:11:22:33:44:55",
            isPaired = true
        ),
        BtDevice(
            device = null,
            name = "HC-06",
            address = "AA:BB:CC:DD:EE:FF",
            isPaired = false
        )
    )
    
    override suspend fun isBluetoothEnabled(): Boolean = true
    
    override suspend fun startScanning() {
        _connectionState.value = BtState.Scanning
        delay(2000) // Simulate scan time
        _connectionState.value = BtState.Discovered(fakeDevices)
    }
    
    override suspend fun stopScanning() {
        if (_connectionState.value is BtState.Scanning) {
            _connectionState.value = BtState.Idle
        }
    }
    
    override suspend fun connect(device: BtDevice): Result<Unit> {
        _connectionState.value = BtState.Connected(device)
        
        // Simulate incoming messages
        simulateIncomingMessages()
        
        return Result.success(Unit)
    }
    
    override suspend fun disconnect() {
        _connectionState.value = BtState.Disconnected()
    }
    
    override suspend fun send(command: String): Result<Unit> {
        // Simulate some responses based on commands
        when (command) {
            Commands.FORWARD -> {
                delay(200)
                _inboundMessages.value = BtMessage("MSG,[Moving forward]")
                // Simulate robot position update
                delay(300)
                _inboundMessages.value = BtMessage("ROBOT,18,17,N")
            }
            Commands.LEFT -> {
                delay(200)
                _inboundMessages.value = BtMessage("MSG,[Turning left]")
                // Simulate robot direction change
                delay(300)
                _inboundMessages.value = BtMessage("ROBOT,17,17,W")
            }
            Commands.RIGHT -> {
                delay(200)
                _inboundMessages.value = BtMessage("MSG,[Turning right]")
                // Simulate robot direction change
                delay(300)
                _inboundMessages.value = BtMessage("ROBOT,17,17,E")
            }
            Commands.REVERSE -> {
                delay(200)
                _inboundMessages.value = BtMessage("MSG,[Moving backward]")
                // Simulate robot position update
                delay(300)
                _inboundMessages.value = BtMessage("ROBOT,16,17,N")
            }
            "STOP" -> {
                delay(200)
                _inboundMessages.value = BtMessage("MSG,[Stopped]")
            }
            else -> {
                // Handle obstacle commands
                if (command.startsWith("ADD,")) {
                    delay(200)
                    _inboundMessages.value = BtMessage("MSG,[Obstacle added]")
                } else if (command.startsWith("SUB,")) {
                    delay(200)
                    _inboundMessages.value = BtMessage("MSG,[Obstacle removed]")
                } else if (command.startsWith("FACE,")) {
                    delay(200)
                    _inboundMessages.value = BtMessage("MSG,[Face set]")
                }
            }
        }
        
        return Result.success(Unit)
    }
    
    private suspend fun simulateIncomingMessages() {
        // Simulate target messages
        delay(3000)
        _inboundMessages.value = BtMessage("TARGET,B1,5")
        
        delay(2000)
        _inboundMessages.value = BtMessage("TARGET,B2,11,N")
        
        delay(1500)
        _inboundMessages.value = BtMessage("MSG,[Robot ready]")
    }
}
