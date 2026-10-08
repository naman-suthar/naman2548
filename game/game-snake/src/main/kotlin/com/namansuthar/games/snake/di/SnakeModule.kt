package com.namansuthar.games.snake.di

import com.namansuthar.games.snake.SnakeEngine
import com.namansuthar.games.snake.ui.SnakeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Snake game dependencies.
 */
val snakeModule = module {

    single { SnakeEngine() }

    viewModel { SnakeViewModel(get()) }
}
