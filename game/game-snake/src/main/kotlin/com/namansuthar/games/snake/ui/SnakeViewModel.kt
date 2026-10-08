package com.namansuthar.games.snake.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namansuthar.games.engine.GameEvent
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.snake.SnakeEngine
import com.namansuthar.games.snake.model.Direction
import com.namansuthar.games.snake.model.SnakeAction
import com.namansuthar.games.snake.model.SnakeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Snake game screen.
 * Manages game state and auto-play loop.
 */
class SnakeViewModel(
    private val engine: SnakeEngine = SnakeEngine()
) : ViewModel() {

    private val _state = MutableStateFlow(engine.initialState())
    val state: StateFlow<SnakeState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var gameLoopJob: Job? = null

    init {
        startGameLoop()
    }

    fun onDirectionChange(direction: Direction) {
        processAction(SnakeAction.ChangeDirection(direction))
    }

    fun onPauseToggle() {
        if (_state.value.isPaused) {
            processAction(SnakeAction.Resume)
            startGameLoop()
        } else {
            processAction(SnakeAction.Pause)
            stopGameLoop()
        }
    }

    fun onNewGame() {
        processAction(SnakeAction.NewGame)
        startGameLoop()
    }

    fun onSpeedIncrease() {
        processAction(SnakeAction.IncreaseSpeed)
    }

    fun onSpeedDecrease() {
        processAction(SnakeAction.DecreaseSpeed)
    }

    /**
     * Start the auto-play game loop.
     */
    private fun startGameLoop() {
        stopGameLoop()

        gameLoopJob = viewModelScope.launch {
            while (true) {
                val currentState = _state.value

                if (currentState.isGameOver || currentState.isPaused) {
                    break
                }

                // Move snake automatically
                processAction(SnakeAction.Move)

                // Delay based on current speed
                delay(SnakeState.getDelayForSpeed(currentState.speed))
            }
        }
    }

    /**
     * Stop the auto-play game loop.
     */
    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun processAction(action: SnakeAction) {
        viewModelScope.launch {
            when (val result = engine.processAction(_state.value, action)) {
                is GameResult.Success -> {
                    _state.value = result.newState

                    // Emit events
                    result.events.forEach { event ->
                        _events.emit(event)
                    }

                    // Stop loop if game is over
                    if (result.newState.isGameOver) {
                        stopGameLoop()
                    }
                }
                is GameResult.Invalid -> {
                    // Ignore invalid actions
                }
                is GameResult.Error -> {
                    // Handle error
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
