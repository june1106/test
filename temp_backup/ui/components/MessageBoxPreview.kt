package com.example.mdpremotecontroller.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mdpremotecontroller.core.model.BtMessage
import com.example.mdpremotecontroller.ui.theme.MDPRemoteControllerTheme

@Preview(showBackground = true)
@Composable
fun MessageBoxPreview() {
    MDPRemoteControllerTheme {
        val sampleMessages = listOf(
            BtMessage("MSG,[Robot ready]"),
            BtMessage("TARGET,B1,5"),
            BtMessage("MSG,[Moving forward]"),
            BtMessage("TARGET,B2,11,N")
        )
        
        MessageBox(messages = sampleMessages)
    }
}
