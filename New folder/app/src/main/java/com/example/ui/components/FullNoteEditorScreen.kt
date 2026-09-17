package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.runtime.mutableIntStateOf
import com.example.ui.components.FullScreenImageZoomDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.BlockType
import com.example.data.DocumentBlock
import com.example.data.GameNote
import com.example.data.NoteType
import com.example.data.TodoItem
import com.example.ui.theme.ThemePreferences
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullNoteEditorScreen(
    note: GameNote?,
    initialGameTag: String,
    availableGameTabs: List<String>,
    onBack: () -> Unit,
    onSave: (GameNote) -> Unit,
    onAskGemini: (GameNote) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var gameTag by remember { mutableStateOf(note?.gameTag?.takeIf { it.isNotBlank() } ?: initialGameTag) }
    var isEditingGameTagDialog by remember { mutableStateOf(false) }
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Rich blocks list
    val blocks = remember {
        mutableStateListOf<DocumentBlock>().apply {
            if (note != null && note.blocksJson.isNotBlank() && note.blocksJson != "[]") {
                val loaded = DocumentBlock.jsonToList(note.blocksJson)
                val migrated = loaded.map { blk ->
                    if (blk.type == BlockType.IMAGE && blk.imageUri.isNotBlank()) {
                        blk.copy(imageUri = com.example.util.StorageUtils.saveImageToInternalStorage(context, blk.imageUri))
                    } else blk
                }
                addAll(migrated)
            } else if (note != null && (note.content.isNotBlank() || note.imageUris.isNotEmpty() || note.todoItems.isNotEmpty())) {
                // Migrate legacy plain text note into rich blocks
                if (note.content.isNotBlank()) {
                    add(DocumentBlock(type = BlockType.TEXT, text = note.content))
                }
                note.imageUris.forEach { uri ->
                    val persistentUri = com.example.util.StorageUtils.saveImageToInternalStorage(context, uri)
                    add(DocumentBlock(type = BlockType.IMAGE, imageUri = persistentUri, imageWidthPercent = 0.80f))
                }
                note.todoItems.forEach { item ->
                    add(DocumentBlock(type = BlockType.CHECKLIST, text = item.text, isChecked = item.isDone))
                }
                add(DocumentBlock(type = BlockType.TEXT, text = ""))
            } else {
                // Fresh note starts with empty text block
                add(DocumentBlock(type = BlockType.TEXT, text = ""))
            }
        }
    }

    var activeFocusedBlockIndex by remember { mutableIntStateOf(0) }
    var activeCursorPosition by remember { mutableIntStateOf(0) }

    // Photo picker for inline images - inserts precisely at the current cursor position
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val internalUriStr = com.example.util.StorageUtils.saveImageToInternalStorage(context, it)
            val newImageBlock = DocumentBlock(
                type = BlockType.IMAGE,
                imageUri = internalUriStr,
                imageWidthPercent = 0.80f
            )

            if (activeFocusedBlockIndex in 0 until blocks.size && blocks[activeFocusedBlockIndex].type == BlockType.TEXT) {
                val currentBlock = blocks[activeFocusedBlockIndex]
                val currentText = currentBlock.text
                val cursor = activeCursorPosition.coerceIn(0, currentText.length)
                val textBefore = currentText.take(cursor)
                val textAfter = currentText.drop(cursor)

                if (textBefore.isEmpty() && textAfter.isEmpty()) {
                    // Empty text block: replace with image, followed by empty text
                    blocks[activeFocusedBlockIndex] = newImageBlock
                    blocks.add(activeFocusedBlockIndex + 1, DocumentBlock(type = BlockType.TEXT, text = ""))
                    activeFocusedBlockIndex = activeFocusedBlockIndex + 1
                    activeCursorPosition = 0
                } else if (textBefore.isEmpty()) {
                    // Cursor at very start: insert image before text block
                    blocks.add(activeFocusedBlockIndex, newImageBlock)
                    activeFocusedBlockIndex = activeFocusedBlockIndex + 1
                    activeCursorPosition = 0
                } else if (textAfter.isEmpty()) {
                    // Cursor at very end: insert image after text block, followed by empty text
                    val insertIdx = activeFocusedBlockIndex + 1
                    blocks.add(insertIdx, newImageBlock)
                    blocks.add(insertIdx + 1, DocumentBlock(type = BlockType.TEXT, text = ""))
                    activeFocusedBlockIndex = insertIdx + 1
                    activeCursorPosition = 0
                } else {
                    // Cursor in the middle: split the text block around the image
                    blocks[activeFocusedBlockIndex] = currentBlock.copy(text = textBefore)
                    val insertIdx = activeFocusedBlockIndex + 1
                    blocks.add(insertIdx, newImageBlock)
                    blocks.add(insertIdx + 1, DocumentBlock(type = BlockType.TEXT, text = textAfter))
                    activeFocusedBlockIndex = insertIdx + 1
                    activeCursorPosition = 0
                }
            } else {
                // Fallback: insert at the end or right after the active block
                val targetIdx = (activeFocusedBlockIndex + 1).coerceIn(0, blocks.size)
                blocks.add(targetIdx, newImageBlock)
                blocks.add(targetIdx + 1, DocumentBlock(type = BlockType.TEXT, text = ""))
                activeFocusedBlockIndex = targetIdx + 1
                activeCursorPosition = 0
            }
        }
    }

    var activeBlockIndexForSecondImage by remember { mutableIntStateOf(-1) }
    val secondPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val internalUriStr = com.example.util.StorageUtils.saveImageToInternalStorage(context, it)
            val idx = activeBlockIndexForSecondImage
            if (idx in 0 until blocks.size) {
                val current = blocks[idx]
                blocks[idx] = current.copy(secondImageUri = internalUriStr)
            }
        }
    }

    val customTextColor = MaterialTheme.colorScheme.primary
    val topBarBg = customTextColor
    val topBarContent = ThemePreferences.getContrastingContentColor(topBarBg)

    val insertChecklistAtCurrentCursor: () -> Unit = {
        if (blocks.isEmpty()) {
            blocks.add(DocumentBlock(type = BlockType.CHECKLIST, text = "", isChecked = false))
            activeFocusedBlockIndex = 0
            activeCursorPosition = 0
        } else {
            val targetIndex = activeFocusedBlockIndex.coerceIn(0, blocks.size - 1)
            val currBlock = blocks[targetIndex]

            if (currBlock.type == BlockType.TEXT) {
                val text = currBlock.text
                val cursor = activeCursorPosition.coerceIn(0, text.length)
                val textBefore = text.substring(0, cursor)
                val textAfter = text.substring(cursor)

                val newChecklistBlock = DocumentBlock(
                    type = BlockType.CHECKLIST,
                    text = "",
                    isChecked = false
                )

                if (textBefore.isEmpty() && textAfter.isEmpty()) {
                    blocks[targetIndex] = newChecklistBlock
                    activeFocusedBlockIndex = targetIndex
                    activeCursorPosition = 0
                } else if (textBefore.isEmpty()) {
                    blocks.add(targetIndex, newChecklistBlock)
                    activeFocusedBlockIndex = targetIndex
                    activeCursorPosition = 0
                } else if (textAfter.isEmpty()) {
                    blocks.add(targetIndex + 1, newChecklistBlock)
                    activeFocusedBlockIndex = targetIndex + 1
                    activeCursorPosition = 0
                } else {
                    // Split text at exact cursor position!
                    blocks[targetIndex] = currBlock.copy(text = textBefore)
                    blocks.add(targetIndex + 1, newChecklistBlock)
                    blocks.add(targetIndex + 2, DocumentBlock(id = java.util.UUID.randomUUID().toString(), type = BlockType.TEXT, text = textAfter))
                    activeFocusedBlockIndex = targetIndex + 1
                    activeCursorPosition = 0
                }
            } else {
                val newChecklistBlock = DocumentBlock(
                    type = BlockType.CHECKLIST,
                    text = "",
                    isChecked = false
                )
                blocks.add(targetIndex + 1, newChecklistBlock)
                activeFocusedBlockIndex = targetIndex + 1
                activeCursorPosition = 0
            }
        }
    }

    fun saveCurrent() {
        val finalTitle = title.ifBlank { "ملاحظة جديدة" }
        // Extract plain text for search and backwards compatibility (including any text wrapped beside images)
        val extractedContent = blocks.mapNotNull { b ->
            when (b.type) {
                BlockType.TEXT -> b.text.takeIf { it.isNotBlank() }
                BlockType.IMAGE -> b.text.takeIf { it.isNotBlank() }
                BlockType.CHECKLIST -> null
            }
        }.joinToString("\n\n")

        val extractedImages = blocks.filter { it.type == BlockType.IMAGE && it.imageUri.isNotBlank() }.map { it.imageUri }
        val extractedTodos = blocks.filter { it.type == BlockType.CHECKLIST && it.text.isNotBlank() }.map {
            TodoItem(id = it.id, text = it.text, isDone = it.isChecked)
        }

        val saved = (note ?: GameNote(title = finalTitle)).copy(
            title = finalTitle,
            content = extractedContent,
            gameTag = gameTag,
            noteType = NoteType.REGULAR.name,
            blocksJson = DocumentBlock.listToJson(blocks),
            imageUris = extractedImages,
            todoItems = extractedTodos,
            updatedAt = System.currentTimeMillis()
        )
        onSave(saved)
    }

    Scaffold(
        topBar = {
            // Requirement 3: Top Bar containing Note Name, Game Name, and Action Buttons
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            saveCurrent()
                            onBack()
                        },
                        modifier = Modifier.testTag("full_note_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع وحفظ",
                            tint = topBarContent
                        )
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.clickable { isEditingGameTagDialog = true }
                    ) {
                        Text(
                            text = if (title.isBlank()) "ملاحظة جديدة" else title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = topBarContent,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = topBarContent.copy(alpha = 0.85f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (gameTag.isNotBlank()) gameTag else "عام (اضغط للتعديل)",
                                style = MaterialTheme.typography.labelSmall,
                                color = topBarContent.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل اللعبة",
                                tint = topBarContent.copy(alpha = 0.7f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Requirement 3: زر مخصص لإدراج/إضافة 'قائمة مهام' في أي مكان داخل الملاحظة (Inline at cursor)
                    IconButton(
                        onClick = {
                            insertChecklistAtCurrentCursor()
                        },
                        modifier = Modifier.testTag("action_insert_checklist")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "إدراج قائمة مهام",
                            tint = topBarContent
                        )
                    }

                    // زر إدراج صورة Inline
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("action_insert_image")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "إدراج صورة",
                            tint = topBarContent
                        )
                    }

                    // زر اسأل جيمني
                    IconButton(
                        onClick = {
                            val plainContent = blocks.filter { it.type == BlockType.TEXT }.joinToString("\n") { it.text }
                            val temp = (note ?: GameNote(title = title.ifBlank { "ملاحظة" })).copy(
                                title = title,
                                content = plainContent,
                                gameTag = gameTag
                            )
                            onAskGemini(temp)
                        },
                        modifier = Modifier.testTag("action_ask_gemini")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "اسأل جيمني",
                            tint = topBarContent
                        )
                    }

                    // زر حفظ
                    IconButton(
                        onClick = {
                            saveCurrent()
                            onBack()
                        },
                        modifier = Modifier.testTag("action_save_note")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "حفظ وإغلاق",
                            tint = topBarContent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBg)
            )
        },
        bottomBar = {
            // Quick rich insert toolbar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val idx = (activeFocusedBlockIndex + 1).coerceIn(0, blocks.size)
                            blocks.add(idx, DocumentBlock(type = BlockType.TEXT, text = ""))
                            activeFocusedBlockIndex = idx
                            activeCursorPosition = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ نص حر",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ صورة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            insertChecklistAtCurrentCursor()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ مهمة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Requirement 2: Full-Page Free Canvas Rich Text Editor (Like Google Keep / Notion)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 20.dp)
                .testTag("full_note_canvas"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Large Document Title (Borderless, modern Notion style - strictly single line)
                BasicTextField(
                    value = title,
                    onValueChange = { title = it.replace("\n", "").replace("\r", "") },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = "عنوان الملاحظة...",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_note_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            }

            // Rich document blocks
            itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
                when (block.type) {
                    BlockType.TEXT -> {
                        // Unconstrained text block: write freely anywhere with exact cursor tracking
                        var textVal by remember(block.id) {
                            mutableStateOf(TextFieldValue(text = block.text, selection = TextRange(block.text.length)))
                        }
                        if (textVal.text != block.text) {
                            textVal = textVal.copy(text = block.text)
                        }

                        BasicTextField(
                            value = textVal,
                            onValueChange = { newTfv ->
                                textVal = newTfv
                                blocks[index] = block.copy(text = newTfv.text)
                                activeFocusedBlockIndex = index
                                activeCursorPosition = newTfv.selection.start
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 26.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    if (state.isFocused) {
                                        activeFocusedBlockIndex = index
                                        activeCursorPosition = textVal.selection.start
                                    }
                                }
                                .testTag("rich_text_block_$index")
                        )
                    }

                    BlockType.IMAGE -> {
                        var isDragging by remember { mutableStateOf(false) }
                        var dragOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                        val isTwoImages = block.secondImageUri.isNotBlank()
                        val isAlignRight = block.imageAlignment != "LEFT"

                        if (isTwoImages) {
                            // Requirement 6: صورتين جنب بعض
                            // إذا بدك تحط صورة جنب صورة بروح خيار كتابة كلام جنب الصورة والصورتين بوخذوا عرض المكان بالتساوي
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                                    .graphicsLayer {
                                        if (isDragging) {
                                            scaleX = 1.02f
                                            scaleY = 1.02f
                                            alpha = 0.92f
                                        }
                                    }
                                    .pointerInput(block.id, blocks.size) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                isDragging = true
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val swapThreshold = 55.dp.toPx()
                                                if (dragOffsetY > swapThreshold && index < blocks.size - 1) {
                                                    val item = blocks.removeAt(index)
                                                    blocks.add(index + 1, item)
                                                    dragOffsetY = 0f
                                                } else if (dragOffsetY < -swapThreshold && index > 0) {
                                                    val item = blocks.removeAt(index)
                                                    blocks.add(index - 1, item)
                                                    dragOffsetY = 0f
                                                }
                                            },
                                            onDragEnd = {
                                                isDragging = false
                                                dragOffsetY = 0f
                                            },
                                            onDragCancel = {
                                                isDragging = false
                                                dragOffsetY = 0f
                                            }
                                        )
                                    }
                                    .testTag("dual_images_block_$index")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Image 1 (50% equal width)
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            AsyncImage(
                                                model = block.imageUri,
                                                contentDescription = "الصورة الأولى - اضغط للتكبير",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(150.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable { zoomedImageUri = block.imageUri }
                                            )
                                            // Zoom icon
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.55f),
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .align(Alignment.TopStart)
                                                    .clickable { zoomedImageUri = block.imageUri }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ZoomIn,
                                                    contentDescription = "تكبير الصورة الأولى",
                                                    tint = Color.White,
                                                    modifier = Modifier.padding(4.dp).size(14.dp)
                                                )
                                            }
                                            // Delete image 1 (promotes image 2 to primary)
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .align(Alignment.TopEnd)
                                                    .clickable {
                                                        blocks[index] = block.copy(imageUri = block.secondImageUri, secondImageUri = "")
                                                    }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "حذف الصورة الأولى",
                                                    tint = Color.White,
                                                    modifier = Modifier.padding(4.dp).size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Image 2 (50% equal width)
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            AsyncImage(
                                                model = block.secondImageUri,
                                                contentDescription = "الصورة الثانية - اضغط للتكبير",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(150.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable { zoomedImageUri = block.secondImageUri }
                                            )
                                            // Zoom icon
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.55f),
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .align(Alignment.TopStart)
                                                    .clickable { zoomedImageUri = block.secondImageUri }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ZoomIn,
                                                    contentDescription = "تكبير الصورة الثانية",
                                                    tint = Color.White,
                                                    modifier = Modifier.padding(4.dp).size(14.dp)
                                                )
                                            }
                                            // Delete image 2 (returns to single image)
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .align(Alignment.TopEnd)
                                                    .clickable {
                                                        blocks[index] = block.copy(secondImageUri = "")
                                                    }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "حذف الصورة الثانية",
                                                    tint = Color.White,
                                                    modifier = Modifier.padding(4.dp).size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Dual Image Footer (Drag Handle, Info label & remove block)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "اسحب مطولاً لإعادة الترتيب",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "صورتان متجاورتان بالتساوي (50% / 50%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { blocks.removeAt(index) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الصورتين",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Requirement 3: Single image with alignment (يمين أو شمال) + smart sizing + option to add second image
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                                    .graphicsLayer {
                                        if (isDragging) {
                                            scaleX = 1.02f
                                            scaleY = 1.02f
                                            alpha = 0.92f
                                        }
                                    }
                                    .pointerInput(block.id, blocks.size) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                isDragging = true
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val swapThreshold = 55.dp.toPx()
                                                if (dragOffsetY > swapThreshold && index < blocks.size - 1) {
                                                    val item = blocks.removeAt(index)
                                                    blocks.add(index + 1, item)
                                                    dragOffsetY = 0f
                                                } else if (dragOffsetY < -swapThreshold && index > 0) {
                                                    val item = blocks.removeAt(index)
                                                    blocks.add(index - 1, item)
                                                    dragOffsetY = 0f
                                                }
                                            },
                                            onDragEnd = {
                                                isDragging = false
                                                dragOffsetY = 0f
                                            },
                                            onDragCancel = {
                                                isDragging = false
                                                dragOffsetY = 0f
                                            }
                                        )
                                    }
                                    .testTag("inline_image_block_$index")
                            ) {
                                // Explicit LTR container to strictly position Right vs Left
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        val imageCardComposable = @Composable {
                                            Card(
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                border = if (isDragging) {
                                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                                } else {
                                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                                },
                                                elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 2.dp),
                                                modifier = Modifier
                                                    .weight(block.imageWidthPercent, fill = false)
                                                    .testTag("inline_image_card_$index")
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Box(modifier = Modifier.fillMaxWidth()) {
                                                        AsyncImage(
                                                            model = block.imageUri,
                                                            contentDescription = "صورة الملاحظة - انقر للتكبير",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height((210 * block.imageWidthPercent).dp.coerceAtLeast(110.dp))
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .clickable { zoomedImageUri = block.imageUri }
                                                        )

                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color.Black.copy(alpha = 0.5f),
                                                            modifier = Modifier
                                                                .padding(6.dp)
                                                                .align(Alignment.TopStart)
                                                                .clickable { zoomedImageUri = block.imageUri }
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ZoomIn,
                                                                contentDescription = "تكبير الصورة",
                                                                tint = Color.White,
                                                                modifier = Modifier.padding(4.dp).size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        val textCardComposable = @Composable {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                                modifier = Modifier
                                                    .weight(1f - block.imageWidthPercent)
                                                    .padding(top = 2.dp)
                                            ) {
                                                // RTL inside text box for Arabic typing
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                                    BasicTextField(
                                                        value = block.text,
                                                        onValueChange = { newText ->
                                                            blocks[index] = block.copy(text = newText)
                                                        },
                                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                            lineHeight = 22.sp
                                                        ),
                                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                        decorationBox = { innerTextField ->
                                                            Box(modifier = Modifier.padding(10.dp)) {
                                                                if (block.text.isEmpty()) {
                                                                    Text(
                                                                        text = "اكتب نصاً بجانب الصورة...",
                                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                                        )
                                                                    )
                                                                }
                                                                innerTextField()
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .onFocusChanged { state ->
                                                                if (state.isFocused) {
                                                                    activeFocusedBlockIndex = index
                                                                    activeCursorPosition = block.text.length
                                                                }
                                                            }
                                                    )
                                                }
                                            }
                                        }

                                        if (isAlignRight) {
                                            // LTR order: Text on Left, Image on Right
                                            if (block.imageWidthPercent < 1.0f) {
                                                textCardComposable()
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            imageCardComposable()
                                        } else {
                                            // LTR order: Image on Left, Text on Right
                                            imageCardComposable()
                                            if (block.imageWidthPercent < 1.0f) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                textCardComposable()
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Controls Panel: Alignment Toggle + Preset Sizing + Smooth Slider + Add 2nd Image + Delete
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        // Row 1: Alignment toggle & Add second image button & Delete button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Alignment Switch: يمين / شمال
                                                Surface(
                                                    onClick = {
                                                        val newAlign = if (isAlignRight) "LEFT" else "RIGHT"
                                                        blocks[index] = block.copy(imageAlignment = newAlign)
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.SwapHoriz,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp),
                                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (isAlignRight) "المحاذاة: يمين" else "المحاذاة: شمال",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))

                                                // Add Second Image Side-by-Side button
                                                Surface(
                                                    onClick = {
                                                        activeBlockIndexForSecondImage = index
                                                        secondPhotoPickerLauncher.launch(
                                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        )
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Collections,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp),
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "+ صورة بجانبها",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                            }

                                            IconButton(
                                                onClick = { blocks.removeAt(index) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف الصورة",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Row 2: Sizing Presets (30%, 50%, 75%, 100%) + Slider
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "الحجم:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))

                                            val presets = listOf(0.30f to "30%", 0.50f to "50%", 0.75f to "75%", 1.00f to "100%")
                                            presets.forEach { (pct, label) ->
                                                val isCur = kotlin.math.abs(block.imageWidthPercent - pct) < 0.05f
                                                Surface(
                                                    onClick = { blocks[index] = block.copy(imageWidthPercent = pct) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isCur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        0.5.dp,
                                                        if (isCur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 2.dp)
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isCur) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Smooth Slider for fine tuning
                                            androidx.compose.material3.Slider(
                                                value = block.imageWidthPercent,
                                                onValueChange = { blocks[index] = block.copy(imageWidthPercent = it) },
                                                valueRange = 0.25f..1.0f,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(26.dp)
                                                    .testTag("image_size_slider_$index")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BlockType.CHECKLIST -> {
                        // Requirement: Embedded checklist item inside the document
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = block.isChecked,
                                onCheckedChange = { checked ->
                                    blocks[index] = block.copy(isChecked = checked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = block.text,
                                onValueChange = { newText ->
                                    if (newText.contains("\n")) {
                                        val parts = newText.split("\n", limit = 2)
                                        // If current item is empty when Enter was pressed, switch seamlessly to regular TEXT!
                                        if (block.text.isBlank()) {
                                            blocks[index] = DocumentBlock(
                                                type = BlockType.TEXT,
                                                text = if (parts.size > 1) parts[1] else ""
                                            )
                                        } else {
                                            blocks[index] = block.copy(text = parts[0])
                                            blocks.add(
                                                index + 1,
                                                DocumentBlock(
                                                    type = BlockType.CHECKLIST,
                                                    text = if (parts.size > 1) parts[1] else "",
                                                    isChecked = false
                                                )
                                            )
                                        }
                                    } else {
                                        blocks[index] = block.copy(text = newText)
                                    }
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (block.isChecked) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                                    textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        if (block.text.isBlank()) {
                                            blocks[index] = DocumentBlock(type = BlockType.TEXT, text = "")
                                        } else {
                                            blocks.add(
                                                index + 1,
                                                DocumentBlock(type = BlockType.CHECKLIST, text = "", isChecked = false)
                                            )
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { state ->
                                        if (state.isFocused) {
                                            activeFocusedBlockIndex = index
                                            activeCursorPosition = block.text.length
                                        }
                                    }
                            )
                            IconButton(
                                onClick = { blocks.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف المهمة",
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Requirement: Tap to resume writing standard text below checklists / canvas
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .clickable {
                            val last = blocks.lastOrNull()
                            if (last == null || last.type != BlockType.TEXT || last.text.isNotBlank()) {
                                blocks.add(DocumentBlock(type = BlockType.TEXT, text = ""))
                            }
                        }
                        .padding(top = 16.dp, bottom = 64.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (blocks.lastOrNull()?.type == BlockType.CHECKLIST) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable {
                                blocks.add(DocumentBlock(type = BlockType.TEXT, text = ""))
                                activeFocusedBlockIndex = blocks.size - 1
                                activeCursorPosition = 0
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "المس هنا لاستئناف كتابة نص عادي أسفل المهام...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    // Dialog for changing Game Tag
    if (isEditingGameTagDialog) {
        var customTagInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { isEditingGameTagDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "اختر أو غيّر تبويب اللعبة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (availableGameTabs.isNotEmpty()) {
                        Text(
                            text = "التبويبات المتاحة:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        availableGameTabs.forEach { tab ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gameTag = tab
                                        isEditingGameTagDialog = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (tab == gameTag) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { customTagInput = it },
                        label = { Text("أو اكتب اسم لعبة جديد") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (customTagInput.isNotBlank()) {
                                    gameTag = customTagInput.trim()
                                }
                                isEditingGameTagDialog = false
                            }
                        ) {
                            Text("تأكيد")
                        }
                    }
                }
            }
        }
    }

    // Fullscreen zoomable image dialog
    if (zoomedImageUri != null) {
        FullScreenImageZoomDialog(
            imageUri = zoomedImageUri!!,
            onDismiss = { zoomedImageUri = null }
        )
    }
}
