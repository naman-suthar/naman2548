package com.namansuthar.games.game2048.di

import com.namansuthar.games.game2048.Game2048Engine
import com.namansuthar.games.game2048.ui.Game2048ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for 2048 game dependencies.
 */
val game2048Module = module {

    single { Game2048Engine() }

    viewModel { Game2048ViewModel(get()) }
}
