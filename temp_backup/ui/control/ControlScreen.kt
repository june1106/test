package com.example.mdpremotecontroller.ui.control

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mdpremotecontroller.MainViewModel
import com.example.mdpremotecontroller.ui.components.DrivePad
import com.example.mdpremotecontroller.ui.components.MessageBox
import com.example.mdpremotecontroller.ui.map.MapScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Drive", "Map")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MDP Remote Controller") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) {
                                    Icons.Default.DirectionsCar
                                } else {
                                    Icons.Default.Map
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> DriveTab(viewModel = viewModel)
                1 -> MapScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DriveTab(
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DrivePad(
            onForward = { viewModel.sendForward() },
            onLeft = { viewModel.sendLeft() },
            onRight = { viewModel.sendRight() },
            onReverse = { viewModel.sendReverse() }
        )
        
        MessageBox(
            messages = viewModel.uiState.value.messages
        )
    }
}
