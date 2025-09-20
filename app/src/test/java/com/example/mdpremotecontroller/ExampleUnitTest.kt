package com.example.mdpremotecontroller

import com.example.mdpremotecontroller.core.model.Commands
import com.example.mdpremotecontroller.core.model.Facing
import com.example.mdpremotecontroller.core.model.ObstacleId
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun testCommands() {
        // Test robot motion commands
        assertEquals("FORWARD", Commands.FORWARD)
        assertEquals("LEFT", Commands.LEFT)
        assertEquals("RIGHT", Commands.RIGHT)
        assertEquals("REVERSE", Commands.REVERSE)
        
        // Test obstacle commands
        val obstacleId = ObstacleId("B1")
        assertEquals("ADD,B1,(10,6)", Commands.addObstacle(obstacleId, 10, 6))
        assertEquals("SUB,B1", Commands.removeObstacle(obstacleId))
        assertEquals("FACE,B1,N", Commands.setFace(obstacleId, Facing.N))
    }
    
    @Test
    fun testFacingEnum() {
        assertEquals("N", Facing.N.name)
        assertEquals("E", Facing.E.name)
        assertEquals("S", Facing.S.name)
        assertEquals("W", Facing.W.name)
    }
}