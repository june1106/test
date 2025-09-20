package com.example.mdpremotecontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mdpremotecontroller.nav.Screen
import com.example.mdpremotecontroller.ui.connect.ConnectScreen
import com.example.mdpremotecontroller.ui.control.ControlScreen
import com.example.mdpremotecontroller.ui.theme.MDPRemoteControllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MDPRemoteControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MDPRemoteControllerApp()
                }
            }
        }
    }
}

@Composable
fun MDPRemoteControllerApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Connect.route
    ) {
        composable(Screen.Connect.route) {
            ConnectScreen(
                onNavigateToControl = {
                    navController.navigate(Screen.Control.route) {
                        popUpTo(Screen.Connect.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Control.route) {
            ControlScreen()
        }
    }
}
