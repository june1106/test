package com.example.mdpremotecontroller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

data class BtItem(val name: String, val address: String)

class BluetoothClient(private val context: Context) {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var outStream: OutputStream? = null
    private var inStream: InputStream? = null
    private var listenJob: Job? = null

    // Standard Serial Port Profile (SPP) UUID
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /** List already‑paired devices (keep it simple to start). */
    fun scanPairedDevices(): List<BtItem> {
        val set: Set<BluetoothDevice>? = adapter?.bondedDevices
        return set?.map { BtItem(it.name ?: "Unknown", it.address) } ?: emptyList()
    }

    /** Connect to a device by MAC address. */
    fun connect(address: String, onConnected: () -> Unit, onError: (Throwable) -> Unit) {
        disconnect() // close any old socket first
        val device = adapter?.getRemoteDevice(address)
        if (device == null) {
            onError(IllegalStateException("Device not found: $address"))
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = device.createRfcommSocketToServiceRecord(sppUuid)
                adapter?.cancelDiscovery()
                s.connect()
                socket = s
                outStream = s.outputStream
                inStream = s.inputStream
                onConnected()
                startListening()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    /** Send a line of text (include '\n' when sending commands). */
    fun send(line: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try { outStream?.write(line.toByteArray()) } catch (_: Throwable) {}
        }
    }

    /** Background reader; we’ll wire this to a console later. */
    private fun startListening() {
        listenJob?.cancel()
        listenJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            while (isActive) {
                try {
                    val n = inStream?.read(buffer) ?: break
                    if (n <= 0) break
                    val msg = String(buffer, 0, n)
                    println("BT IN: $msg")
                } catch (_: Throwable) {
                    break
                }
            }
        }
    }

    fun disconnect() {
        listenJob?.cancel()
        try { inStream?.close() } catch (_: Throwable) {}
        try { outStream?.close() } catch (_: Throwable) {}
        try { socket?.close() } catch (_: Throwable) {}
        inStream = null
        outStream = null
        socket = null
    }
}
