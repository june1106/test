package com.example.mdpremotecontroller.core.map

import com.example.mdpremotecontroller.core.model.Obstacle
import com.example.mdpremotecontroller.core.model.RobotPose

data class MapState(
    val obstacles: List<Obstacle> = emptyList(),
    val robotPose: RobotPose = RobotPose(17, 17, com.example.mdpremotecontroller.core.model.Facing.N), // Center of 40x40 grid
    val lastModified: Long = System.currentTimeMillis()
)
