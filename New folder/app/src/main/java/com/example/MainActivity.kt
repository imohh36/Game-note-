package com.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AiModelOption
import com.example.data.AppSettingsPreferences
import com.example.data.FloatingTaskDisplayMode
import com.example.data.GameNote
import com.example.data.GameTab
import com.example.data.NoteType
import com.example.service.FloatingOverlayService
import com.example.ui.components.ChecklistEditorScreen
import com.example.ui.components.FullNoteEditorScreen
import com.example.ui.components.GeminiChatSheet
import com.example.ui.components.NoteCard
import com.example.ui.components.NoteTypeSelectionDialog
import com.example.ui.components.TrashManagementDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GameNotesMainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                FloatingOverlayService.start(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to auto-start overlay in onResume", e)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameNotesMainScreen() {
    val context = LocalContext.current
    val repository = remember { GameNotesApp.instance.repository }
    val coroutineScope = rememberCoroutineScope()

    val isOverlayServiceRunning by FloatingOverlayService.isServiceRunning.collectAsState()

    // Dynamic Theme colors
    val customTextColor by ThemePreferences.textColor.collectAsState()
    val customBgColor by ThemePreferences.bgColor.collectAsState()
    val topBarContentColor = remember(customTextColor) {
        ThemePreferences.getContrastingContentColor(customTextColor)
    }

    // Navigation Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Dynamic Tabs from Database (Starts completely empty)
    val allTabs by repository.allTabs.collectAsState(initial = emptyList())
    var selectedGameTag by remember { mutableStateOf("الكل") }
    var isAddingTabExpanded by remember { mutableStateOf(false) }
    var newGameTabInputText by remember { mutableStateOf("") }
    val tabInputFocusRequester = remember { FocusRequester() }
    var tabToDelete by remember { mutableStateOf<GameTab?>(null) }

    // Auto-focus input when the tab input box is opened
    LaunchedEffect(isAddingTabExpanded) {
        if (isAddingTabExpanded) {
            try {
                tabInputFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // If user deleted the active tab or tabs became available
    LaunchedEffect(allTabs) {
        if (allTabs.isNotEmpty() && selectedGameTag == "الكل" && allTabs.size == 1) {
            selectedGameTag = allTabs.first().name
        } else if (allTabs.isNotEmpty() && selectedGameTag != "الكل" && allTabs.none { it.name == selectedGameTag }) {
            selectedGameTag = allTabs.first().name
        }
    }

    // Search query & Notes
    var searchQuery by remember { mutableStateOf("") }
    val allNotes by repository.allNotes.collectAsState(initial = emptyList())

    val filteredNotes = remember(allNotes, selectedGameTag, searchQuery) {
        allNotes.filter { note ->
            val matchesTag = selectedGameTag == "الكل" || note.gameTag.equals(selectedGameTag, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.todoItems.any { it.text.contains(searchQuery, ignoreCase = true) }
            matchesTag && matchesSearch
        }
    }

    // Note creation / Full Editor states
    var isSelectingNoteType by remember { mutableStateOf(false) }
    var activeFullEditorType by remember { mutableStateOf<NoteType?>(null) }
    var noteToEdit by remember { mutableStateOf<GameNote?>(null) }
    var activeTargetGameTag by remember { mutableStateOf("") }

    // Gemini Assistant Sheet state
    var selectedNoteForGemini by remember { mutableStateOf<GameNote?>(null) }
    var isGeminiSheetOpen by remember { mutableStateOf(false) }
    val geminiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Trash Management dialog state
    var showTrashDialog by remember { mutableStateOf(false) }

    // App Settings Preferences
    val floatingTaskMode by AppSettingsPreferences.floatingTaskMode.collectAsState()
    val aiModel by AppSettingsPreferences.aiModel.collectAsState()
    val isMiniTaskListEnabled by AppSettingsPreferences.isMiniTaskListEnabled.collectAsState()
    val userGeminiApiKey by AppSettingsPreferences.userGeminiApiKey.collectAsState()

    // Main Scaffold SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    // Overlay permissions launcher
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
            FloatingOverlayService.start(context)
            Toast.makeText(context, "تم تفعيل الأداة العائمة بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-launch overlay on app startup if permitted, or request permission
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                FloatingOverlayService.start(context)
            } else {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    overlayPermissionLauncher.launch(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error launching overlay permission", e)
                }
            }
        } else {
            FloatingOverlayService.start(context)
        }
    }

    // Deep navigation from Floating Overlay (Open note in full editor)
    val activity = context as? Activity
    LaunchedEffect(activity?.intent) {
        val targetId = activity?.intent?.getLongExtra("target_note_id", -1L) ?: -1L
        if (targetId != -1L) {
            coroutineScope.launch {
                val note = repository.getNoteByIdDirect(targetId)
                if (note != null) {
                    noteToEdit = note
                    activeFullEditorType = if (note.isTodoList) NoteType.TODO_LIST else NoteType.REGULAR
                }
            }
        }
    }

    // Helper to start creating a new note bound immediately to the active game tab
    fun startCreatingNote(defaultType: NoteType? = null) {
        activeTargetGameTag = if (selectedGameTag != "الكل") selectedGameTag else (allTabs.firstOrNull()?.name ?: "")
        if (defaultType == null) {
            isSelectingNoteType = true
        } else {
            noteToEdit = null
            activeFullEditorType = defaultType
        }
    }

    // =========================================================================
    // FULL SCREEN EDITORS (With integrated Gemini Assistant support)
    // =========================================================================

    // 1. Checklist Full Page Editor (Numbered auto-enter)
    if (activeFullEditorType == NoteType.TODO_LIST || (noteToEdit != null && noteToEdit!!.isTodoList)) {
        val targetGame = noteToEdit?.gameTag?.takeIf { it.isNotBlank() } ?: activeTargetGameTag
        Box(modifier = Modifier.fillMaxSize()) {
            ChecklistEditorScreen(
                note = noteToEdit,
                initialGameTag = targetGame,
                availableGameTabs = allTabs.map { it.name },
                onBack = {
                    noteToEdit = null
                    activeFullEditorType = null
                },
                onSave = { savedNote ->
                    coroutineScope.launch {
                        try {
                            if (noteToEdit == null) {
                                repository.insertNote(savedNote)
                            } else {
                                repository.updateNote(savedNote)
                            }
                            noteToEdit = null
                            activeFullEditorType = null
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Error saving checklist note", e)
                        }
                    }
                },
                onAskGemini = { note ->
                    selectedNoteForGemini = note
                    isGeminiSheetOpen = true
                }
            )

            if (isGeminiSheetOpen && selectedNoteForGemini != null) {
                val geminiNote = selectedNoteForGemini!!
                val promptContext = buildString {
                    append(geminiNote.title)
                    if (geminiNote.content.isNotBlank()) append("\n").append(geminiNote.content)
                    val pending = geminiNote.todoItems.filter { !it.isDone }
                    if (pending.isNotEmpty()) {
                        append("\nالمهام: ")
                        append(pending.joinToString(", ") { it.text })
                    }
                }

                ModalBottomSheet(
                    onDismissRequest = {
                        isGeminiSheetOpen = false
                        selectedNoteForGemini = null
                    },
                    sheetState = geminiSheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    GeminiChatSheet(
                        initialPrompt = promptContext,
                        noteTitle = geminiNote.title,
                        gameTag = geminiNote.gameTag,
                        onClose = {
                            isGeminiSheetOpen = false
                            selectedNoteForGemini = null
                        }
                    )
                }
            }
        }
        return
    }

    // 2. Full-Page Rich Text Document Editor (Unbounded canvas with inline resizable & movable images)
    if (activeFullEditorType == NoteType.REGULAR || (noteToEdit != null && !noteToEdit!!.isTodoList)) {
        val targetGame = noteToEdit?.gameTag?.takeIf { it.isNotBlank() } ?: activeTargetGameTag
        Box(modifier = Modifier.fillMaxSize()) {
            FullNoteEditorScreen(
                note = noteToEdit,
                initialGameTag = targetGame,
                availableGameTabs = allTabs.map { it.name },
                onBack = {
                    noteToEdit = null
                    activeFullEditorType = null
                },
                onSave = { savedNote ->
                    coroutineScope.launch {
                        try {
                            if (noteToEdit == null) {
                                repository.insertNote(savedNote)
                            } else {
                                repository.updateNote(savedNote)
                            }
                            noteToEdit = null
                            activeFullEditorType = null
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Error saving note", e)
                        }
                    }
                },
                onAskGemini = { note ->
                    selectedNoteForGemini = note
                    isGeminiSheetOpen = true
                }
            )

            if (isGeminiSheetOpen && selectedNoteForGemini != null) {
                val geminiNote = selectedNoteForGemini!!
                val promptContext = buildString {
                    append(geminiNote.title)
                    if (geminiNote.content.isNotBlank()) append("\n").append(geminiNote.content)
                    val pending = geminiNote.todoItems.filter { !it.isDone }
                    if (pending.isNotEmpty()) {
                        append("\nالمهام: ")
                        append(pending.joinToString(", ") { it.text })
                    }
                }

                ModalBottomSheet(
                    onDismissRequest = {
                        isGeminiSheetOpen = false
                        selectedNoteForGemini = null
                    },
                    sheetState = geminiSheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    GeminiChatSheet(
                        initialPrompt = promptContext,
                        noteTitle = geminiNote.title,
                        gameTag = geminiNote.gameTag,
                        onClose = {
                            isGeminiSheetOpen = false
                            selectedNoteForGemini = null
                        }
                    )
                }
            }
        }
        return
    }

    // =========================================================================
    // MAIN APP VIEW & TAB HIERARCHY
    // =========================================================================

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(320.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Drawer Header
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(customTextColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = topBarContentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "إعدادات GameNotes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تخصيص الألوان والمظهر",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    // Section 1: Custom Text Color
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "لون النصوص والشريط العلوي:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(customTextColor)
                                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ThemePreferences.textColorPresets.forEach { preset ->
                                        val isSelected = preset.color == customTextColor
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(preset.color)
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    ThemePreferences.setTextColor(context, preset.color)
                                                }
                                                .testTag("text_color_chip_${preset.name}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "محدد",
                                                    tint = ThemePreferences.getContrastingContentColor(preset.color),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: Custom Background Color
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "لون الخلفية الأساسية:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(customBgColor)
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ThemePreferences.bgColorPresets.forEach { preset ->
                                        val isSelected = preset.color == customBgColor
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(preset.color)
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    ThemePreferences.setBgColor(context, preset.color)
                                                }
                                                .testTag("bg_color_chip_${preset.name}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "محدد",
                                                    tint = ThemePreferences.getContrastingContentColor(preset.color),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reset button
                    item {
                        OutlinedButton(
                            onClick = { ThemePreferences.resetToDefaults(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة الألوان الافتراضية")
                        }
                    }

                    // Overlay shortcut
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الأداة العائمة (Overlay)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Switch(
                                checked = isOverlayServiceRunning,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            overlayPermissionLauncher.launch(intent)
                                        } else {
                                            FloatingOverlayService.start(context)
                                        }
                                    } else {
                                        FloatingOverlayService.stop(context)
                                    }
                                }
                            )
                        }
                    }

                    // Trash Management (سلة المهملات - استعادة وحذف نهائي)
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                showTrashDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("drawer_trash_management_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "سلة المهملات",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "استعادة الملاحظات المحذوفة أو تفريغ السلة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Settings: Bring Your Own Key (BYOK) & AI Model Selection
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الذكاء الاصطناعي (Gemini)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "أدخل مفتاحك الخاص لتفعيل ميزات الذكاء الاصطناعي واختيار النموذج",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 1. Mandatory BYOK API Key Input Field
                            var apiKeyInput by remember(userGeminiApiKey) { mutableStateOf(userGeminiApiKey) }
                            var isKeyVisible by remember { mutableStateOf(false) }

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { newVal ->
                                    apiKeyInput = newVal
                                    AppSettingsPreferences.setUserGeminiApiKey(context, newVal)
                                },
                                label = { Text("مفتاح Gemini API الخاص بك (إجباري)", fontSize = 12.sp) },
                                placeholder = { Text("AIzaSy...", fontSize = 11.sp) },
                                singleLine = true,
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = if (userGeminiApiKey.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                        Icon(
                                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (isKeyVisible) "إخفاء المفتاح" else "إظهار المفتاح",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_api_key_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (userGeminiApiKey.isNotBlank()) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Lock State or Active Model Selector
                            val isAiUnlocked = userGeminiApiKey.isNotBlank()

                            if (!isAiUnlocked) {
                                // Locked State Banner
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "مقفول",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "ميزات الذكاء الاصطناعي مقفلة",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "يرجى كتابة أو لصق مفتاح Gemini API الخاص بك أعلاه لفتح قائمة النماذج وتفعيل المساعد الذكي.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Active State: 3 Specific Models
                                Text(
                                    text = "اختر النموذج المناسب لاستهلاكك:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                AiModelOption.entries.forEach { option ->
                                    val isSelected = aiModel == option
                                    Surface(
                                        onClick = { AppSettingsPreferences.setAiModel(context, option) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .testTag("ai_model_option_${option.modelId}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { AppSettingsPreferences.setAiModel(context, option) }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = option.displayName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                        modifier = Modifier.padding(horizontal = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = option.badge,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = option.description,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Settings: Floating Task Display Mode (طريقة عرض المهام العائمة)
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "طريقة عرض المهام العائمة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "اختر كيفية ظهور قوائم المهام داخل الأداة العائمة (Overlay)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FloatingTaskDisplayMode.entries.forEach { mode ->
                                val isSelected = floatingTaskMode == mode
                                Surface(
                                    onClick = { AppSettingsPreferences.setFloatingTaskMode(context, mode) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { AppSettingsPreferences.setFloatingTaskMode(context, mode) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mode.label,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = mode.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Settings: Mini Task-List Toggle (قائمة المهام المصغرة للنافذة العائمة)
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "قائمة المهام المصغرة",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isMiniTaskListEnabled)
                                                "مفعل: تصغير المهام إلى ويدجت عائم مصغر"
                                            else
                                                "معطل: عرض المهام بالحجم الطبيعي (نصف شاشة)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = isMiniTaskListEnabled,
                                    onCheckedChange = { isChecked ->
                                        AppSettingsPreferences.setMiniTaskListEnabled(context, isChecked)
                                    },
                                    modifier = Modifier.testTag("mini_task_list_toggle_switch")
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        // Trash Management Dialog
        if (showTrashDialog) {
            TrashManagementDialog(
                repository = repository,
                onDismiss = { showTrashDialog = false }
            )
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_notes_main_scaffold"),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Top Bar background matches the app's main text color
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "القائمة الجانبية والإعدادات",
                                tint = topBarContentColor
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(topBarContentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = topBarContentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "GameNotes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = topBarContentColor
                                )
                                Text(
                                    text = if (selectedGameTag != "الكل") "تبويب: $selectedGameTag" else "مفكرة اللاعبين الذكية",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = topBarContentColor.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick floating overlay toggle
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = topBarContentColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, topBarContentColor.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    if (isOverlayServiceRunning) {
                                        FloatingOverlayService.stop(context)
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            overlayPermissionLauncher.launch(intent)
                                        } else {
                                            FloatingOverlayService.start(context)
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = topBarContentColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOverlayServiceRunning) "العائمة نشطة" else "العائمة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = topBarContentColor
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = customTextColor
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { startCreatingNote() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_note_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedGameTag != "الكل") "ملاحظة لـ $selectedGameTag" else "ملاحظة جديدة",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = 14.dp)
            ) {
                // =============================================================
                // Requirement 1: Tabs Bar & Management (Sticky Top)
                // =============================================================
                Spacer(modifier = Modifier.height(8.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تبويبات الألعاب:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Add game tab button (+)
                        Surface(
                            onClick = { isAddingTabExpanded = !isAddingTabExpanded },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAddingTabExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("add_game_tab_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAddingTabExpanded) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = "إضافة تبويب لعبة",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isAddingTabExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAddingTabExpanded) "إغلاق" else "إضافة لعبة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAddingTabExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // =============================================================
                    // المستطيل لإدخال اسم اللعبة وإنشاء التبويب الجديد
                    // =============================================================
                    AnimatedVisibility(
                        visible = isAddingTabExpanded,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("add_game_tab_box")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Gamepad,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "إنشاء تبويب لعبة جديد",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            isAddingTabExpanded = false
                                            newGameTabInputText = ""
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "إلغاء",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // المستطيل لإدخال اسم اللعبة
                                OutlinedTextField(
                                    value = newGameTabInputText,
                                    onValueChange = { newGameTabInputText = it },
                                    label = { Text("اسم اللعبة") },
                                    placeholder = { Text("مثال: Minecraft, GTA, Warzone, FIFA...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done,
                                        capitalization = KeyboardCapitalization.Words
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            val name = newGameTabInputText.trim()
                                            if (name.isNotBlank()) {
                                                coroutineScope.launch {
                                                    val existing = allTabs.firstOrNull { it.name.equals(name, ignoreCase = true) }
                                                    if (existing != null) {
                                                        selectedGameTag = existing.name
                                                        Toast.makeText(context, "التبويب موجود بالفعل وتم اختياره", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        repository.insertTab(name)
                                                        selectedGameTag = name
                                                        Toast.makeText(context, "تم إنشاء تبويب '$name' بنجاح", Toast.LENGTH_SHORT).show()
                                                    }
                                                    newGameTabInputText = ""
                                                    isAddingTabExpanded = false
                                                }
                                            }
                                        }
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Gamepad,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        if (newGameTabInputText.isNotBlank()) {
                                            IconButton(onClick = { newGameTabInputText = "" }) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(tabInputFocusRequester)
                                        .testTag("new_game_tab_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            isAddingTabExpanded = false
                                            newGameTabInputText = ""
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("إلغاء")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val name = newGameTabInputText.trim()
                                            if (name.isNotBlank()) {
                                                coroutineScope.launch {
                                                    val existing = allTabs.firstOrNull { it.name.equals(name, ignoreCase = true) }
                                                    if (existing != null) {
                                                        selectedGameTag = existing.name
                                                        Toast.makeText(context, "التبويب موجود بالفعل وتم اختياره", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        repository.insertTab(name)
                                                        selectedGameTag = name
                                                        Toast.makeText(context, "تم إنشاء تبويب '$name' بنجاح", Toast.LENGTH_SHORT).show()
                                                    }
                                                    newGameTabInputText = ""
                                                    isAddingTabExpanded = false
                                                }
                                            }
                                        },
                                        enabled = newGameTabInputText.isNotBlank(),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("confirm_add_tab_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إضافة التبويب", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (allTabs.isEmpty()) {
                        // Empty tabs prompt
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "شريط التبويبات فارغ",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "أضف تبويب لعبتك الأولى (مثل Minecraft أو Stardew Valley) لربط الملاحظات والمهام بها فوراً.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { isAddingTabExpanded = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("create_tab_button_empty")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إنشاء تبويب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Horizontal scrollable tabs row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // "الكل" Tab
                            item {
                                val isSelected = selectedGameTag == "الكل"
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .clickable { selectedGameTag = "الكل" }
                                        .testTag("game_tab_chip_all")
                                ) {
                                    Text(
                                        text = "الكل",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            // Dynamic User Game Tabs
                            items(allTabs, key = { it.id }) { tab ->
                                val isSelected = tab.name == selectedGameTag
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .clickable { selectedGameTag = tab.name }
                                        .testTag("game_tab_chip_${tab.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Gamepad,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        // Delete tab icon button
                                        IconButton(
                                            onClick = { tabToDelete = tab },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "حذف التبويب",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick "+" button at the end of the tabs row
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { isAddingTabExpanded = true }
                                        .testTag("add_tab_chip_inline")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "إضافة لعبة أخرى",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "لعبة جديدة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                // =============================================================
                // Requirement 1: Tab Header & Dedicated Add Note Action inside Tab
                // =============================================================
                if (selectedGameTag != "الكل") {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Gamepad,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "تبويب لعبة: $selectedGameTag",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${filteredNotes.size} ملاحظة / قائمة مهام خاصة بهذه اللعبة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Button inside the tab to immediately bind new note to this game
                                Button(
                                    onClick = { startCreatingNote() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("tab_inner_add_note_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "إضافة ملاحظة جديدة لـ $selectedGameTag",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث في ملاحظاتك ومهامك...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                // Empty State or Notes List
                if (filteredNotes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillParentMaxHeight(0.75f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .testTag("empty_state_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(26.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gamepad,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (selectedGameTag != "الكل")
                                        "لا توجد ملاحظات لـ '$selectedGameTag' بعد"
                                    else if (allNotes.isEmpty())
                                        "مرحباً بك في GameNotes"
                                    else
                                        "لا توجد عناصر مطابقة",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedGameTag != "الكل")
                                        "ابدأ بتسجيل إحداثياتك، خطط اللعب، أو قائمة مهامك لـ '$selectedGameTag'."
                                    else if (allNotes.isEmpty())
                                        "مفكرتك الذكية لتسجيل خطط ألعابك، إحداثياتك، وقوائم مهامك."
                                    else
                                        "جرّب البحث بكلمات أخرى أو اختر تبويباً مختلفاً.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { startCreatingNote() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("empty_state_add_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedGameTag != "الكل")
                                            "إضافة ملاحظة لـ $selectedGameTag"
                                        else
                                            "إضافة ملاحظة أو قائمة مهام"
                                    )
                                }
                            }
                        }
                        } // End of Box
                    }
                } else {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onToggleTodo = { todoId ->
                                coroutineScope.launch {
                                    repository.toggleTodoItem(note.id, todoId)
                                }
                            },
                            onAskGemini = { n ->
                                selectedNoteForGemini = n
                                isGeminiSheetOpen = true
                            },
                            onEdit = { n ->
                                noteToEdit = n
                                activeFullEditorType = if (n.isTodoList) NoteType.TODO_LIST else NoteType.REGULAR
                            },
                            onDelete = { n ->
                                coroutineScope.launch {
                                    repository.softDeleteNote(n.id)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "تم نقل \"${n.title.ifBlank { "الملاحظة" }}\" إلى سلة المهملات",
                                        actionLabel = "تراجع",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        repository.restoreNote(n.id)
                                    }
                                }
                            },
                            onTogglePin = { n ->
                                coroutineScope.launch {
                                    repository.updateNote(n.copy(isPinned = !n.isPinned))
                                }
                            },
                            onPinAsMiniWidget = { n ->
                                if (isOverlayServiceRunning) {
                                    FloatingOverlayService.pinTodoWidget(context, n.id)
                                    Toast.makeText(context, "تم تثبيت قائمة المهام المصغرة فوق الشاشة", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        overlayPermissionLauncher.launch(intent)
                                    } else {
                                        FloatingOverlayService.pinTodoWidget(context, n.id)
                                        Toast.makeText(context, "تم تفعيل القائمة العائمة بنجاح", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOGS & SHEETS
    // =========================================================================

    if (tabToDelete != null) {
        val tab = tabToDelete!!
        Dialog(onDismissRequest = { tabToDelete = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "حذف تبويب اللعبة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "هل أنت متأكد من حذف تبويب '${tab.name}'؟ الملاحظات المرتبطة به لن تُحذف.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { tabToDelete = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.deleteTab(tab)
                                    if (selectedGameTag == tab.name) {
                                        selectedGameTag = allTabs.firstOrNull { it.id != tab.id }?.name ?: "الكل"
                                    }
                                    tabToDelete = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("حذف")
                        }
                    }
                }
            }
        }
    }

    // Requirement 4: Selection Dialog for Note Types
    if (isSelectingNoteType) {
        NoteTypeSelectionDialog(
            onDismiss = { isSelectingNoteType = false },
            onSelectType = { selectedType ->
                isSelectingNoteType = false
                noteToEdit = null
                activeFullEditorType = selectedType
            }
        )
    }

    // Modal Sheet for Gemini Assistant
    if (isGeminiSheetOpen && selectedNoteForGemini != null) {
        val note = selectedNoteForGemini!!
        val notePromptContext = buildString {
            append(note.title)
            if (note.content.isNotBlank()) append("\n").append(note.content)
            val pending = note.todoItems.filter { !it.isDone }
            if (pending.isNotEmpty()) {
                append("\nالمهام المتبقية: ")
                append(pending.joinToString(", ") { it.text })
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                isGeminiSheetOpen = false
                selectedNoteForGemini = null
            },
            sheetState = geminiSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            GeminiChatSheet(
                initialPrompt = notePromptContext,
                noteTitle = note.title,
                gameTag = note.gameTag,
                onClose = {
                    isGeminiSheetOpen = false
                    selectedNoteForGemini = null
                }
            )
        }
    }
}
}
