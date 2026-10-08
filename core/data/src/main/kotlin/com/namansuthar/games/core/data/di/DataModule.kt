package com.namansuthar.games.core.data.di

import android.content.Context
import androidx.room.Room
import com.namansuthar.games.core.data.database.GameDatabase
import com.namansuthar.games.core.data.repository.GameStateRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin dependency injection module for data layer.
 */
val dataModule = module {

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            GameDatabase::class.java,
            GameDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<GameDatabase>().gameStateDao() }
    single { get<GameDatabase>().widgetStateDao() }

    // Repository
    single { GameStateRepository(get()) }
}
