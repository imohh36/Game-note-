import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Revert ThemePreferences
target_init = """    LaunchedEffect(Unit) {
        com.example.ui.theme.ThemePreferences.init(context)
        com.example.data.AppSettingsPreferences.init(context)
    }
    val customTextColor by ThemePreferences.textColor.collectAsState()"""
content = content.replace(target_init, "    val customTextColor by ThemePreferences.textColor.collectAsState()")

# 2. Revert the Settings UI (AI Model, Overlay Behavior, Trash)
bad_settings_ui = """                    // AI Settings & Overlay Behavior
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("إعدادات متقدمة", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val aiModel by com.example.data.AppSettingsPreferences.aiModel.collectAsState()
                        val overlayBehavior by com.example.data.AppSettingsPreferences.floatingTaskMode.collectAsState()

                        // AI Model Selection
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("نموذج الذكاء الاصطناعي:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { 
                                        val newModel = if (aiModel == com.example.data.AiModelOption.FLASH_LITE) com.example.data.AiModelOption.PRO else com.example.data.AiModelOption.FLASH_LITE
                                        com.example.data.AppSettingsPreferences.setAiModel(context, newModel)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = aiModel.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Overlay Behavior
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
"""
content = content.replace(bad_settings_ui, "")

# 3. Revert isTrashDialogOpen var
trash_var = """    var isAddingTabDialogOpen by remember { mutableStateOf(false) }
    var isTrashDialogOpen by remember { mutableStateOf(false) }"""
content = content.replace(trash_var, "    var isAddingTabDialogOpen by remember { mutableStateOf(false) }")

# 4. Remove Trash Dialog
# Find the start of trash dialog:
trash_dialog_start = content.find("    if (isTrashDialogOpen) {")
if trash_dialog_start != -1:
    trash_dialog_end = content.find("    if (isAddingTabDialogOpen) {")
    if trash_dialog_end != -1:
        content = content[:trash_dialog_start] + content[trash_dialog_end:]

# 5. Revert deleteNoteById to deleteNote
content = content.replace("repository.deleteNoteById(n.id)", "repository.deleteNote(n)")
content = content.replace("repository.restoreNote(note.id)", "repository.restoreNoteById(note.id)") # Wait, actually restoreNoteById wasn't used before this, because there was no trash.
# But just in case, I will leave the NoteCard edits logic

# 6. Revert Tabs to original state (Wait, I used fix_tabs.py for that. Did I? I didn't see fix_tabs.py in the previous logs!
# Did I change the tabs? The user said "تم إعادة هندسة تخطيط الصفحة الرئيسية؛ حيث تم فصل شريط التبويبات ليصبح مثبتاً (Sticky)". 
# Ah, I don't see any fix_tabs.py in the truncated context, meaning the tabs were NOT changed in the last 15 actions.
# Let's write the file.
with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Done")
