package com.namansuthar.games.tictactoe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namansuthar.games.core.data.repository.GameStateRepository
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.tictactoe.TicTacToeEngine
import com.namansuthar.games.tictactoe.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Tic-Tac-Toe game.
 */
class TicTacToeViewModel(
    private val engine: TicTacToeEngine,
    private val repository: GameStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(engine.initialState())
    val state: StateFlow<TicTacToeState> = _state.asStateFlow()

    private val currentState: TicTacToeState
        get() = _state.value

    init {
        loadGameState()
    }

    private fun loadGameState() {
        viewModelScope.launch {
            val savedState = repository.getGameState(gameType = "tictactoe", instanceId = "main")
            if (savedState != null) {
                _state.value = engine.deserialize(savedState.stateData)
            }
        }
    }

    private fun saveGameState() {
        viewModelScope.launch {
            repository.saveGameState(
                gameType = "tictactoe",
                instanceId = "main",
                stateData = engine.serialize(currentState),
                score = getOverallScore(),
                moves = currentState.moveHistory.size,
                isGameOver = currentState.isGameOver
            )
        }
    }

    private fun getOverallScore(): Int {
        return currentState.xWins * 3 + currentState.oWins * 3 + currentState.draws
    }

    fun processAction(action: TicTacToeAction) {
        val result = engine.processAction(currentState, action)

        if (result is GameResult.Success) {
            _state.value = result.newState
            saveGameState()

            // If it's CPU's turn after this move, make CPU move
            if (result.newState.isCpuTurn() && !result.newState.isGameOver) {
                makeCpuMove()
            }
        }
    }

    fun placeMarker(position: Position) {
        // Ignore if it's CPU's turn
        if (currentState.isCpuTurn()) return

        processAction(TicTacToeAction.PlaceMarker(position))
    }

    private fun makeCpuMove() {
        viewModelScope.launch {
            // Small delay to make it feel more natural
            delay(500)

            val bestMove = engine.getBestMove(currentState)
            if (bestMove != null) {
                processAction(TicTacToeAction.PlaceMarker(bestMove))
            }
        }
    }

    fun newGame() {
        processAction(
            TicTacToeAction.NewGame(
                gameMode = currentState.gameMode,
                difficulty = currentState.difficulty
            )
        )

        // If CPU starts (X in CPU vs CPU mode), make first move
        if (currentState.gameMode == GameMode.CPU_VS_CPU) {
            makeCpuMove()
        }
    }

    fun setGameMode(gameMode: GameMode) {
        processAction(
            TicTacToeAction.NewGame(
                gameMode = gameMode,
                difficulty = currentState.difficulty
            )
        )

        // If CPU vs CPU or CPU starts, make first move
        if (gameMode == GameMode.CPU_VS_CPU) {
            makeCpuMove()
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        processAction(
            TicTacToeAction.NewGame(
                gameMode = currentState.gameMode,
                difficulty = difficulty
            )
        )
    }

    fun undo() {
        processAction(TicTacToeAction.Undo)
    }

    fun restart() {
        processAction(TicTacToeAction.Restart)
    }
}
