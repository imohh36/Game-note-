import re

with open('app/src/main/java/com/example/ui/components/FullNoteEditorScreen.kt', 'r') as f:
    content = f.read()

# Fix insertTargetIndex
content = content.replace('insertTargetIndex = blocks.size', 'focusedBlockIndex = blocks.size')

# Fix onFocusChanged
content = content.replace('.androidx.compose.ui.focus.onFocusChanged', '.onFocusChanged')

# Fix graphicsLayer
content = content.replace('.androidx.compose.ui.graphics.graphicsLayer', '.graphicsLayer')

# Fix detectTransformGestures (by importing and removing fully qualified)
content = content.replace('androidx.compose.foundation.gestures.detectTransformGestures', 'detectTransformGestures')

# Add imports if they don't exist
imports = """import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTransformGestures"""

if "import androidx.compose.ui.focus.onFocusChanged" not in content:
    content = content.replace('import androidx.compose.runtime.Composable\n', f'import androidx.compose.runtime.Composable\n{imports}\n')

with open('app/src/main/java/com/example/ui/components/FullNoteEditorScreen.kt', 'w') as f:
    f.write(content)

print("Done")
