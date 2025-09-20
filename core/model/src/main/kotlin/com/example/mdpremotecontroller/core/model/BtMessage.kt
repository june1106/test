package com.example.mdpremotecontroller.core.model

data class BtMessage(
    val raw: String,
    val ts: Long = System.currentTimeMillis()
)
