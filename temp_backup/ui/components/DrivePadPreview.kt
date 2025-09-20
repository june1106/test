package com.example.mdpremotecontroller.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mdpremotecontroller.ui.theme.MDPRemoteControllerTheme

@Preview(showBackground = true)
@Composable
fun DrivePadPreview() {
    MDPRemoteControllerTheme {
        DrivePad(
            onForward = {},
            onLeft = {},
            onRight = {},
            onReverse = {}
        )
    }
}
