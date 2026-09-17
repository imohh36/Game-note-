import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """    val customTextColor by ThemePreferences.textColor.collectAsState()"""
replacement = """    LaunchedEffect(Unit) {
        com.example.ui.theme.ThemePreferences.init(context)
        com.example.util.AppPreferences.init(context)
    }
    val customTextColor by ThemePreferences.textColor.collectAsState()"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
