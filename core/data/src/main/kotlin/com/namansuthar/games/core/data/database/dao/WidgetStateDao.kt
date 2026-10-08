package com.namansuthar.games.core.data.database.dao

import androidx.room.*
import com.namansuthar.games.core.data.database.entity.WidgetStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing widget states in the database.
 */
@Dao
interface WidgetStateDao {

    @Query("SELECT * FROM widget_states WHERE widgetId = :widgetId")
    suspend fun getByWidgetId(widgetId: String): WidgetStateEntity?

    @Query("SELECT * FROM widget_states WHERE gameType = :gameType")
    fun observeByGameType(gameType: String): Flow<List<WidgetStateEntity>>

    @Query("SELECT * FROM widget_states")
    fun observeAll(): Flow<List<WidgetStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(widgetState: WidgetStateEntity)

    @Update
    suspend fun update(widgetState: WidgetStateEntity)

    @Delete
    suspend fun delete(widgetState: WidgetStateEntity)

    @Query("DELETE FROM widget_states WHERE widgetId = :widgetId")
    suspend fun deleteByWidgetId(widgetId: String)

    @Query("UPDATE widget_states SET isPinned = :isPinned WHERE widgetId = :widgetId")
    suspend fun updatePinnedStatus(widgetId: String, isPinned: Boolean)
}
