package com.namansuthar.games

import android.app.Application
import com.namansuthar.games.core.data.di.dataModule
import com.namansuthar.games.game2048.di.game2048Module
import com.namansuthar.games.snake.di.snakeModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application class for Board Games app.
 * Sets up dependency injection and other global configurations.
 */
class BoardGamesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin for dependency injection
        startKoin {
            androidLogger()
            androidContext(this@BoardGamesApplication)
            modules(
                dataModule,
                game2048Module,
                snakeModule
            )
        }
    }
}
