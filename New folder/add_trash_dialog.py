import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """    if (isAddingTabDialogOpen) {"""

trash_dialog = """    if (isTrashDialogOpen) {
        val deletedNotes by repository.deletedNotes.collectAsState(initial = emptyList())
        Dialog(onDismissRequest = { isTrashDialogOpen = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سلة المحذوفات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (deletedNotes.isNotEmpty()) {
                            Text(
                                text = "إفراغ السلة",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable {
                                    coroutineScope.launch {
                                        repository.emptyTrash()
                                    }
                                }.padding(4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (deletedNotes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("السلة فارغة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(deletedNotes) { note ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(note.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(note.gameTag, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Row {
                                        IconButton(onClick = { coroutineScope.launch { repository.restoreNoteById(note.id) } }) {
                                            Icon(imageVector = Icons.Default.Restore, contentDescription = "استعادة", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { coroutineScope.launch { repository.deleteNoteById(note.id) } }) {
                                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "حذف نهائي", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { isTrashDialogOpen = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }

    if (isAddingTabDialogOpen) {"""

content = content.replace(target, trash_dialog)

# Add imports for Restore and DeleteForever
imports = """import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.DeleteForever
"""
content = content.replace("import androidx.compose.material.icons.filled.Settings", "import androidx.compose.material.icons.filled.Settings\n" + imports)


with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Done")
