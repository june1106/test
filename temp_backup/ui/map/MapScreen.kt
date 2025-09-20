package com.example.mdpremotecontroller.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdpremotecontroller.MainViewModel
import com.example.mdpremotecontroller.core.model.Facing
import com.example.mdpremotecontroller.core.model.Obstacle
import com.example.mdpremotecontroller.core.model.ObstacleId
import com.example.mdpremotecontroller.core.model.RobotPose

@Composable
fun MapScreen(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val mapState = uiState.mapState
    var nextObstacleId by remember { mutableStateOf(1) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Arena Map (13×13)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        // Add new obstacle at center
                        val obstacle = Obstacle(
                            obstacleId = ObstacleId(nextObstacleId.toString()),
                            x = 6,
                            y = 6
                        )
                        viewModel.addObstacle(obstacle)
                        nextObstacleId++
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Obstacle")
                }
                
                Button(
                    onClick = { viewModel.undo() },
                    enabled = uiState.canUndo
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }
                
                Button(
                    onClick = { viewModel.redo() },
                    enabled = uiState.canRedo
                ) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo")
                }
                
                Button(
                    onClick = { 
                        viewModel.clearMap()
                        nextObstacleId = 1
                    }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Arena Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            ArenaCanvas(
                robotPose = mapState.robotPose,
                obstacles = mapState.obstacles,
                onCellClick = { x, y ->
                    // Add obstacle at clicked cell
                    val obstacle = Obstacle(
                        obstacleId = ObstacleId("B$nextObstacleId"),
                        x = x,
                        y = y
                    )
                    viewModel.addObstacle(obstacle)
                    nextObstacleId++
                },
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
                    }
                },
                onObstacleDrag = { obstacle, newX, newY ->
                    // Remove old obstacle and add at new position
                    viewModel.removeObstacle(obstacle.obstacleId)
                    val newObstacle = obstacle.copy(x = newX, y = newY)
                    viewModel.addObstacle(newObstacle)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Instructions: Tap empty cells to add obstacles, tap obstacles to cycle target faces, drag obstacles to move them",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ArenaCanvas(
    robotPose: RobotPose,
    obstacles: List<Obstacle>,
    onCellClick: (Int, Int) -> Unit,
    onObstacleClick: (Obstacle) -> Unit,
    onObstacleDrag: (Obstacle, Int, Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val gridSize = 13
    val cellSize = 30.dp
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Check if drag started on an obstacle
                        val cellX = (offset.x / (size.width / gridSize)).toInt().coerceIn(0, gridSize - 1)
                        val cellY = (offset.y / (size.height / gridSize)).toInt().coerceIn(0, gridSize - 1)
                        
                        obstacles.find { it.x == cellX && it.y == cellY }?.let { obstacle ->
                            // Store the dragged obstacle for later use
                        }
                    },
                    onDragEnd = { },
                    onDrag = { change, _ ->
                        // Handle drag movement
                        val cellX = (change.position.x / (size.width / gridSize)).toInt().coerceIn(0, gridSize - 1)
                        val cellY = (change.position.y / (size.height / gridSize)).toInt().coerceIn(0, gridSize - 1)
                        
                        // Find obstacle at drag start position and move it
                        obstacles.find { it.x == cellX && it.y == cellY }?.let { obstacle ->
                            onObstacleDrag(obstacle, cellX, cellY)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press) {
                            val offset = event.changes.first().position
                            val cellX = (offset.x / (size.width / gridSize)).toInt().coerceIn(0, gridSize - 1)
                            val cellY = (offset.y / (size.height / gridSize)).toInt().coerceIn(0, gridSize - 1)
                            
                            // Check if click is on an obstacle
                            val clickedObstacle = obstacles.find { it.x == cellX && it.y == cellY }
                            if (clickedObstacle != null) {
                                onObstacleClick(clickedObstacle)
                            } else {
                                onCellClick(cellX, cellY)
                            }
                        }
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val cellWidth = canvasWidth / gridSize
        val cellHeight = canvasHeight / gridSize
        
        // Draw grid
        for (i in 0..gridSize) {
            val x = i * cellWidth
            val y = i * cellHeight
            
            // Vertical lines
            drawLine(
                color = Color.Gray,
                start = Offset(x, 0f),
                end = Offset(x, canvasHeight),
                strokeWidth = 1f
            )
            
            // Horizontal lines
            drawLine(
                color = Color.Gray,
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1f
            )
        }
        
        // Draw axis labels
        for (i in 0 until gridSize) {
            val x = i * cellWidth + cellWidth / 2
            val y = i * cellHeight + cellHeight / 2
            
            // X-axis labels (top)
            drawText(
                textMeasurer = textMeasurer,
                text = i.toString(),
                topLeft = Offset(x - 5f, 5f),
                style = TextStyle(fontSize = 10.sp)
            )
            
            // Y-axis labels (left)
            drawText(
                textMeasurer = textMeasurer,
                text = i.toString(),
                topLeft = Offset(5f, y - 5f),
                style = TextStyle(fontSize = 10.sp)
            )
        }
        
        // Draw obstacles
        obstacles.forEach { obstacle ->
            val x = obstacle.x * cellWidth
            val y = obstacle.y * cellHeight
            
            // Draw obstacle rectangle
            drawRect(
                color = Color.LightGray,
                topLeft = Offset(x + 2f, y + 2f),
                size = androidx.compose.ui.geometry.Size(cellWidth - 4f, cellHeight - 4f)
            )
            
            // Draw obstacle ID
            drawText(
                textMeasurer = textMeasurer,
                text = obstacle.obstacleId.id,
                topLeft = Offset(x + cellWidth / 2 - 10f, y + cellHeight / 2 - 5f),
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
            )
            
            // Draw target face indicator
            obstacle.targetFace?.let { face ->
                drawTargetFaceIndicator(
                    x = x,
                    y = y,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    face = face
                )
            }
            
            // Draw target ID badge
            obstacle.targetId?.let { targetId ->
                drawTargetIdBadge(
                    x = x,
                    y = y,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    targetId = targetId
                )
            }
        }
        
        // Draw robot
        val robotX = robotPose.x * cellWidth + cellWidth / 2
        val robotY = robotPose.y * cellHeight + cellHeight / 2
        val rotation = when (robotPose.facing) {
            Facing.N -> 0f
            Facing.E -> 90f
            Facing.S -> 180f
            Facing.W -> 270f
        }
        
        drawRobot(
            centerX = robotX,
            centerY = robotY,
            rotation = rotation
        )
    }
}

