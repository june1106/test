package com.example.mdpremotecontroller.core.bluetooth

import com.example.mdpremotecontroller.core.model.BtMessage
import com.example.mdpremotecontroller.core.model.Facing
import com.example.mdpremotecontroller.core.model.ObstacleId
import com.example.mdpremotecontroller.core.model.Commands

class MessageParser {
    
    sealed class ParsedMessage {
        data class Target(
            val obstacleId: ObstacleId,
            val targetId: Int,
            val face: Facing? = null
        ) : ParsedMessage()
        
        data class Robot(
            val x: Int,
            val y: Int,
            val direction: Facing
        ) : ParsedMessage()
        
        data class Message(val content: String) : ParsedMessage()
        
        data class Unknown(val raw: String) : ParsedMessage()
    }
    
    fun parseMessage(btMessage: BtMessage): ParsedMessage {
        val message = btMessage.raw.trim()
        
        return when {
            message.startsWith("TARGET,") -> parseTargetMessage(message)
            message.startsWith("ROBOT,") -> parseRobotMessage(message)
            message.startsWith("MSG,") -> parseMessageCommand(message)
            else -> ParsedMessage.Unknown(message)
        }
    }
    
    private fun parseTargetMessage(message: String): ParsedMessage.Target {
        val parts = message.split(",")
        return when (parts.size) {
            3 -> {
                // TARGET,<ObstacleId>,<Id>
                val obstacleId = ObstacleId(parts[1])
                val targetId = parts[2].toIntOrNull() ?: 0
                ParsedMessage.Target(obstacleId, targetId)
            }
            4 -> {
                // TARGET,<ObstacleId>,<Id>,<Face>
                val obstacleId = ObstacleId(parts[1])
                val targetId = parts[2].toIntOrNull() ?: 0
                val face = try {
                    Facing.valueOf(parts[3])
                } catch (e: IllegalArgumentException) {
                    null
                }
                ParsedMessage.Target(obstacleId, targetId, face)
            }
            else -> ParsedMessage.Target(ObstacleId("0"), 0)
        }
    }
    
    private fun parseRobotMessage(message: String): ParsedMessage.Robot {
        // ROBOT,<x>,<y>,<direction>
        val parts = message.split(",")
        return if (parts.size == 4) {
            val x = parts[1].toIntOrNull() ?: 0
            val y = parts[2].toIntOrNull() ?: 0
            val direction = try {
                Facing.valueOf(parts[3])
            } catch (e: IllegalArgumentException) {
                Facing.N
            }
            ParsedMessage.Robot(x, y, direction)
        } else {
            ParsedMessage.Robot(0, 0, Facing.N)
        }
    }
    
    private fun parseMessageCommand(message: String): ParsedMessage.Message {
        // MSG,[content]
        val content = message.removePrefix("MSG,").removeSurrounding("[", "]")
        return ParsedMessage.Message(content)
    }
}
