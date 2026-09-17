import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix the missing braces
bad_pattern = """                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("النافذة العائمة الافتراضية:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { 
                                        val newBehavior = if (overlayBehavior == com.example.data.FloatingTaskDisplayMode.INLINE) com.example.data.FloatingTaskDisplayMode.SEPARATED_BOTTOM else com.example.data.FloatingTaskDisplayMode.INLINE
                                        com.example.data.AppSettingsPreferences.setFloatingTaskMode(context, newBehavior)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = overlayBehavior.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Trash Button
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { isTrashDialogOpen = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سلة المحذوفات")
                        }
                    }
                }
            }
        }
    ) {"""

good_pattern = """                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("النافذة العائمة الافتراضية:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { 
                                        val newBehavior = if (overlayBehavior == com.example.data.FloatingTaskDisplayMode.INLINE) com.example.data.FloatingTaskDisplayMode.SEPARATED_BOTTOM else com.example.data.FloatingTaskDisplayMode.INLINE
                                        com.example.data.AppSettingsPreferences.setFloatingTaskMode(context, newBehavior)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = overlayBehavior.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Trash Button
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { isTrashDialogOpen = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سلة المحذوفات")
                        }
                    }
                }
                }
                }
            }
        }
    ) {"""

content = content.replace(bad_pattern, good_pattern)

# Fix softDeleteNoteById
content = content.replace("repository.softDeleteNoteById", "repository.deleteNoteById")

# Fix restoreNoteById
content = content.replace("repository.restoreNoteById", "repository.restoreNote")

# Add heightIn import
if "import androidx.compose.foundation.layout.heightIn" not in content:
    content = content.replace("import androidx.compose.foundation.layout.height", "import androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.heightIn")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Done MainActivity")

with open('app/src/main/java/com/example/service/FloatingOverlayService.kt', 'r') as f:
    content = f.read()

content = content.replace("repository.softDeleteNoteById", "repository.deleteNoteById")
with open('app/src/main/java/com/example/service/FloatingOverlayService.kt', 'w') as f:
    f.write(content)
print("Done FloatingOverlayService")
