package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameNoteDao {
    @Query("SELECT * FROM game_notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<GameNote>>

    @Query("SELECT * FROM game_notes WHERE isDeleted = 0 AND gameTag = :gameTag ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByGame(gameTag: String): Flow<List<GameNote>>

    @Query("SELECT * FROM game_notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): Flow<List<GameNote>>

    @Query("SELECT * FROM game_notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<GameNote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: GameNote): Long

    @Update
    suspend fun updateNote(note: GameNote)

    @Query("UPDATE game_notes SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteNoteById(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE game_notes SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restoreNoteById(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteNote(note: GameNote)

    @Query("DELETE FROM game_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM game_notes WHERE isDeleted = 1")
    suspend fun emptyTrash()
}
