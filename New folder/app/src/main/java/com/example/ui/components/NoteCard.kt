package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.BlockType
import com.example.data.DocumentBlock
import com.example.data.GameNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: GameNote,
    onToggleTodo: (String) -> Unit,
    onAskGemini: (GameNote) -> Unit,
    onEdit: (GameNote) -> Unit,
    onDelete: (GameNote) -> Unit,
    onTogglePin: ((GameNote) -> Unit)? = null,
    onPinAsMiniWidget: ((GameNote) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(!isCompact) }
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    val gameTagColor = remember(note.gameTag) {
        when {
            note.gameTag.contains("Stardew", ignoreCase = true) -> Color(0xFF10B981)
            note.gameTag.contains("Minecraft", ignoreCase = true) -> Color(0xFF0EA5E9)
            note.gameTag.contains("RPG", ignoreCase = true) -> Color(0xFFA855F7)
            note.gameTag.contains("Elden", ignoreCase = true) -> Color(0xFFF59E0B)
            else -> Color(0xFF06B6D4)
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(note.updatedAt) { dateFormatter.format(Date(note.updatedAt)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}")
            .clickable { onEdit(note) }
            .border(
                width = if (note.isPinned) 1.5.dp else 1.dp,
                color = if (note.isPinned) gameTagColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 10.dp else 14.dp)
        ) {
            // Header: Type badge, Game badge, Date, and Pin action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Note Type Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (note.isTodoList) Color(0xFF0284C7).copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (note.isTodoList) Icons.Default.Checklist else Icons.AutoMirrored.Filled.Article,
                                contentDescription = null,
                                tint = if (note.isTodoList) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (note.isTodoList) "قائمة مهام" else "ملاحظة",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (note.isTodoList) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Game Tag Badge if available
                    if (note.gameTag.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = gameTagColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, gameTagColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = gameTagColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = note.gameTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = gameTagColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    if (onTogglePin != null) {
                        IconButton(
                            onClick = { onTogglePin(note) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("pin_button_${note.id}")
                        ) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "تثبيت الملاحظة",
                                tint = if (note.isPinned) gameTagColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = note.title,
                style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Requirement 8: Unified Preview: Display text, images, and tasks integrated together exactly as they look inside
            val parsedBlocks = remember(note.blocksJson) {
                if (note.blocksJson.isNotBlank() && note.blocksJson != "[]") {
                    DocumentBlock.jsonToList(note.blocksJson).filter { b ->
                        b.text.isNotBlank() || b.imageUri.isNotBlank()
                    }
                } else {
                    emptyList()
                }
            }

            if (parsedBlocks.isNotEmpty()) {
                val displayBlocks = if (isExpanded) parsedBlocks else parsedBlocks.take(if (isCompact) 3 else 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displayBlocks.forEach { block ->
                        when (block.type) {
                            BlockType.TEXT -> {
                                if (block.text.isNotBlank()) {
                                    Text(
                                        text = block.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                            BlockType.IMAGE -> {
                                if (block.secondImageUri.isNotBlank()) {
                                    // Dual images side by side (equal width)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                                .clickable { zoomedImageUri = block.imageUri }
                                        ) {
                                            AsyncImage(
                                                model = block.imageUri,
                                                contentDescription = "صورة الملاحظة الأولى",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(if (isCompact) 80.dp else 105.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                                .clickable { zoomedImageUri = block.secondImageUri }
                                        ) {
                                            AsyncImage(
                                                model = block.secondImageUri,
                                                contentDescription = "صورة الملاحظة الثانية",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(if (isCompact) 80.dp else 105.dp)
                                            )
                                        }
                                    }
                                } else if (block.imageUri.isNotBlank()) {
                                    val isAlignRight = block.imageAlignment != "LEFT"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        val imgBox = @Composable {
                                            Box(
                                                modifier = Modifier
                                                    .weight(block.imageWidthPercent.coerceIn(0.25f, 1.0f), fill = false)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                                    .clickable { zoomedImageUri = block.imageUri }
                                            ) {
                                                AsyncImage(
                                                    model = block.imageUri,
                                                    contentDescription = "صورة الملاحظة - اضغط للتكبير",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(if (isCompact) (95 * block.imageWidthPercent).dp.coerceAtLeast(60.dp) else (130 * block.imageWidthPercent).dp.coerceAtLeast(80.dp))
                                                )
                                            }
                                        }

                                        val textBox = @Composable {
                                            if (block.imageWidthPercent < 1.0f && block.text.isNotBlank()) {
                                                Text(
                                                    text = block.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f - block.imageWidthPercent.coerceIn(0.25f, 1.0f))
                                                )
                                            }
                                        }

                                        if (isAlignRight) {
                                            imgBox()
                                            if (block.imageWidthPercent < 1.0f && block.text.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                textBox()
                                            }
                                        } else {
                                            if (block.imageWidthPercent < 1.0f && block.text.isNotBlank()) {
                                                textBox()
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            imgBox()
                                        }
                                    }
                                }
                            }
                            BlockType.CHECKLIST -> {
                                if (block.text.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleTodo(block.id) }
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Checkbox(
                                            checked = block.isChecked,
                                            onCheckedChange = { onToggleTodo(block.id) },
                                            modifier = Modifier.size(22.dp),
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.primary,
                                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = block.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (block.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                            textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (!isExpanded && parsedBlocks.size > displayBlocks.size) {
                        Text(
                            text = "... +${parsedBlocks.size - displayBlocks.size} عنصر إضافي",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                // Fallback for legacy notes created before rich blocks
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (note.imageUris.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        note.imageUris.take(if (isExpanded) 6 else 2).forEach { uriString ->
                            AsyncImage(
                                model = uriString,
                                contentDescription = "صورة مرفقة بالملاحظة - اضغط للتكبير",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(if (isCompact) 56.dp else 72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable { zoomedImageUri = uriString }
                            )
                        }
                    }
                }
                if (note.todoItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    note.todoItems.take(if (isExpanded) note.todoItems.size else 3).forEach { todo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTodo(todo.id) }
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = todo.isDone,
                                onCheckedChange = { onToggleTodo(todo.id) },
                                modifier = Modifier.size(22.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = todo.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left action button based on type
                if (!note.isTodoList) {
                    Button(
                        onClick = { onAskGemini(note) },
                        modifier = Modifier
                            .testTag("ask_gemini_button_${note.id}")
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اسأل جيمني",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (onPinAsMiniWidget != null) {
                    // Pin as floating mini widget button for To-Do lists
                    Button(
                        onClick = { onPinAsMiniWidget(note) },
                        modifier = Modifier
                            .testTag("pin_as_mini_widget_${note.id}")
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7).copy(alpha = 0.2f),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تثبيت كقائمة مصغرة",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Edit & Delete icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onEdit(note) },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("edit_note_button_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDelete(note) },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_note_button_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }

    if (zoomedImageUri != null) {
        FullScreenImageZoomDialog(
            imageUri = zoomedImageUri!!,
            onDismiss = { zoomedImageUri = null }
        )
    }
}
