package com.example.mdpremotecontroller.core.model

object Commands {
    // Robot motion commands
    const val FORWARD = "FORWARD"
    const val LEFT = "LEFT"
    const val RIGHT = "RIGHT"
    const val REVERSE = "REVERSE"
    
    // Obstacle management commands
    fun addObstacle(obstacleId: ObstacleId, x: Int, y: Int): String {
        return "ADD,${obstacleId.id},($x,$y)"
    }
    
    fun removeObstacle(obstacleId: ObstacleId): String {
        return "SUB,${obstacleId.id}"
    }
    
    fun setFace(obstacleId: ObstacleId, face: Facing): String {
        return "FACE,${obstacleId.id},$face"
    }
    
    // Target commands (received from robot)
    fun targetCommand(obstacleId: ObstacleId, id: Int, face: Facing? = null): String {
        return if (face != null) {
            "TARGET,${obstacleId.id},$id,$face"
        } else {
            "TARGET,${obstacleId.id},$id"
        }
    }
    
    // Message command (received from robot)
    fun messageCommand(content: String): String {
        return "MSG,[$content]"
    }
}
