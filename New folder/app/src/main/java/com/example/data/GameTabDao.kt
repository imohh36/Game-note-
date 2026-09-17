package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameTabDao {
    @Query("SELECT * FROM game_tabs ORDER BY createdAt ASC")
    fun getAllTabs(): Flow<List<GameTab>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: GameTab): Long

    @Delete
    suspend fun deleteTab(tab: GameTab)

    @Query("DELETE FROM game_tabs WHERE id = :id")
    suspend fun deleteTabById(id: Long)
}
