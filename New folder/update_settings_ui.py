import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace the init call
content = content.replace("com.example.util.AppPreferences.init(context)", "com.example.data.AppSettingsPreferences.init(context)")

# Replace the UI for AI Model and Overlay Behavior
target_ui = """                        val aiModel by com.example.util.AppPreferences.aiModel.collectAsState(initial = com.example.util.AppPreferences.MODEL_FLASH)
                        val overlayBehavior by com.example.util.AppPreferences.overlayBehavior.collectAsState(initial = com.example.util.AppPreferences.OVERLAY_MINI)

                        // AI Model Selection
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("نموذج الذكاء الاصطناعي:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { 
                                        val newModel = if (aiModel == com.example.util.AppPreferences.MODEL_FLASH) com.example.util.AppPreferences.MODEL_PRO else com.example.util.AppPreferences.MODEL_FLASH
                                        com.example.util.AppPreferences.setAiModel(context, newModel)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (aiModel == com.example.util.AppPreferences.MODEL_FLASH) "Flash (سريع)" else "Pro (ذكي)",
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
                                        val newBehavior = if (overlayBehavior == com.example.util.AppPreferences.OVERLAY_MINI) com.example.util.AppPreferences.OVERLAY_EXPANDED else com.example.util.AppPreferences.OVERLAY_MINI
                                        com.example.util.AppPreferences.setOverlayBehavior(context, newBehavior)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (overlayBehavior == com.example.util.AppPreferences.OVERLAY_MINI) "قائمة مصغرة" else "نافذة موسعة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }"""

new_ui = """                        val aiModel by com.example.data.AppSettingsPreferences.aiModel.collectAsState()
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
                        }"""

content = content.replace(target_ui, new_ui)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Done")
