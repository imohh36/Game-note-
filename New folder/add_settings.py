import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                        }
                    }
                }
            }
        }
    ) {"""

new_items = """                    // AI Settings & Overlay Behavior
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("إعدادات متقدمة", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val aiModel by com.example.util.AppPreferences.aiModel.collectAsState(initial = com.example.util.AppPreferences.MODEL_FLASH)
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

content = content.replace(target, new_items)

# Add isTrashDialogOpen variable at the top
target2 = "    var isAddingTabDialogOpen by remember { mutableStateOf(false) }"
replacement2 = """    var isAddingTabDialogOpen by remember { mutableStateOf(false) }
    var isTrashDialogOpen by remember { mutableStateOf(false) }"""
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
