package com.namansuthar.games.tictactoe.di

import com.namansuthar.games.tictactoe.TicTacToeEngine
import com.namansuthar.games.tictactoe.ui.TicTacToeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Dependency injection module for Tic-Tac-Toe.
 */
val ticTacToeModule = module {
    single { TicTacToeEngine() }
    viewModel { TicTacToeViewModel(get(), get()) }
}
