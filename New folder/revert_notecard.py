import re

with open('app/src/main/java/com/example/ui/components/NoteCard.kt', 'r') as f:
    content = f.read()

# Replace block preview with the original content render logic
# Basically everything between isExpanded = !isCompact and Action Row
# I don't remember the exact previous state, but I can approximate what it was or look at git history if it exists. Wait, I don't have git history.
# Let's just remove the blocks logic and `clickable { onEdit(note) }`
content = content.replace(".clickable { onEdit(note) }", "")
content = content.replace("import com.example.data.GameNote\nimport com.example.data.DocumentBlock\nimport com.example.data.BlockType", "import com.example.data.GameNote")

# Replace the condition `if (!note.isTodoList) { // ASK GEMINI BUTTON` back to `if (true) {`
# Or wait, before it was `if (true) { // Ask Gemini` for all notes? Or only for `!note.isTodoList`?
# In the original state, there was no `if (!note.isTodoList)` around the Ask Gemini button, it was just there.
content = content.replace("if (!note.isTodoList) {\n                    // ASK GEMINI BUTTON (Only for regular notes as required!)\n", "if (true) {\n")

with open('app/src/main/java/com/example/ui/components/NoteCard.kt', 'w') as f:
    f.write(content)

print("Done")
