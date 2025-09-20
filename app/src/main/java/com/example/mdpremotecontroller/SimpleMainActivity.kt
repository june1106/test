package com.example.mdpremotecontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import android.content.Context
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.mdpremotecontroller.core.map.MapState
import com.example.mdpremotecontroller.core.model.*
import com.example.mdpremotecontroller.ui.theme.MDPRemoteControllerTheme


class SimpleMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MDPRemoteControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    SimpleTestApp()
                }
            }
        }
    }
}

@Composable
fun SimpleTestApp() {
    MDPGroup3App()
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(context) as T
    }
}

@Composable
fun MDPGroup3App(
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current)
    )
) {
    var selectedTab by remember { mutableStateOf(0) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bluetoothState by viewModel.bluetoothState.collectAsStateWithLifecycle()
    val robotPosition by viewModel.robotPosition.collectAsStateWithLifecycle()
    val targetCaptured by viewModel.targetCaptured.collectAsStateWithLifecycle()
    val latestCommand by viewModel.latestCommand.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    val tabs = listOf("Home", "Bluetooth", "Messages")

    Scaffold(
        topBar = {
            // Red header bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Red),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "MDP Group 3"
                        1 -> "Bluetooth"
                        2 -> "Messages"
                        else -> "MDP Group 3"
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Settings
                                    2 -> Icons.Default.Notifications
                                    else -> Icons.Default.Home
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Red
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    MainControlScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        robotPosition = robotPosition,
                        targetCaptured = targetCaptured,
                        latestCommand = latestCommand,
                        messages = messages
                    )
                }
                1 -> {
                    BluetoothScreen(
                        viewModel = viewModel,
                        bluetoothState = bluetoothState,
                        onNavigateToMain = { selectedTab = 0 }
                    )
                }
                2 -> {
                    MessagesScreen(
                        messages = messages
                    )
                }
            }
        }
    }
}

