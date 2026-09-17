import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Change the Dialog to AlertDialog
old_dialog = """    // Dialog for adding dynamic game tab
    if (isAddingTabDialogOpen) {
        var newTabName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { isAddingTabDialogOpen = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "إضافة تبويب لعبة جديد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newTabName,
                        onValueChange = { newTabName = it },
                        label = { Text("اسم اللعبة (مثال: Minecraft)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_game_tab_name_input")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { isAddingTabDialogOpen = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTabName.isNotBlank()) {
                                    coroutineScope.launch {
                                        repository.insertTab(newTabName.trim())
                                        selectedGameTag = newTabName.trim()
                                        isAddingTabDialogOpen = false
                                    }
                                }
                            },
                            enabled = newTabName.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("confirm_add_tab_button")
                        ) {
                            Text("إضافة")
                        }
                    }
                }
            }
        }
    }"""

new_dialog = """    // Dialog for adding dynamic game tab
    if (isAddingTabDialogOpen) {
        var newTabName by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { isAddingTabDialogOpen = false },
            title = {
                Text(
                    text = "إضافة تبويب لعبة جديد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newTabName,
                    onValueChange = { newTabName = it },
                    label = { Text("اسم اللعبة") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTabName.isNotBlank()) {
                            coroutineScope.launch {
                                repository.insertTab(newTabName.trim())
                                selectedGameTag = newTabName.trim()
                                isAddingTabDialogOpen = false
                            }
                        }
                    },
                    enabled = newTabName.isNotBlank()
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isAddingTabDialogOpen = false }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }"""

content = content.replace(old_dialog, new_dialog)

# Also change the first Surface to have an onClick
old_surface = """                        // Add game tab button (+)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable { 
android.util.Log.d("DEBUG_TABS", "Button Clicked 2")
Toast.makeText(context, "Clicked 2", Toast.LENGTH_SHORT).show()
isAddingTabDialogOpen = true 
}
                                .testTag("add_game_tab_button")
                        ) {"""
new_surface = """                        // Add game tab button (+)
                        Surface(
                            onClick = { isAddingTabDialogOpen = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("add_game_tab_button")
                        ) {"""

content = content.replace(old_surface, new_surface)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Done")
