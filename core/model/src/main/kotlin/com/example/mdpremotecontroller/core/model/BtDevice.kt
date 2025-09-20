package com.example.mdpremotecontroller.core.model

import android.bluetooth.BluetoothDevice

data class BtDevice(
    val device: BluetoothDevice?,
    val name: String,
    val address: String,
    val isPaired: Boolean = false,
    val rssi: Int = 0
)