@Composable
fun BluetoothScreen(
    viewModel: MainViewModel,
    bluetoothState: BtState,
    onNavigateToMain: () -> Unit
) {
    var bluetoothEnabled by remember { mutableStateOf(true) }
    var selectedDevice by remember { mutableStateOf<BtDevice?>(null) }
    val isConnected = bluetoothState is BtState.Connected
    val isScanning = bluetoothState is BtState.Scanning
    val discoveredDevices = when (bluetoothState) {
        is BtState.Discovered -> bluetoothState.devices
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Bluetooth toggle section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (bluetoothEnabled) "ON" else "OFF",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "Bluetooth",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Allows Android to connect to RPI",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = bluetoothEnabled,
                onCheckedChange = { bluetoothEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.Blue,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Connection status and controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                color = if (isConnected) Color.Green else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isConnected) {
                            onNavigateToMain()
                        } else {
                            selectedDevice?.let { device ->
                                viewModel.connect(device)
                            } ?: run {
                                // No selection yet – trigger a scan to populate list
                                viewModel.startScanning()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("CONNECT", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (isScanning) {
                            viewModel.stopScanning()
                        } else {
                            viewModel.startScanning()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.width(80.dp)
                ) {
                    Text(if (isScanning) "STOP" else "SCAN", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Devices section (paired or found during scan)
        Text(
            text = "Devices",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.DarkGray)
                .border(1.dp, Color.Gray)
        ) {
            if (isScanning) {
                Text(
                    text = "Scanning...",
                    color = Color.Yellow,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (discoveredDevices.isNotEmpty()) {
                LazyColumn {
                    items(discoveredDevices) { device ->
                        val isSelected = selectedDevice?.address == device.address
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(if (isSelected) Color.Blue else Color(0xFF2E7D32))
                                .padding(horizontal = 12.dp)
                                .clickable {
                                    selectedDevice = device
                                    viewModel.connect(device)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = device.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = device.address,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No devices. Tap SCAN to search or ensure device is paired.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun MessagesScreen(
    messages: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Message Log",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No messages yet",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages.reversed()) { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.DarkGray
                        )
                    ) {
                        Text(
                            text = message,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MainControlScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    robotPosition: String,
    targetCaptured: String,
    latestCommand: String,
    messages: List<String>
) {
    var selectedObstacle by remember { mutableStateOf(1) } // 1-8 for B1-B8
    var taskType by remember { mutableStateOf("FASTEST CAR") }
    var timer by remember { mutableStateOf("00:00:000") }
    var isPlacingRobot by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    val isConnected = uiState.isConnected

    // Drag state for obstacles
    var draggedObstacle by remember { mutableStateOf<Obstacle?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    // Timer effect - optimized to reduce recompositions
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            startTime = System.currentTimeMillis()
            while (isTimerRunning) {
                val elapsed = System.currentTimeMillis() - startTime
                val minutes = (elapsed / 60000) % 60
                val seconds = (elapsed / 1000) % 60
                val milliseconds = elapsed % 1000
                timer = String.format("%02d:%02d:%03d", minutes, seconds, milliseconds)
                kotlinx.coroutines.delay(100) // Update every 100ms to reduce recompositions
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Top section with robot movement and target captured
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side - Robot Movement and Target Captured
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Robot Movement:",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = robotPosition,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Target captured:",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = targetCaptured,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Timer:",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = timer,
                    color = if (isTimerRunning) Color.Green else Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (isTimerRunning) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isTimerRunning) Color(0xFF003300) else Color.Gray)
                        .padding(8.dp)
                )
            }

            // Right side - Action buttons
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // START/STOP Timer button - prominently placed at top
                Button(
                    onClick = {
                        isTimerRunning = !isTimerRunning
                        if (!isTimerRunning) {
                            timer = "00:00:000" // Reset timer when stopped
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) Color.Red else Color.Green
                    ),
                    modifier = Modifier.width(80.dp)
                ) {
                    Text(
                        text = if (isTimerRunning) "STOP" else "START",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { viewModel.clearMap() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("RESET", color = Color.White, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { /* Presets */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("PRESETS ▼", color = Color.White, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Middle section with obstacles and arena
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side - Arena Map (13x13)
            Column(
                modifier = Modifier.weight(2f)
            ) {
                ArenaGrid(
                    mapState = uiState.mapState,
                    draggedObstacle = draggedObstacle,
                    dragOffset = dragOffset,
                    onObstacleClick = { obstacle ->
                        // Cycle through faces for target annotation
                        val currentFace = obstacle.targetFace
                        val nextFace = when (currentFace) {
                            null -> Facing.N
                            Facing.N -> Facing.E
                            Facing.E -> Facing.S
                            Facing.S -> Facing.W
                            Facing.W -> null
                        }
                        if (nextFace != null) {
                            viewModel.setObstacleFace(obstacle.obstacleId, nextFace)
                        } else {
                            // Remove obstacle if cycling back to null
                            viewModel.removeObstacle(obstacle.obstacleId)
                        }
                    },
                    onObstacleDragStart = { obstacle ->
                        draggedObstacle = obstacle
                        dragOffset = Offset.Zero
                    },
                    onObstacleDrag = { offset ->
                        dragOffset = dragOffset + offset
                    },
                    onObstacleDragMove = { fx, fy ->
                        // Only update projection locally; don't commit to repo yet
                        draggedObstacle = draggedObstacle?.copy(x = fx, y = fy)
                    },
                    onObstacleDragEnd = { x, y ->
                        if (draggedObstacle != null) {
                            if (x == -1 && y == -1) {
                                viewModel.removeObstacle(draggedObstacle!!.obstacleId)
                            } else {
                                val newObstacle = draggedObstacle!!.copy(x = x, y = y)
                                viewModel.updateObstaclePosition(newObstacle)
                            }
                        }
                        draggedObstacle = null
                        dragOffset = Offset.Zero
                    },
                    onCellClick = { x, y ->
                        if (isPlacingRobot) {
                            // Optimized robot placement - direct calculation
                            val newX = (x - 2).coerceIn(0, 34)
                            val newY = (y - 2).coerceIn(0, 34)
                            val newPose = RobotPose(newX, newY, uiState.mapState.robotPose.facing)
                            viewModel.updateRobotPose(newPose)
                            isPlacingRobot = false
                        } else {
                            // Check if clicking on robot area
                            val robot = uiState.mapState.robotPose
                            val isRobotCell = x in robot.x..(robot.x + 5) && y in robot.y..(robot.y + 5)

                            if (isRobotCell) {
                                // Move robot to clicked position
                                val newX = (x - 2).coerceIn(0, 34)
                                val newY = (y - 2).coerceIn(0, 34)
                                val newPose = RobotPose(newX, newY, robot.facing)
                                viewModel.updateRobotPose(newPose)
                            } else {
                                // Handle obstacle placement/removal
                                val existingObstacle = uiState.mapState.obstacles.find { it.x == x && it.y == y }
                                if (existingObstacle != null) {
                                    viewModel.removeObstacle(existingObstacle.obstacleId)
                                } else {
                                    val obstacle = Obstacle(
                                        obstacleId = ObstacleId("$selectedObstacle"),
                                        x = x,
                                        y = y
                                    )
                                    viewModel.addObstacle(obstacle)
                                }
                            }
                        }
                    }
                )
            }

            // Right side - Obstacles and controls
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Obstacles:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Obstacle selection label
                Text(
                    text = "Select Obstacle ID:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Obstacle selection buttons (4x2 grid)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(8) { index ->
                        Button(
                            onClick = { selectedObstacle = index + 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedObstacle == index + 1) Color.Blue else Color(0xFF000080)
                            ),
                            modifier = Modifier.size(50.dp)

                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Map interaction buttons
                Button(
                    onClick = {
                        isPlacingRobot = !isPlacingRobot // Toggle placement mode
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlacingRobot) Color.Red else Color.Blue
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isPlacingRobot) "CANCEL" else "SET ROBOT",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val currentFacing = uiState.mapState.robotPose.facing
                        val nextFacing = when (currentFacing) {
                            Facing.N -> Facing.E
                            Facing.E -> Facing.S
                            Facing.S -> Facing.W
                            Facing.W -> Facing.N
                        }
                        val newPose = uiState.mapState.robotPose.copy(facing = nextFacing)
                        viewModel.updateRobotPose(newPose)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SET DIRECTION", color = Color.Black, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom section with status and controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side - Status and task controls
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Bluetooth: ${if (isConnected) "Connected" else "Not Connected"}",
                    color = if (isConnected) Color.Green else Color.Red,
                    fontSize = 12.sp
                )

                Text(
                    text = "Status: ${if (isConnected) "Ready" else "Not ready"}",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Text(
                    text = "Latest Command: $latestCommand",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select Task Type:",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Button(
                    onClick = { /* Task type selection */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("$taskType ▼", color = Color.White, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* Start task */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("START", color = Color.White, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = timer,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right side - Directional controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Robot direction controls
                Text(
                    text = "Robot Direction:",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    DirectionButton("N", onClick = {
                        val newPose = uiState.mapState.robotPose.copy(facing = Facing.N)
                        viewModel.updateRobotPose(newPose)
                    })
                    DirectionButton("E", onClick = {
                        val newPose = uiState.mapState.robotPose.copy(facing = Facing.E)
                        viewModel.updateRobotPose(newPose)
                    })
                    DirectionButton("S", onClick = {
                        val newPose = uiState.mapState.robotPose.copy(facing = Facing.S)
                        viewModel.updateRobotPose(newPose)
                    })
                    DirectionButton("W", onClick = {
                        val newPose = uiState.mapState.robotPose.copy(facing = Facing.W)
                        viewModel.updateRobotPose(newPose)
                    })
                }

                // Movement buttons - Enhanced and more prominent
                Text(
                    text = "Robot Movement Controls:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Basic movement buttons (FORWARD, LEFT, RIGHT, REVERSE, STOP)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    MovementButton("FORWARD", onClick = { viewModel.sendForward() })
                    MovementButton("LEFT", onClick = { viewModel.sendLeft() })
                    MovementButton("RIGHT", onClick = { viewModel.sendRight() })
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    MovementButton("REVERSE", onClick = { viewModel.sendReverse() })
                    MovementButton("STOP", onClick = { viewModel.sendStop() }, isStopButton = true)
                }

                // Diagonal movement buttons
                Text(
                    text = "Diagonal Movement:",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DirectionButton("↖", onClick = { viewModel.sendLeft(); viewModel.sendForward() })
                    DirectionButton("↑", onClick = { viewModel.sendForward() })
                    DirectionButton("↗", onClick = { viewModel.sendRight(); viewModel.sendForward() })
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DirectionButton("↙", onClick = { viewModel.sendLeft(); viewModel.sendReverse() })
                    DirectionButton("↓", onClick = { viewModel.sendReverse() })
                    DirectionButton("↘", onClick = { viewModel.sendRight(); viewModel.sendReverse() })
                }
            }
        }
    }
}

@Composable
fun ArenaGrid(
    mapState: MapState,
    draggedObstacle: Obstacle?,
    dragOffset: Offset,
    onObstacleClick: (Obstacle) -> Unit,
    onObstacleDragStart: (Obstacle) -> Unit,
    onObstacleDrag: (Offset) -> Unit,
    onObstacleDragMove: (Int, Int) -> Unit,
    onObstacleDragEnd: (Int, Int) -> Unit,
    onCellClick: (Int, Int) -> Unit
) {
    // Memoize obstacles for better performance
    val obstaclesMap = remember(mapState.obstacles) {
        mapState.obstacles.associateBy { "${it.x},${it.y}" }
    }

    // Memoize robot position for better performance
    val robotPos = remember(mapState.robotPose) {
        Triple(mapState.robotPose.x, mapState.robotPose.y, mapState.robotPose.facing)
    }
    // Grid with labels on left and bottom
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Y-axis labels (0-19) on the left - show every 5th label
        Column(
            modifier = Modifier.width(20.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(4) { index -> // Show 4 labels: 0, 5, 10, 15
                Text(
                    text = "${index * 5}",
                    fontSize = 8.sp,
                    color = Color.White,
                    modifier = Modifier.height(100.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Main grid area
        Column {
            // The grid itself (40x40)
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .background(Color.White)
                    .border(1.dp, Color.Black)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(20), // Reduced from 40 to 20 for better performance
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(400) { index -> // 20x20 = 400 cells (much better performance)
                        val x = index % 20
                        val y = index / 20
                        val cellSize = 20.dp // Larger cells for better visibility
                        val density = LocalDensity.current
                        val cellPx = with(density) { cellSize.toPx() }
                        val dragDxCells = if (draggedObstacle != null) (dragOffset.x / cellPx).toInt() else 0
                        val dragDyCells = if (draggedObstacle != null) (dragOffset.y / cellPx).toInt() else 0
                        val projectedX = (draggedObstacle?.x ?: -999) + dragDxCells * 2
                        val projectedY = (draggedObstacle?.y ?: -999) + dragDyCells * 2
                        val isProjectedCell = draggedObstacle != null && (x * 2) == projectedX && (y * 2) == projectedY

                        // Optimized obstacle detection using memoized map
                        val obstacle = obstaclesMap["${x * 2},${y * 2}"]
                        val isDraggingOriginalCell = draggedObstacle?.obstacleId == obstacle?.obstacleId

                        // Optimized robot detection using memoized position
                        val robotX = robotPos.first / 2
                        val robotY = robotPos.second / 2
                        val isRobotCell = x >= robotX && x < robotX + 3 && y >= robotY && y < robotY + 3

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(
                                    when {
                                        isRobotCell -> Color.Red.copy(alpha = 0.7f)
                                        isProjectedCell -> Color.Green
                                        obstacle != null && !isDraggingOriginalCell -> Color.Blue
                                        else -> Color.White
                                    }
                                )
                                .border(0.5.dp, Color.LightGray)
                                .pointerInput(obstacle) {
                                    if (obstacle != null) {
                                        // Single tap on obstacle: trigger obstacle click (cycle face)
                                        detectTapGestures(
                                            onTap = {
                                                onObstacleClick(obstacle)
                                            }
                                        )
                                    } else {
                                        // Tap on empty cell: place/remove obstacle
                                        detectTapGestures(
                                            onTap = {
                                                onCellClick(x * 2, y * 2) // Scale coordinates back
                                            }
                                        )
                                    }
                                }
                                .pointerInput(obstacle?.obstacleId) {
                                    if (obstacle != null) {
                                        // Local accumulator that lives for the gesture
                                        var accum = Offset.Zero

                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                onObstacleDragStart(obstacle)
                                                accum = Offset.Zero
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                // accumulate the pixel delta locally — don't rely on outer dragOffset state
                                                accum += dragAmount

                                                val dxCells = kotlin.math.round(accum.x / cellPx).toInt()
                                                val dyCells = kotlin.math.round(accum.y / cellPx).toInt()

                                                // project the ghost position while dragging
                                                onObstacleDragMove(obstacle.x + dxCells * 2, obstacle.y + dyCells * 2)
                                            },
                                            onDragEnd = {
                                                val dxCells = kotlin.math.round(accum.x / cellPx).toInt()
                                                val dyCells = kotlin.math.round(accum.y / cellPx).toInt()
                                                val candidateX = obstacle.x + dxCells * 2
                                                val candidateY = obstacle.y + dyCells * 2

                                                // outside grid -> signal delete
                                                if (candidateX < 0 || candidateX > 38 || candidateY < 0 || candidateY > 38) {
                                                    onObstacleDragEnd(-1, -1)
                                                } else {
                                                    onObstacleDragEnd(
                                                        candidateX.coerceIn(0, 38),
                                                        candidateY.coerceIn(0, 38)
                                                    )
                                                }

                                                accum = Offset.Zero
                                            },
                                            onDragCancel = {
                                                accum = Offset.Zero
                                            }
                                        )
                                    }
                                }
                        ) {
                            // Show obstacle ID (C.5 - 2D Arena Display)
                            if (isProjectedCell && draggedObstacle != null) {
                                Text(
                                    text = draggedObstacle.obstacleId.id,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else if (obstacle != null && !isDraggingOriginalCell) {
                                Text(
                                    text = obstacle.obstacleId.id,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            // Show target face on obstacles (C.7 - Annotation of Obstacle Faces)
                            obstacle?.targetFace?.let { face: Facing ->
                                Text(
                                    text = face.name,
                                    color = Color.Yellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                )
                            }

                            // Show target ID (C.9 - Displaying Target ID on Obstacle Blocks)
                            obstacle?.targetId?.let { targetId ->
                                Text(
                                    text = "$targetId",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Red.copy(alpha = 0.8f))
                                        .padding(2.dp)
                                )
                            }

                            // Show robot direction indicator using memoized position
                            val frontX = when (robotPos.third) {
                                Facing.N -> robotX + 1
                                Facing.E -> robotX + 2
                                Facing.S -> robotX + 1
                                Facing.W -> robotX
                            }
                            val frontY = when (robotPos.third) {
                                Facing.N -> robotY
                                Facing.E -> robotY + 1
                                Facing.S -> robotY + 2
                                Facing.W -> robotY + 1
                            }

                            if (isRobotCell && x == frontX && y == frontY) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.Black)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }

            // X-axis labels (0-19) on the bottom - show every 5th label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(4) { index -> // Show 4 labels: 0, 5, 10, 15
                    Text(
                        text = "${index * 5}",
                        fontSize = 8.sp,
                        color = Color.White,
                        modifier = Modifier.width(100.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DirectionButton(
    direction: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = direction,
            color = Color.Yellow,
            fontSize = 16.sp
        )
    }
}

@Composable
fun MovementButton(
    text: String,
    onClick: () -> Unit,
    isStopButton: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isStopButton) Color.Red else Color.Blue
        ),
        modifier = Modifier
            .width(60.dp)
            .height(40.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
