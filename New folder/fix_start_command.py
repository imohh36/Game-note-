import re

with open('app/src/main/java/com/example/service/FloatingOverlayService.kt', 'r') as f:
    content = f.read()

target = """            if (intent?.action == ACTION_PIN_TODO) {
                val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
                if (noteId != -1L) {
                    switchToMiniWidget(noteId)
                }
            }"""

replacement = """            if (intent?.action == ACTION_PIN_TODO) {
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

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/service/FloatingOverlayService.kt', 'w') as f:
    f.write(content)
print("Done")
