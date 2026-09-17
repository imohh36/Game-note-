package com.example.ui.components

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GameNote
import com.example.data.NoteType
import com.example.data.TodoItem
import com.example.ui.theme.ThemePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEditorScreen(
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

    // List of checkable tasks: starts with 1 item by default if empty
    val items = remember {
        mutableStateListOf<TodoItem>().apply {
            if (note != null && note.todoItems.isNotEmpty()) {
                addAll(note.todoItems)
            } else {
                add(TodoItem(text = "", isDone = false))
            }
        }
    }

    // Focus requesters for auto-focus on Enter key
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }
    var pendingFocusIndex by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Handle auto-focus when a new item is created via Enter
    LaunchedEffect(pendingFocusIndex, items.size) {
        pendingFocusIndex?.let { index ->
            delay(50)
            focusRequesters[index]?.requestFocus()
            listState.animateScrollToItem((index + 1).coerceAtMost(items.size))
            pendingFocusIndex = null
        }
    }

    val customTextColor = MaterialTheme.colorScheme.primary
    val topBarBg = customTextColor
    val topBarContent = ThemePreferences.getContrastingContentColor(topBarBg)

    // Progress metrics
    val totalCount = items.size
    val completedCount = items.count { it.isDone }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    fun saveCurrent() {
        val filteredTodos = items.filter { it.text.isNotBlank() }
        val finalTitle = title.ifBlank { "قائمة مهام جديدة" }
        val saved = (note ?: GameNote(title = finalTitle)).copy(
            title = finalTitle,
            gameTag = gameTag,
            noteType = NoteType.TODO_LIST.name,
            todoItems = filteredTodos,
            updatedAt = System.currentTimeMillis()
        )
        onSave(saved)
    }

    Scaffold(
        topBar = {
            // Requirement 3: Top Bar containing Note Name, Game Name, and Opposite Action Buttons
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            saveCurrent()
                            onBack()
                        },
                        modifier = Modifier.testTag("checklist_back_button")
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
                            text = if (title.isBlank()) "قائمة مهام جديدة" else title,
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
                    // Gemini Action
                    IconButton(
                        onClick = {
                            val tempNote = (note ?: GameNote(title = title.ifBlank { "قائمة مهام" })).copy(
                                title = title,
                                gameTag = gameTag,
                                todoItems = items.toList()
                            )
                            onAskGemini(tempNote)
                        },
                        modifier = Modifier.testTag("checklist_ask_gemini_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "اسأل جيمني",
                            tint = topBarContent
                        )
                    }

                    // Done / Save button
                    IconButton(
                        onClick = {
                            saveCurrent()
                            onBack()
                        },
                        modifier = Modifier.testTag("checklist_done_button")
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Requirement 4: المربع الأول العلوي: مخصص لكتابة عنوان قائمة المهام
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checklist_title_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "عنوان قائمة المهام:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.replace("\n", "").replace("\r", "") },
                        placeholder = { Text("مثال: مهام اليوم 31 ستاردو، أو متطلبات بوابة النذر...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checklist_title_input"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Progress indicator if items exist
                    if (items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الإنجاز: $completedCount من $totalCount مهام",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Requirement 4: المربع الكبير السفلي (الذي يغطي الشاشة)
            // يبدأ تلقائياً برقم 1. وبجانبه مربع اختيار فاضي (Checkbox).
            // وعند الضغط على Enter في الكيبورد، ينشئ تلقائياً السطر التالي (2.) ومربع اختيار فاضي!
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("checklist_items_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "عناصر المهام (اضغط Enter للانتقال للمهمة التالية تلقائياً):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = {
                                val newIndex = items.size
                                items.add(TodoItem(text = "", isDone = false))
                                pendingFocusIndex = newIndex
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة مهمة يدوياً",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                            val requester = focusRequesters.getOrPut(index) { FocusRequester() }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Auto-numbering: 1., 2., 3., etc.
                                Surface(
                                    shape = CircleShape,
                                    color = if (item.isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Checkbox: checks / unchecks the task
                                Checkbox(
                                    checked = item.isDone,
                                    onCheckedChange = { isChecked ->
                                        items[index] = item.copy(isDone = isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("checklist_checkbox_$index")
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Task Text Field: auto-handles Enter key to insert next numbered item!
                                TextField(
                                    value = item.text,
                                    onValueChange = { newText ->
                                        if (newText.contains("\n")) {
                                            // Intercept Enter key!
                                            val parts = newText.split("\n", limit = 2)
                                            val currentText = parts[0]
                                            val nextText = if (parts.size > 1) parts[1] else ""
                                            items[index] = item.copy(text = currentText)
                                            val insertIndex = index + 1
                                            items.add(insertIndex, TodoItem(text = nextText, isDone = false))
                                            pendingFocusIndex = insertIndex
                                        } else {
                                            items[index] = item.copy(text = newText)
                                        }
                                    },
                                    placeholder = {
                                        Text(
                                            text = "اكتب المهمة ${index + 1}...",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            // Pressing IME Next creates next numbered task
                                            val insertIndex = index + 1
                                            items.add(insertIndex, TodoItem(text = "", isDone = false))
                                            pendingFocusIndex = insertIndex
                                        }
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(requester)
                                        .testTag("checklist_item_input_$index")
                                )

                                // Delete task button
                                IconButton(
                                    onClick = {
                                        if (items.size > 1) {
                                            items.removeAt(index)
                                        } else {
                                            items[0] = TodoItem(text = "", isDone = false)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف المهمة",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for editing Game Tag
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
}
