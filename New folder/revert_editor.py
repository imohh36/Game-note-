import re

with open('app/src/main/java/com/example/ui/components/FullNoteEditorScreen.kt', 'r') as f:
    content = f.read()

# Revert focusedBlockIndex back to insertTargetIndex
content = content.replace("focusedBlockIndex = blocks.size", "insertTargetIndex = blocks.size")
content = content.replace("focusedBlockIndex", "insertTargetIndex")
# Add back the androidx.compose prefixes which were replaced, this isn't strictly necessary for functionality, but it restores the previous state.
content = content.replace(".onFocusChanged", ".androidx.compose.ui.focus.onFocusChanged")
content = content.replace(".graphicsLayer(", ".androidx.compose.ui.graphics.graphicsLayer(")
content = content.replace("detectTransformGestures", "androidx.compose.foundation.gestures.detectTransformGestures")

# Remove Zoom Dialog
zoom_dialog_pattern = r"    if \(zoomedImageUri \!\= null\) \{[\s\S]*Dialog\(onDismissRequest \= \{ zoomedImageUri \= null \}\) \{[\s\S]*\} \/\* End of Dialog \*\/ \n    \}"
# Actually let's just use string replacement or regex from "if (zoomedImageUri != null) {" to the end.
# Since it's at the very end of the file:
end_index = content.find("    if (zoomedImageUri != null) {")
if end_index != -1:
    content = content[:end_index] + "}\n"

with open('app/src/main/java/com/example/ui/components/FullNoteEditorScreen.kt', 'w') as f:
    f.write(content)
print("Done")
