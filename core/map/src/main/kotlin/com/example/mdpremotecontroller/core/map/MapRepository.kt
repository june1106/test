package com.example.mdpremotecontroller.core.map

import com.example.mdpremotecontroller.core.model.Facing
import com.example.mdpremotecontroller.core.model.Obstacle
import com.example.mdpremotecontroller.core.model.ObstacleId
import com.example.mdpremotecontroller.core.model.RobotPose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MapRepository {
    val mapState: Flow<MapState>
    val canUndo: Flow<Boolean>
    val canRedo: Flow<Boolean>
    
    suspend fun addObstacle(obstacle: Obstacle)
    suspend fun removeObstacle(obstacleId: ObstacleId)
    suspend fun updateObstacleFace(obstacleId: ObstacleId, face: Facing)
    suspend fun updateObstacleTarget(obstacleId: ObstacleId, targetId: Int, face: Facing? = null)
    suspend fun updateRobotPose(pose: RobotPose)
    suspend fun clearMap()
    suspend fun undo()
    suspend fun redo()
    suspend fun saveMap()
    suspend fun loadMap()
}

class MapRepositoryImpl : MapRepository {
    
    private val _mapState = MutableStateFlow(MapState())
    override val mapState: Flow<MapState> = _mapState.asStateFlow()
    
    private val _canUndo = MutableStateFlow(false)
    override val canUndo: Flow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    override val canRedo: Flow<Boolean> = _canRedo.asStateFlow()
    
    private val undoStack = mutableListOf<MapState>()
    private val redoStack = mutableListOf<MapState>()

    override suspend fun addObstacle(obstacle: Obstacle) {
        val current = _mapState.value

        // ✅ remove old obstacle with same ID first
        val newList = current.obstacles.filter { it.obstacleId != obstacle.obstacleId } + obstacle

        saveToUndoStack(current)
        _mapState.value = current.copy(
            obstacles = newList,
            lastModified = System.currentTimeMillis()
        )
        clearRedoStack()
    }


    override suspend fun removeObstacle(obstacleId: ObstacleId) {
        val currentState = _mapState.value
        val newObstacles = currentState.obstacles.filter { it.obstacleId != obstacleId }
        
        saveToUndoStack(currentState)
        _mapState.value = currentState.copy(
            obstacles = newObstacles,
            lastModified = System.currentTimeMillis()
        )
        clearRedoStack()
    }

    override suspend fun updateObstacleFace(obstacleId: ObstacleId, face: Facing) {
        val currentState = _mapState.value
        val newObstacles = currentState.obstacles.map { obstacle ->
            if (obstacle.obstacleId == obstacleId) {
                obstacle.copy(targetFace = face)
            } else {
                obstacle
            }
        }

        saveToUndoStack(currentState)
        _mapState.value = currentState.copy(
            obstacles = newObstacles,
            lastModified = System.currentTimeMillis()
        )
        clearRedoStack()
    }

    override suspend fun updateObstacleTarget(obstacleId: ObstacleId, targetId: Int, face: Facing?) {
        val currentState = _mapState.value
        val newObstacles = currentState.obstacles.map { obstacle ->
            if (obstacle.obstacleId == obstacleId) {
                obstacle.copy(
                    targetId = targetId,
                    targetFace = face ?: obstacle.targetFace
                )
            } else {
                obstacle
            }
        }
        
        saveToUndoStack(currentState)
        _mapState.value = currentState.copy(
            obstacles = newObstacles,
            lastModified = System.currentTimeMillis()
        )
        clearRedoStack()
    }
    
    override suspend fun updateRobotPose(pose: RobotPose) {
        val currentState = _mapState.value
        saveToUndoStack(currentState)
        _mapState.value = currentState.copy(
            robotPose = pose,
            lastModified = System.currentTimeMillis()
        )
        clearRedoStack()
    }
    
    override suspend fun clearMap() {
        val currentState = _mapState.value
        saveToUndoStack(currentState)
        _mapState.value = MapState()
        clearRedoStack()
    }
    
    override suspend fun undo() {
        val currentState = _mapState.value
        if (undoStack.isNotEmpty()) {
            redoStack.add(currentState)
            _mapState.value = undoStack.removeLast()
            updateUndoRedoState()
        }
    }
    
    override suspend fun redo() {
        val currentState = _mapState.value
        if (redoStack.isNotEmpty()) {
            undoStack.add(currentState)
            _mapState.value = redoStack.removeLast()
            updateUndoRedoState()
        }
    }
    
    override suspend fun saveMap() {
        // Implementation would save to DataStore
        // For now, just update the lastModified timestamp
        val currentState = _mapState.value
        _mapState.value = currentState.copy(lastModified = System.currentTimeMillis())
    }
    
    override suspend fun loadMap() {
        // Implementation would load from DataStore
        // For now, do nothing
    }
    
    private fun saveToUndoStack(state: MapState) {
        undoStack.add(state)
        if (undoStack.size > MAX_UNDO_STACK_SIZE) {
            undoStack.removeFirst()
        }
        updateUndoRedoState()
    }
    
    private fun clearRedoStack() {
        redoStack.clear()
        updateUndoRedoState()
    }
    
    private fun updateUndoRedoState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
    
    companion object {
        private const val MAX_UNDO_STACK_SIZE = 20
    }
}
