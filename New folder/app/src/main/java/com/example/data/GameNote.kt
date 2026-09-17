package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteType {
    REGULAR,    // Free text, images, Gemini assistant
    TODO_LIST   // Checkable tasks only
}

@Entity(tableName = "game_notes")
data class GameNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val gameTag: String = "",
    val noteType: String = NoteType.REGULAR.name,
    val todoItems: List<TodoItem> = emptyList(),
    val imageUris: List<String> = emptyList(),
    val blocksJson: String = "[]",
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val colorHex: String = "#10B981",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val completedTodosCount: Int
        get() = todoItems.count { it.isDone }

    val totalTodosCount: Int
        get() = todoItems.size

    val isTodoList: Boolean
        get() = noteType == NoteType.TODO_LIST.name
}

