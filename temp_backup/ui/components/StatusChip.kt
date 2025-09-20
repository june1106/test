package com.example.mdpremotecontroller.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.mdpremotecontroller.core.model.BtState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusChip(state: BtState) {
    val (text, color) = when (state) {
        is BtState.Idle -> "Idle" to Color.Gray
        is BtState.Scanning -> "Scanning..." to Color.Blue
        is BtState.Discovered -> "Found ${state.devices.size} devices" to Color.Green
        is BtState.Connected -> "Connected" to Color.Green
        is BtState.Disconnected -> "Disconnected" to Color.Red
    }
    
    AssistChip(
        onClick = { },
        label = { Text(text) }
    )
}
