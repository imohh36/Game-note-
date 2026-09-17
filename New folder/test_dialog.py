import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add a Toast to the button click
content = content.replace(
    "onClick = { isAddingTabDialogOpen = true },", 
    "onClick = { \nandroid.util.Log.d(\"DEBUG_TABS\", \"Button Clicked\")\nToast.makeText(context, \"Clicked\", Toast.LENGTH_SHORT).show()\nisAddingTabDialogOpen = true \n},"
)

content = content.replace(
    ".clickable { isAddingTabDialogOpen = true }",
    ".clickable { \nandroid.util.Log.d(\"DEBUG_TABS\", \"Button Clicked 2\")\nToast.makeText(context, \"Clicked 2\", Toast.LENGTH_SHORT).show()\nisAddingTabDialogOpen = true \n}"
)

# Add a Toast inside the dialog
content = content.replace(
    "if (isAddingTabDialogOpen) {",
    "if (isAddingTabDialogOpen) {\nandroid.util.Log.d(\"DEBUG_TABS\", \"Dialog Recomposed\")\n"
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Done")
