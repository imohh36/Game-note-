package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameNotesRepository(
    private val dao: GameNoteDao,
    private val tabDao: GameTabDao
) {
    val allNotes: Flow<List<GameNote>> = dao.getAllNotes()
    val allTabs: Flow<List<GameTab>> = tabDao.getAllTabs()
    val deletedNotes: Flow<List<GameNote>> = dao.getDeletedNotes()

    fun getNotesByGame(gameTag: String): Flow<List<GameNote>> {
        return if (gameTag.isBlank() || gameTag == "الكل" || gameTag.equals("All", ignoreCase = true)) {
            dao.getAllNotes()
        } else {
            dao.getNotesByGame(gameTag)
        }
    }

    fun getNoteById(id: Long): Flow<GameNote?> = dao.getNoteById(id)

    suspend fun getNoteByIdDirect(id: Long): GameNote? = dao.getNoteById(id).firstOrNull()

    suspend fun insertNote(note: GameNote): Long = dao.insertNote(note)

    suspend fun updateNote(note: GameNote) {
        dao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    // Soft delete sets isDeleted = true and moves to Trash
    suspend fun softDeleteNote(id: Long) = dao.softDeleteNoteById(id)

    // Restore note from Trash
    suspend fun restoreNote(id: Long) = dao.restoreNoteById(id)

    // Permanent deletion
    suspend fun hardDeleteNote(note: GameNote) = dao.deleteNote(note)

    suspend fun hardDeleteNoteById(id: Long) = dao.deleteNoteById(id)

    // Empty entire trash
    suspend fun emptyTrash() = dao.emptyTrash()

    // Backward compatibility alias: deleteNote performs soft delete
    suspend fun deleteNote(note: GameNote) = dao.softDeleteNoteById(note.id)

    suspend fun deleteNoteById(id: Long) = dao.softDeleteNoteById(id)

    suspend fun insertTab(name: String): Long {
        if (name.isBlank()) return -1
        return tabDao.insertTab(GameTab(name = name.trim()))
    }

    suspend fun deleteTab(tab: GameTab) = tabDao.deleteTab(tab)

    suspend fun deleteTabById(id: Long) = tabDao.deleteTabById(id)

    suspend fun toggleTodoItem(noteId: Long, todoId: String) {
        val note = dao.getNoteById(noteId).firstOrNull() ?: return
        val updatedTodos = note.todoItems.map {
            if (it.id == todoId) it.copy(isDone = !it.isDone) else it
        }
        val updatedBlocksJson = if (note.blocksJson.isNotBlank() && note.blocksJson != "[]") {
            try {
                val blocks = DocumentBlock.jsonToList(note.blocksJson)
                val newBlocks = blocks.map { b ->
                    if (b.type == BlockType.CHECKLIST && b.id == todoId) {
                        b.copy(isChecked = !b.isChecked)
                    } else b
                }
                DocumentBlock.listToJson(newBlocks)
            } catch (e: Exception) {
                note.blocksJson
            }
        } else {
            note.blocksJson
        }
        dao.updateNote(note.copy(
            todoItems = updatedTodos,
            blocksJson = updatedBlocksJson,
            updatedAt = System.currentTimeMillis()
        ))
    }
}