private fun DrawScope.drawRobot(centerX: Float, centerY: Float, rotation: Float) {
    val path = Path().apply {
        moveTo(centerX, centerY - 10f)
        lineTo(centerX - 8f, centerY + 10f)
        lineTo(centerX + 8f, centerY + 10f)
        close()
    }
    
    drawPath(
        path = path,
        color = Color.Blue
    )
    
    // Draw direction indicator
    drawCircle(
        color = Color.Red,
        radius = 3f,
        center = Offset(centerX, centerY - 5f)
    )
}

private fun DrawScope.drawTargetFaceIndicator(
    x: Float,
    y: Float,
    cellWidth: Float,
    cellHeight: Float,
    face: Facing
) {
    val lineLength = 8f
    val lineWidth = 2f
    
    when (face) {
        Facing.N -> {
            drawLine(
                color = Color.Red,
                start = Offset(x + cellWidth / 2 - lineLength / 2, y + 2f),
                end = Offset(x + cellWidth / 2 + lineLength / 2, y + 2f),
                strokeWidth = lineWidth
            )
        }
        Facing.E -> {
            drawLine(
                color = Color.Red,
                start = Offset(x + cellWidth - 2f, y + cellHeight / 2 - lineLength / 2),
                end = Offset(x + cellWidth - 2f, y + cellHeight / 2 + lineLength / 2),
                strokeWidth = lineWidth
            )
        }
        Facing.S -> {
            drawLine(
                color = Color.Red,
                start = Offset(x + cellWidth / 2 - lineLength / 2, y + cellHeight - 2f),
                end = Offset(x + cellWidth / 2 + lineLength / 2, y + cellHeight - 2f),
                strokeWidth = lineWidth
            )
        }
        Facing.W -> {
            drawLine(
                color = Color.Red,
                start = Offset(x + 2f, y + cellHeight / 2 - lineLength / 2),
                end = Offset(x + 2f, y + cellHeight / 2 + lineLength / 2),
                strokeWidth = lineWidth
            )
        }
    }
}

private fun DrawScope.drawTargetIdBadge(
    x: Float,
    y: Float,
    cellWidth: Float,
    cellHeight: Float,
    targetId: Int
) {
    val badgeWidth = 20f
    val badgeHeight = 12f
    val badgeX = x + cellWidth - badgeWidth - 2f
    val badgeY = y + 2f
    
    drawRect(
        color = Color.Green,
        topLeft = Offset(badgeX, badgeY),
        size = androidx.compose.ui.geometry.Size(badgeWidth, badgeHeight)
    )
    
    // Draw target ID text
    drawText(
        textMeasurer = rememberTextMeasurer(),
        text = "ID:$targetId",
        topLeft = Offset(badgeX + 2f, badgeY + 1f),
        style = TextStyle(fontSize = 8.sp, color = Color.White)
    )
}
