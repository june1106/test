package com.example.mdpremotecontroller.core.model

sealed interface BtState {
    data object Idle : BtState
    data object Scanning : BtState
    data class Discovered(val devices: List<BtDevice>) : BtState
    data class Connected(val device: BtDevice) : BtState
    data class Disconnected(val reason: String? = null) : BtState
}
