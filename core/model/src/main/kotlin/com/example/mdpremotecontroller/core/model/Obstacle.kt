package com.example.mdpremotecontroller.core.model

data class Obstacle(
    val obstacleId: ObstacleId,
    val x: Int,
    val y: Int,
    val targetFace: Facing? = null,
    val targetId: Int? = null
)
