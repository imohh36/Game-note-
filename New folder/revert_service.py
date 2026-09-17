import re

with open('app/src/main/java/com/example/service/FloatingOverlayService.kt', 'r') as f:
    content = f.read()

target = """            if (intent?.action == ACTION_PIN_TODO) {
                val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
                if (noteId != -1L) {
                    val mode = com.example.data.AppSettingsPreferences.floatingTaskMode.value
                    if (mode == com.example.data.FloatingTaskDisplayMode.SEPARATED_BOTTOM) {
                        activeTodoNoteId = noteId
                        switchToPanel()
                    } else {
                        switchToMiniWidget(noteId)
                    }
                }
            }"""

replacement = """            if (intent?.action == ACTION_PIN_TODO) {
                val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
                if (noteId != -1L) {
                    switchToMiniWidget(noteId)
                }
            }"""
content = content.replace(target, replacement)
content = content.replace("repository.deleteNoteById(n.id)", "repository.deleteNote(n)")

with open('app/src/main/java/com/example/service/FloatingOverlayService.kt', 'w') as f:
    f.write(content)
print("Done")
