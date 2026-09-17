package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.GameNotesRepository

class GameNotesApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: GameNotesRepository by lazy { GameNotesRepository(database.gameNoteDao(), database.gameTabDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.example.ui.theme.ThemePreferences.init(this)
        com.example.data.AppSettingsPreferences.init(this)
    }

    companion object {
        lateinit var instance: GameNotesApp
            private set
    }
}
