package com.namansuthar.games.game2048.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namansuthar.games.engine.GameEvent
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.game2048.Game2048Engine
import com.namansuthar.games.game2048.model.Direction
import com.namansuthar.games.game2048.model.Game2048Action
import com.namansuthar.games.game2048.model.Game2048State
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the 2048 game screen.
 * Manages game state and processes user actions.
 */
class Game2048ViewModel(
    private val engine: Game2048Engine = Game2048Engine()
) : ViewModel() {

    private val _state = MutableStateFlow(engine.initialState())
    val state: StateFlow<Game2048State> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onMove(direction: Direction) {
        processAction(Game2048Action.Move(direction))
    }

    fun onNewGame() {
        processAction(Game2048Action.NewGame)
        _uiState.update { it.copy(showWinDialog = false, showGameOverDialog = false) }
    }

    fun onUndo() {
        processAction(Game2048Action.Undo)
    }

    fun onWinDialogDismiss() {
        _uiState.update { it.copy(showWinDialog = false) }
    }

    fun onGameOverDialogDismiss() {
        _uiState.update { it.copy(showGameOverDialog = false) }
    }

    fun onKeepPlaying() {
        _uiState.update { it.copy(showWinDialog = false) }
    }

    private fun processAction(action: Game2048Action) {
        viewModelScope.launch {
            when (val result = engine.processAction(_state.value, action)) {
                is GameResult.Success -> {
                    _state.value = result.newState

                    // Emit events
                    result.events.forEach { event ->
                        _events.emit(event)

                        // Handle UI state changes
                        when (event) {
                            is GameEvent.GameOver -> {
                                if (event.won && !_uiState.value.hasSeenWinDialog) {
                                    _uiState.update {
                                        it.copy(
                                            showWinDialog = true,
                                            hasSeenWinDialog = true
                                        )
                                    }
                                } else if (!event.won) {
                                    _uiState.update { it.copy(showGameOverDialog = true) }
                                }
                            }
                            else -> { /* Handle other events if needed */ }
                        }
                    }
                }
                is GameResult.Invalid -> {
                    // Could show a toast or ignore
                }
                is GameResult.Error -> {
                    // Could show error message
                }
            }
        }
    }

    data class UiState(
        val showWinDialog: Boolean = false,
        val showGameOverDialog: Boolean = false,
        val hasSeenWinDialog: Boolean = false
    )
}
