package com.example.mdpremotecontroller.core.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.example.mdpremotecontroller.core.model.BtDevice
import com.example.mdpremotecontroller.core.model.BtMessage
import com.example.mdpremotecontroller.core.model.BtState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class AndroidBluetoothRepository(
    private val context: Context
) : BluetoothRepository {
    
    companion object {
        private const val TAG = "AndroidBluetoothRepository"
        private const val SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val SCAN_TIMEOUT = 12000L // 12 seconds
        private const val RECONNECT_DELAY = 5000L // 5 seconds
    }
    
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    private val _connectionState = MutableStateFlow<BtState>(BtState.Idle)
    override val connectionState: Flow<BtState> = _connectionState.asStateFlow()
    
    private val _inboundMessages = MutableStateFlow<BtMessage?>(null)
    override val inboundMessages: Flow<BtMessage> = flow {
        _inboundMessages.collect { message ->
            message?.let { emit(it) }
        }
    }
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isScanning = false
    private var lastConnectedDevice: BtDevice? = null
    
    override suspend fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    override suspend fun startScanning() {
        if (!isBluetoothEnabled()) {
            _connectionState.value = BtState.Disconnected("Bluetooth is disabled")
            return
        }
        
        if (isScanning) return
        
        isScanning = true
        _connectionState.value = BtState.Scanning
        
        try {
            val discoveredDevices = mutableListOf<BtDevice>()
            
            // Get paired devices
            bluetoothAdapter?.bondedDevices?.forEach { device ->
                discoveredDevices.add(device.toBtDevice(isPaired = true))
            }
            
            // Start discovery for unpaired devices
            bluetoothAdapter?.startDiscovery()
            
            // Wait for discovery to complete
            delay(SCAN_TIMEOUT)
            
            bluetoothAdapter?.cancelDiscovery()
            
            _connectionState.value = BtState.Discovered(discoveredDevices)
        } catch (e: Exception) {
            Log.e(TAG, "Error during scanning", e)
            _connectionState.value = BtState.Disconnected("Scan failed: ${e.message}")
        } finally {
            isScanning = false
        }
    }
    
    override suspend fun stopScanning() {
        bluetoothAdapter?.cancelDiscovery()
        isScanning = false
        if (_connectionState.value is BtState.Scanning) {
            _connectionState.value = BtState.Idle
        }
    }
    
    override suspend fun connect(device: BtDevice): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                disconnect()
                
                Log.d(TAG, "Connecting to ${device.name} (${device.address})")
                
                val uuid = UUID.fromString(SPP_UUID)
                bluetoothSocket = device.device?.createRfcommSocketToServiceRecord(uuid)
                
                bluetoothSocket?.connect()
                
                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream
                
                lastConnectedDevice = device
                _connectionState.value = BtState.Connected(device)
                
                // Start reading messages
                startMessageReader()
                
                Log.d(TAG, "Connected successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                disconnect()
                Result.failure(e)
            }
        }
    }
    
    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                inputStream?.close()
                outputStream?.close()
                bluetoothSocket?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error during disconnect", e)
            } finally {
                inputStream = null
                outputStream = null
                bluetoothSocket = null
                
                if (_connectionState.value is BtState.Connected) {
                    _connectionState.value = BtState.Disconnected()
                }
            }
        }
    }
    
    override suspend fun send(command: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val socket = bluetoothSocket
                val stream = outputStream
                
                if (socket == null || stream == null) {
                    return@withContext Result.failure(IOException("Not connected"))
                }
                
                val message = "$command\n"
                stream.write(message.toByteArray())
                stream.flush()
                
                Log.d(TAG, "Sent: $command")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send command: $command", e)
                Result.failure(e)
            }
        }
    }
    
    private fun startMessageReader() {
        CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            while (isActive) {
                try {
                    val input = inputStream ?: break
                    val bytesRead = input.read(buffer)
                    if (bytesRead <= 0) break
                    
                    val message = String(buffer, 0, bytesRead).trim()
                    if (message.isNotEmpty()) {
                        val btMessage = BtMessage(
                            raw = message,
                            ts = System.currentTimeMillis()
                        )
                        _inboundMessages.value = btMessage
                        Log.d(TAG, "Received: $message")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading message", e)
                    if (e is CancellationException) throw e
                    break
                }
            }
            
            // Connection lost, attempt to reconnect
            if (isActive && lastConnectedDevice != null) {
                Log.d(TAG, "Connection lost, attempting to reconnect...")
                delay(RECONNECT_DELAY)
                lastConnectedDevice?.let { device ->
                    connect(device)
                }
            }
        }
    }
    
    private fun BluetoothDevice.toBtDevice(isPaired: Boolean = false): BtDevice {
        return BtDevice(
            device = this,
            name = name ?: "Unknown Device",
            address = address,
            isPaired = isPaired
        )
    }
}
