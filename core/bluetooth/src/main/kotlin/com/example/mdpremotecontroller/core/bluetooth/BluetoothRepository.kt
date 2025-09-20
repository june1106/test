package com.example.mdpremotecontroller.core.bluetooth

import com.example.mdpremotecontroller.core.model.BtDevice
import com.example.mdpremotecontroller.core.model.BtMessage
import com.example.mdpremotecontroller.core.model.BtState
import kotlinx.coroutines.flow.Flow

interface BluetoothRepository {
    val connectionState: Flow<BtState>
    val inboundMessages: Flow<BtMessage>
    
    suspend fun startScanning()
    suspend fun stopScanning()
    suspend fun connect(device: BtDevice): Result<Unit>
    suspend fun disconnect()
    suspend fun send(command: String): Result<Unit>
    suspend fun isBluetoothEnabled(): Boolean
}
