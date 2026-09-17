package com.example.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.compose.AsyncImage
import com.example.GameNotesApp
import com.example.MainActivity
import com.example.R
import com.example.data.AppSettingsPreferences
import com.example.data.BlockType
import com.example.data.DocumentBlock
import com.example.data.FloatingTaskDisplayMode
import com.example.data.GameNote
import com.example.data.GameTab
import com.example.data.NoteType
import com.example.data.TodoItem
import com.example.ui.components.GeminiChatSheet
import com.example.ui.components.NoteCard
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

enum class OverlayMode {
    BUBBLE,
    PANEL,
    MINI_TODO_WIDGET
}

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val overlayLifecycleOwner = OverlayLifecycleOwner()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var bubbleView: ComposeView? = null
    private var panelView: ComposeView? = null
    private var miniWidgetView: ComposeView? = null
    private var dismissZoneView: ComposeView? = null

    private var bubbleParams: WindowManager.LayoutParams? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var miniWidgetParams: WindowManager.LayoutParams? = null
    private var dismissZoneParams: WindowManager.LayoutParams? = null

    private var currentMode = OverlayMode.BUBBLE
    private var activeTodoNoteId: Long? = null
    private var targetPanelNoteId by mutableStateOf<Long?>(null)
    private var isDockedRight = true
    private var snapAnimator: ValueAnimator? = null
    private var miniWidgetWidthPx: Int = 0
    private var miniWidgetHeightPx: Int = 0

    // State for drag-to-dismiss visual indicator
    private val isDismissTargetHighlighted = MutableStateFlow(false)

    companion object {
        const val ACTION_STOP_SERVICE = "com.example.ACTION_STOP_SERVICE"
        const val ACTION_PIN_TODO = "com.example.ACTION_PIN_TODO"
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "gamenotes_overlay_channel"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            try {
                val intent = Intent(context, FloatingOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("FloatingOverlayService", "Failed to start service", e)
            }
        }

        fun pinTodoWidget(context: Context, noteId: Long) {
            try {
                val intent = Intent(context, FloatingOverlayService::class.java).apply {
                    action = ACTION_PIN_TODO
                    putExtra(EXTRA_NOTE_ID, noteId)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("FloatingOverlayService", "Failed to pin todo widget", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, FloatingOverlayService::class.java).apply {
                    action = ACTION_STOP_SERVICE
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("FloatingOverlayService", "Failed to stop service", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            overlayLifecycleOwner.onCreate()

            createNotificationChannel()
            startAsForeground()

            setupDismissZoneView()
            setupBubbleView()
            setupPanelView()
            setupMiniWidgetView()

            serviceScope.launch {
                AppSettingsPreferences.isMiniTaskListEnabled.collect { isMiniEnabled ->
                    if (!isMiniEnabled && currentMode == OverlayMode.MINI_TODO_WIDGET) {
                        activeTodoNoteId?.let { noteId ->
                            targetPanelNoteId = noteId
                            switchToPanel()
                        }
                    }
                }
            }

            _isServiceRunning.value = true
        } catch (e: Exception) {
            Log.e("FloatingOverlayService", "Error during onCreate", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent?.action == ACTION_STOP_SERVICE) {
                stopSelf()
                return START_NOT_STICKY
            }
            if (intent?.action == ACTION_PIN_TODO) {
                val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
                if (noteId != -1L) {
                    if (AppSettingsPreferences.isMiniTaskListEnabled(applicationContext)) {
                        switchToMiniWidget(noteId)
                    } else {
                        targetPanelNoteId = noteId
                        switchToPanel()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FloatingOverlayService", "Error in onStartCommand", e)
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GameNotes Floating Hub",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "أداة عائمة فوق الألعاب لعرض الملاحظات وقوائم المهام"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startAsForeground() {
        try {
            val openAppIntent = Intent(this, MainActivity::class.java)
            val pendingOpenApp = PendingIntent.getActivity(
                this, 0, openAppIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val stopIntent = Intent(this, FloatingOverlayService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            val pendingStop = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GameNotes عائم فوق الألعاب")
                .setContentText("اضغط لفتح الملاحظات أو اسحب للأسفل للإغلاق")
                .setSmallIcon(R.drawable.ic_game_notes_logo)
                .setContentIntent(pendingOpenApp)
                .addAction(R.drawable.ic_game_notes_logo, "إيقاف الأداة العائمة", pendingStop)
                .setOngoing(true)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("FloatingOverlayService", "Error in startAsForeground", e)
        }
    }

    // WindowManager Safe Helpers
    private fun safeAddView(view: View?, params: WindowManager.LayoutParams?) {
        if (view == null || params == null) return
        try {
            if (!view.isAttachedToWindow) {
                windowManager.addView(view, params)
            }
        } catch (e: Exception) {
            Log.w("FloatingOverlayService", "safeAddView failed: ${e.message}")
        }
    }

    private fun safeRemoveView(view: View?) {
        if (view == null) return
        try {
            if (view.isAttachedToWindow) {
                windowManager.removeView(view)
            }
        } catch (e: Exception) {
            Log.w("FloatingOverlayService", "safeRemoveView failed: ${e.message}")
        }
    }

    private fun safeUpdateView(view: View?, params: WindowManager.LayoutParams?) {
        if (view == null || params == null) return
        try {
            if (view.isAttachedToWindow) {
                windowManager.updateViewLayout(view, params)
            }
        } catch (e: Exception) {
            Log.w("FloatingOverlayService", "safeUpdateView failed: ${e.message}")
        }
    }

    // 0. Drag to Dismiss Zone Setup (Visible during dragging, hidden by default)
    private fun setupDismissZoneView() {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        dismissZoneParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (48 * density).toInt()
        }

        dismissZoneView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayLifecycleOwner)
            visibility = View.GONE

            setContent {
                val isHighlighted by isDismissTargetHighlighted.collectAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isHighlighted) 1.25f else 1.0f,
                    label = "dismiss_scale"
                )

                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = if (isHighlighted) Color(0xFFDC2626) else Color(0xDD1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (isHighlighted) Color.White else Color(0xFFEF4444)
                        ),
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "إلغاء",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHighlighted) "أفلت للإلغاء" else "إلغاء",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        safeAddView(dismissZoneView, dismissZoneParams)
    }

    // 1. Standard Floating Bubble with Unified App Icon
    private fun setupBubbleView() {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val sizePx = (62 * density).toInt()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val hideOffset = (14 * density).toInt()
        bubbleParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (displayMetrics.widthPixels - sizePx + hideOffset)
            y = (displayMetrics.heightPixels * 0.35f).toInt()
        }

        bubbleView = ComposeView(this).apply {
            alpha = 0.45f
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayLifecycleOwner)

            setContent {
                MyApplicationTheme {
                    FloatingBubbleContent(
                        hasActiveTodo = activeTodoNoteId != null
                    )
                }
            }

            setOnTouchListener(createBubbleTouchListener())
        }

        safeAddView(bubbleView, bubbleParams)
        currentMode = OverlayMode.BUBBLE
    }

    // Smooth free dragging with Smart Snap to Edge, Partial Hide, and Dynamic Alpha
    private fun createBubbleTouchListener(): View.OnTouchListener {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchDownTime = 0L
        var isMoving = false

        return View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    snapAnimator?.cancel()
                    bubbleView?.alpha = 1.0f
                    initialX = bubbleParams?.x ?: 0
                    initialY = bubbleParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchDownTime = System.currentTimeMillis()
                    isMoving = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    val distance = hypot(dx.toDouble(), dy.toDouble())

                    // Differentiate between tap and drag
                    if (!isMoving && distance > 10) {
                        isMoving = true
                        bubbleView?.alpha = 1.0f
                        dismissZoneView?.visibility = View.VISIBLE
                    }

                    if (isMoving) {
                        val displayMetrics = resources.displayMetrics
                        val density = displayMetrics.density
                        val screenWidth = displayMetrics.widthPixels
                        val screenHeight = displayMetrics.heightPixels

                        bubbleParams?.let { params ->
                            val bubbleW = bubbleView?.width?.takeIf { it > 0 } ?: (62 * density).toInt()
                            val bubbleH = bubbleView?.height?.takeIf { it > 0 } ?: (62 * density).toInt()

                            params.x = (initialX + dx).toInt().coerceIn(- (20 * density).toInt(), screenWidth - bubbleW + (20 * density).toInt())
                            params.y = (initialY + dy).toInt().coerceIn(0, screenHeight - bubbleH)
                            safeUpdateView(bubbleView, params)
                        }

                        // Calculate distance / intersection between bubble and dismiss zone (bottom-center)
                        val dismissCenterX = screenWidth / 2f
                        val dismissCenterY = screenHeight - (75 * density)
                        val distToDismiss = hypot((event.rawX - dismissCenterX).toDouble(), (event.rawY - dismissCenterY).toDouble())

                        val isOverDismiss = distToDismiss < (110 * density) ||
                                (event.rawY > (screenHeight - (160 * density)) && abs(event.rawX - dismissCenterX) < (100 * density))

                        isDismissTargetHighlighted.value = isOverDismiss
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dismissZoneView?.visibility = View.GONE
                    val wasOverDismiss = isDismissTargetHighlighted.value
                    isDismissTargetHighlighted.value = false

                    if (isMoving) {
                        if (wasOverDismiss) {
                            // Dropped over Dismiss area: remove all views and stop service!
                            Toast.makeText(applicationContext, "تم إغلاق الأداة العائمة", Toast.LENGTH_SHORT).show()
                            stopSelf()
                            return@OnTouchListener true
                        }

                        // Smart Snap to Edge with Partial Hide & Dynamic Alpha
                        val displayMetrics = resources.displayMetrics
                        val density = displayMetrics.density
                        val screenWidth = displayMetrics.widthPixels
                        val bubbleW = bubbleView?.width?.takeIf { it > 0 } ?: (62 * density).toInt()

                        val currentX = bubbleParams?.x ?: 0
                        val bubbleCenterX = currentX + bubbleW / 2
                        val screenMidX = screenWidth / 2

                        // Partial hide offset: protrude 14dp into phone frame
                        val hideOffset = (14 * density).toInt()
                        val targetX = if (bubbleCenterX < screenMidX) {
                            -hideOffset
                        } else {
                            screenWidth - bubbleW + hideOffset
                        }

                        snapAnimator?.cancel()
                        snapAnimator = ValueAnimator.ofInt(currentX, targetX).apply {
                            duration = 260
                            interpolator = DecelerateInterpolator()
                            addUpdateListener { animator ->
                                bubbleParams?.let { params ->
                                    params.x = animator.animatedValue as Int
                                    safeUpdateView(bubbleView, params)
                                }
                            }
                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    // Dynamic Alpha: 0.45f when settled on edge
                                    bubbleView?.animate()?.alpha(0.45f)?.setDuration(200)?.start()
                                }
                            })
                            start()
                        }
                    } else {
                        // Regular tap
                        val clickDuration = System.currentTimeMillis() - touchDownTime
                        if (clickDuration < 400) {
                            bubbleView?.alpha = 1.0f
                            if (activeTodoNoteId != null && AppSettingsPreferences.isMiniTaskListEnabled(applicationContext)) {
                                switchToMiniWidget(activeTodoNoteId!!)
                            } else {
                                if (activeTodoNoteId != null) {
                                    targetPanelNoteId = activeTodoNoteId
                                }
                                switchToPanel()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    // 2. Half-Screen Panel
    private fun setupPanelView() {
        val displayMetrics = resources.displayMetrics
        val halfScreenWidth = (displayMetrics.widthPixels * 0.58f).toInt()
            .coerceIn((320 * displayMetrics.density).toInt(), (480 * displayMetrics.density).toInt())

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        panelParams = WindowManager.LayoutParams(
            halfScreenWidth,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or (if (isDockedRight) Gravity.END else Gravity.START)
            x = 0
            y = 0
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        panelView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayLifecycleOwner)

            setContent {
                MyApplicationTheme {
                    FloatingOverlayContent(
                        isDockedRight = isDockedRight,
                        initialViewingNoteId = targetPanelNoteId,
                        onClearInitialViewingNote = { targetPanelNoteId = null },
                        onToggleDockSide = {
                            isDockedRight = !isDockedRight
                            panelParams?.gravity = Gravity.TOP or (if (isDockedRight) Gravity.END else Gravity.START)
                            safeUpdateView(panelView, panelParams)
                        },
                        onCollapse = { switchToBubble() },
                        onSelectTodoList = { todoNoteId ->
                            if (AppSettingsPreferences.isMiniTaskListEnabled(this@FloatingOverlayService)) {
                                switchToMiniWidget(todoNoteId)
                            } else {
                                targetPanelNoteId = todoNoteId
                            }
                        },
                        onSetFocusable = { focusable ->
                            setOverlayFocusable(focusable)
                        },
                        onOpenInFullApp = { noteId ->
                            try {
                                val intent = Intent(this@FloatingOverlayService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("target_note_id", noteId)
                                }
                                startActivity(intent)
                                switchToBubble()
                            } catch (e: Exception) {
                                Log.e("FloatingOverlayService", "Failed to open full app", e)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun setOverlayFocusable(focusable: Boolean) {
        panelParams?.let { params ->
            if (focusable) {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
            } else {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            safeUpdateView(panelView, params)
        }
    }

    // 3. Mini Task-List Widget (Fully draggable anywhere on screen, transparent HUD)
    private fun setupMiniWidgetView() {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        miniWidgetWidthPx = (280 * density).toInt()
        miniWidgetHeightPx = (320 * density).toInt()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        miniWidgetParams = WindowManager.LayoutParams(
            miniWidgetWidthPx,
            miniWidgetHeightPx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (displayMetrics.widthPixels - miniWidgetWidthPx - (14 * density)).toInt().coerceAtLeast(0)
            y = (displayMetrics.heightPixels * 0.18f).toInt()
        }

        miniWidgetView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayLifecycleOwner)
        }
    }

    // Smooth dragging for Mini Task-List Widget anywhere across the screen
    private fun handleMiniWidgetDrag(dx: Float, dy: Float) {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        miniWidgetParams?.let { params ->
            val widgetWidth = params.width.takeIf { it > 0 } ?: miniWidgetWidthPx
            val widgetHeight = params.height.takeIf { it > 0 } ?: miniWidgetHeightPx
            params.x = (params.x + dx).toInt().coerceIn(0, (screenWidth - widgetWidth).coerceAtLeast(0))
            params.y = (params.y + dy).toInt().coerceIn(0, (screenHeight - widgetHeight).coerceAtLeast(0))
            safeUpdateView(miniWidgetView, params)

            val dismissCenterX = screenWidth / 2f
            val dismissCenterY = screenHeight - (75 * density)
            val currentCenterX = params.x + widgetWidth / 2f
            val currentCenterY = params.y + widgetHeight / 2f
            val distToDismiss = hypot((currentCenterX - dismissCenterX).toDouble(), (currentCenterY - dismissCenterY).toDouble())
            val isOverDismiss = distToDismiss < (110 * density) ||
                    (params.y > (screenHeight - 160 * density) && abs(currentCenterX - dismissCenterX) < (100 * density))
            isDismissTargetHighlighted.value = isOverDismiss
        }
    }

    // Dynamic corner and edge resizing for Mini Task-List Widget (Expand on pull, shrink on push)
    private fun handleMiniWidgetResize(isLeft: Boolean, dx: Float, dy: Float) {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val minWidth = (200 * density).toInt()
        val maxWidth = (screenWidth - (16 * density)).toInt().coerceAtLeast(minWidth)
        val minHeight = (180 * density).toInt()
        val maxHeight = (screenHeight * 0.85f).toInt().coerceAtLeast(minHeight)

        miniWidgetParams?.let { params ->
            var currentW = params.width.takeIf { it > 0 } ?: miniWidgetWidthPx
            var currentH = params.height.takeIf { it > 0 } ?: miniWidgetHeightPx
            var currentX = params.x
            var currentY = params.y

            if (!isLeft) {
                // Resizing from the Right corner (drag right to expand, push left to shrink)
                val maxAllowedW = (screenWidth - currentX).coerceAtLeast(minWidth)
                val newW = (currentW + dx).toInt().coerceIn(minWidth, minOf(maxWidth, maxAllowedW))
                currentW = newW
            } else {
                // Resizing from the Left corner (pull left to expand, push right to shrink)
                val maxLeftExtent = currentX + currentW
                val targetW = (currentW - dx).toInt().coerceIn(minWidth, maxLeftExtent.coerceAtLeast(minWidth))
                val actualDx = currentW - targetW
                currentX = (currentX + actualDx).coerceIn(0, (screenWidth - minWidth).coerceAtLeast(0))
                currentW = targetW
            }

            // Height resizing (drag down to expand, push up to shrink)
            val maxAllowedH = (screenHeight - currentY).coerceAtLeast(minHeight)
            val newH = (currentH + dy).toInt().coerceIn(minHeight, minOf(maxHeight, maxAllowedH))
            currentH = newH

            params.x = currentX
            params.y = currentY
            params.width = currentW
            params.height = currentH

            miniWidgetWidthPx = currentW
            miniWidgetHeightPx = currentH

            safeUpdateView(miniWidgetView, params)
        }
    }

    private fun handleMiniWidgetResizeHeight(dy: Float) {
        handleMiniWidgetResize(isLeft = false, dx = 0f, dy = dy)
    }

    private fun handleMiniWidgetDragStart() {
        dismissZoneView?.visibility = View.VISIBLE
    }

    private fun handleMiniWidgetDragEnd() {
        dismissZoneView?.visibility = View.GONE
        val wasOverDismiss = isDismissTargetHighlighted.value
        isDismissTargetHighlighted.value = false
        if (wasOverDismiss) {
            Toast.makeText(applicationContext, "تم إغلاق قائمة المهام العائمة", Toast.LENGTH_SHORT).show()
            switchToBubble()
        }
    }

    // Safe State Transitions
    private fun switchToPanel() {
        try {
            safeRemoveView(miniWidgetView)
            safeRemoveView(bubbleView)
            safeAddView(panelView, panelParams)
            currentMode = OverlayMode.PANEL
        } catch (e: Exception) {
            Log.e("FloatingOverlayService", "switchToPanel failed", e)
        }
    }

    private fun switchToBubble() {
        try {
            safeRemoveView(panelView)
            safeRemoveView(miniWidgetView)
            bubbleView?.alpha = 0.45f
            safeAddView(bubbleView, bubbleParams)
            currentMode = OverlayMode.BUBBLE
        } catch (e: Exception) {
            Log.e("FloatingOverlayService", "switchToBubble failed", e)
        }
    }

    private fun switchToMiniWidget(noteId: Long) {
        if (!AppSettingsPreferences.isMiniTaskListEnabled(applicationContext)) {
            activeTodoNoteId = noteId
            targetPanelNoteId = noteId
            switchToPanel()
            return
        }
        activeTodoNoteId = noteId
        try {
            safeRemoveView(panelView)
            safeRemoveView(bubbleView)

            miniWidgetView?.setContent {
                MyApplicationTheme {
                    FloatingMiniTodoWidgetContent(
                        noteId = noteId,
                        onDragDelta = { dx, dy -> handleMiniWidgetDrag(dx, dy) },
                        onDragStart = { handleMiniWidgetDragStart() },
                        onDragEnd = { handleMiniWidgetDragEnd() },
                        onResizeCorner = { isLeft, dx, dy -> handleMiniWidgetResize(isLeft, dx, dy) },
                        onResizeHeight = { dy -> handleMiniWidgetResizeHeight(dy) },
                        onMinimizeToBubble = { switchToBubble() },
                        onExpandToPanel = {
                            targetPanelNoteId = noteId
                            switchToPanel()
                        },
                        onCloseWidget = {
                            activeTodoNoteId = null
                            switchToBubble()
                        }
                    )
                }
            }

            safeAddView(miniWidgetView, miniWidgetParams)
            currentMode = OverlayMode.MINI_TODO_WIDGET
        } catch (e: Exception) {
            Log.e("FloatingOverlayService", "switchToMiniWidget failed", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        _isServiceRunning.value = false
        try {
            safeRemoveView(bubbleView)
            safeRemoveView(panelView)
            safeRemoveView(miniWidgetView)
            safeRemoveView(dismissZoneView)
        } catch (e: Exception) {
            Log.e("FloatingOverlayService", "Error during onDestroy views cleanup", e)
        }
        overlayLifecycleOwner.onDestroy()
    }
}

// =========================================================================
// COMPOSABLES FOR OVERLAY
// =========================================================================

// 1. Unified Floating Bubble with Official App Vector Icon
@Composable
fun FloatingBubbleContent(
    hasActiveTodo: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(62.dp)
            .padding(3.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0369A1),
                        primaryColor
                    )
                )
            )
            .border(2.dp, Color(0xFF38BDF8), CircleShape)
            .testTag("floating_bubble_icon"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_game_notes_logo),
                contentDescription = "GameNotes Overlay",
                modifier = Modifier.size(34.dp)
            )
            if (hasActiveTodo) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    Text(
                        text = "مهام",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

// 2. Mini Task-List Widget (HUD style, transparent glass on game)
@Composable
fun FloatingMiniTodoWidgetContent(
    noteId: Long,
    onDragDelta: (Float, Float) -> Unit = { _, _ -> },
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onResizeCorner: (Boolean, Float, Float) -> Unit = { _, _, _ -> },
    onResizeHeight: (Float) -> Unit = {},
    onMinimizeToBubble: () -> Unit,
    onExpandToPanel: () -> Unit,
    onCloseWidget: () -> Unit
) {
    val repository = remember { GameNotesApp.instance.repository }
    val coroutineScope = rememberCoroutineScope()
    val note by repository.getNoteById(noteId).collectAsState(initial = null)

    var newQuickTaskText by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mini_todo_widget")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(14.dp, RoundedCornerShape(16.dp))
                .border(1.5.dp, primaryColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF20B0F17),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Drag Handle & Header - Supports Free Dragging Anywhere On Screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { onDragStart() },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDragDelta(dragAmount.x, dragAmount.y)
                                }
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "سحب لنقل القائمة",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = note?.title ?: "قائمة المهام",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Minimize to Bubble
                        IconButton(
                            onClick = onMinimizeToBubble,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "تصغير للأيقونة",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Expand to Full Overlay Panel
                        IconButton(
                            onClick = onExpandToPanel,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "فتح اللوحة كاملة",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Close Widget (Returns to bubble mode)
                        IconButton(
                            onClick = onCloseWidget,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق المصغر",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Progress bar
                val totalTodos = note?.totalTodosCount ?: 0
                val completedTodos = note?.completedTodosCount ?: 0
                val progress = if (totalTodos > 0) completedTodos.toFloat() / totalTodos.toFloat() else 0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primaryColor,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$completedTodos/$totalTodos",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Checklist Items - Unified with in-app task design and wrap content to prevent word clipping
                val currentTodos = remember(note) {
                    if (!note?.todoItems.isNullOrEmpty()) {
                        note!!.todoItems
                    } else if (!note?.blocksJson.isNullOrBlank() && note?.blocksJson != "[]") {
                        try {
                            DocumentBlock.jsonToList(note!!.blocksJson)
                                .filter { it.type == BlockType.CHECKLIST && it.text.isNotBlank() }
                                .map { TodoItem(id = it.id, text = it.text, isDone = it.isChecked) }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }

                if (currentTodos.isEmpty()) {
                    Text(
                        text = "لا توجد مهام حالياً",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(currentTodos, key = { _, it -> it.id }) { index, item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.05f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        note?.let { n ->
                                            coroutineScope.launch {
                                                try {
                                                    repository.toggleTodoItem(n.id, item.id)
                                                } catch (e: Exception) {
                                                    Log.e("MiniTodoWidget", "Error toggling todo", e)
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    // Number badge matching in-app style
                                    Surface(
                                        shape = CircleShape,
                                        color = if (item.isDone) primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}.",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isDone) primaryColor else Color.White.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Checkbox(
                                        checked = item.isDone,
                                        onCheckedChange = {
                                            note?.let { n ->
                                                coroutineScope.launch {
                                                    try {
                                                        repository.toggleTodoItem(n.id, item.id)
                                                    } catch (e: Exception) {
                                                        Log.e("MiniTodoWidget", "Error toggling todo", e)
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(22.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = primaryColor,
                                            uncheckedColor = Color.White.copy(alpha = 0.5f),
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (item.isDone) Color.White.copy(alpha = 0.45f) else Color.White,
                                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .wrapContentHeight(),
                                        softWrap = true,
                                        maxLines = Int.MAX_VALUE
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick task addition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newQuickTaskText,
                        onValueChange = { newQuickTaskText = it },
                        placeholder = { Text("أضف مهمة سريعة...", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (newQuickTaskText.isNotBlank() && note != null) {
                                val updatedTodos = note!!.todoItems + TodoItem(text = newQuickTaskText.trim(), isDone = false)
                                coroutineScope.launch {
                                    try {
                                        repository.updateNote(note!!.copy(todoItems = updatedTodos))
                                        newQuickTaskText = ""
                                    } catch (e: Exception) {
                                        Log.e("MiniTodoWidget", "Error updating note", e)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة مهمة سريعة",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Resize Handle Bar with Corner Grips (سحب لتمديد أو تصغير النافذة)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Corner Resize Grip
                    Box(
                        modifier = Modifier
                            .size(32.dp, 18.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onResizeCorner(true, dragAmount.x, dragAmount.y)
                                    }
                                )
                            },
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Canvas(modifier = Modifier.size(13.dp)) {
                            val strokeWidth = 2.dp.toPx()
                            val c = primaryColor.copy(alpha = 0.85f)
                            drawLine(c, Offset(size.width * 0.7f, size.height), Offset(0f, size.height * 0.3f), strokeWidth)
                            drawLine(c, Offset(size.width * 0.95f, size.height), Offset(0f, size.height * 0.05f), strokeWidth)
                        }
                    }

                    // Center Bottom Resize Pill (Pull down to expand, push up to shrink)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onResizeHeight(dragAmount.y)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }

                    // Right Corner Resize Grip
                    Box(
                        modifier = Modifier
                            .size(32.dp, 18.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onResizeCorner(false, dragAmount.x, dragAmount.y)
                                    }
                                )
                            },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Canvas(modifier = Modifier.size(13.dp)) {
                            val strokeWidth = 2.dp.toPx()
                            val c = primaryColor.copy(alpha = 0.85f)
                            drawLine(c, Offset(size.width * 0.3f, size.height), Offset(size.width, size.height * 0.3f), strokeWidth)
                            drawLine(c, Offset(size.width * 0.05f, size.height), Offset(size.width, size.height * 0.95f), strokeWidth)
                        }
                    }
                }
            }
        }

        // Generous corner touch zones for effortless corner dragging
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(38.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onResizeCorner(true, dragAmount.x, dragAmount.y)
                        }
                    )
                }
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(38.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onResizeCorner(false, dragAmount.x, dragAmount.y)
                        }
                    )
                }
        )
    }
}

// 3. Half-Screen Overlay Panel with In-Place Note Viewer & Creator (Crash-Free, No Dialogs)
@Composable
fun FloatingOverlayContent(
    isDockedRight: Boolean,
    initialViewingNoteId: Long? = null,
    onClearInitialViewingNote: () -> Unit = {},
    onToggleDockSide: () -> Unit,
    onCollapse: () -> Unit,
    onSelectTodoList: (Long) -> Unit,
    onSetFocusable: (Boolean) -> Unit = {},
    onOpenInFullApp: (Long) -> Unit
) {
    val repository = remember { GameNotesApp.instance.repository }
    val coroutineScope = rememberCoroutineScope()

    val allTabs by repository.allTabs.collectAsState(initial = emptyList())
    var selectedGameTag by remember { mutableStateOf("الكل") }
    val notes by repository.getNotesByGame(selectedGameTag).collectAsState(initial = emptyList())

    // In-Place Screens inside the overlay panel (Zero WindowManager crashes!)
    var isCreatingNote by remember { mutableStateOf(false) }
    var creatingNoteType by remember { mutableStateOf(NoteType.REGULAR) }
    var viewingNote by remember { mutableStateOf<GameNote?>(null) }
    var startInEditMode by remember { mutableStateOf(false) }
    var isAddingTabInPlace by remember { mutableStateOf(false) }

    LaunchedEffect(initialViewingNoteId) {
        if (initialViewingNoteId != null && initialViewingNoteId > 0) {
            val note = repository.getNoteByIdDirect(initialViewingNoteId)
            if (note != null) {
                viewingNote = note
                isCreatingNote = false
            }
            onClearInitialViewingNote()
        }
    }

    LaunchedEffect(isCreatingNote) {
        onSetFocusable(isCreatingNote)
    }

    // Gemini Assistant state inside overlay
    var isGeminiActive by remember { mutableStateOf(false) }
    var geminiInitialPrompt by remember { mutableStateOf("") }
    var geminiNoteTitle by remember { mutableStateOf("") }
    var geminiGameTag by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("half_screen_overlay_panel")
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(
                    topStart = if (isDockedRight) 20.dp else 0.dp,
                    bottomStart = if (isDockedRight) 20.dp else 0.dp,
                    topEnd = if (!isDockedRight) 20.dp else 0.dp,
                    bottomEnd = if (!isDockedRight) 20.dp else 0.dp
                )
            ),
        color = Color(0xF60B0F17),
        shape = RoundedCornerShape(
            topStart = if (isDockedRight) 20.dp else 0.dp,
            bottomStart = if (isDockedRight) 20.dp else 0.dp,
            topEnd = if (!isDockedRight) 20.dp else 0.dp,
            bottomEnd = if (!isDockedRight) 20.dp else 0.dp
        ),
        tonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(10.dp)
        ) {
            // Control Bar (Replaced X close button with clear Minimize button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_game_notes_logo),
                        contentDescription = "GameNotes",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GameNotes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Switch Docking Side
                    IconButton(
                        onClick = onToggleDockSide,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isDockedRight) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "نقل الجانب",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Minimize Button (Replaced X button to prevent accidental closure!)
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "تصغير للأيقونة",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Screen 1: Viewing a Note in Half-Screen Overlay
            if (viewingNote != null) {
                OverlayNoteViewer(
                    note = viewingNote!!,
                    initialEditMode = startInEditMode,
                    onBack = { 
                        onSetFocusable(false)
                        startInEditMode = false
                        viewingNote = null 
                    },
                    onSetFocusable = onSetFocusable,
                    onNoteUpdated = { updatedNote ->
                        viewingNote = updatedNote
                    },
                    onToggleTodo = { todoId ->
                        coroutineScope.launch {
                            try {
                                repository.toggleTodoItem(viewingNote!!.id, todoId)
                                // Refresh local copy
                                val updated = repository.getNoteByIdDirect(viewingNote!!.id)
                                if (updated != null) viewingNote = updated
                            } catch (e: Exception) {
                                Log.e("OverlayContent", "Error toggling todo", e)
                            }
                        }
                    },
                    onPinAsMiniWidget = {
                        onSelectTodoList(viewingNote!!.id)
                    },
                    onAskGemini = { n ->
                        geminiInitialPrompt = buildString {
                            append(n.title)
                            if (n.content.isNotBlank()) append("\n").append(n.content)
                        }
                        geminiNoteTitle = n.title
                        geminiGameTag = n.gameTag
                        isGeminiActive = true
                    },
                    onOpenInFullApp = {
                        onSetFocusable(false)
                        onOpenInFullApp(viewingNote!!.id)
                    }
                )
                return@Column
            }

            // Sub-Screen 2: In-Place Note / Checklist Creator (No dialogs, no crashes)
            if (isCreatingNote) {
                OverlayInPlaceNoteCreator(
                    initialType = creatingNoteType,
                    availableTabs = allTabs.map { it.name },
                    currentSelectedTab = if (selectedGameTag != "الكل") selectedGameTag else "",
                    onCancel = {
                        onSetFocusable(false)
                        isCreatingNote = false
                    },
                    onSave = { newNote ->
                        coroutineScope.launch {
                            try {
                                val id = repository.insertNote(newNote)
                                onSetFocusable(false)
                                isCreatingNote = false
                                if (newNote.isTodoList && id > 0) {
                                    onSelectTodoList(id)
                                }
                            } catch (e: Exception) {
                                Log.e("OverlayContent", "Failed to insert note", e)
                            }
                        }
                    },
                    onSetFocusable = onSetFocusable
                )
                return@Column
            }

            // Dynamic Game Tabs in Overlay
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الألعاب:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { isAddingTabInPlace = !isAddingTabInPlace },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isAddingTabInPlace) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "إضافة تبويب",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isAddingTabInPlace) {
                var newTabInput by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTabInput,
                        onValueChange = { newTabInput = it },
                        placeholder = { Text("اسم اللعبة...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newTabInput.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        repository.insertTab(newTabInput.trim())
                                        selectedGameTag = newTabInput.trim()
                                        isAddingTabInPlace = false
                                    } catch (e: Exception) {
                                        Log.e("OverlayContent", "Error inserting tab", e)
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("إضافة", fontSize = 12.sp)
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isSelected = selectedGameTag == "الكل"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) primaryColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, primaryColor) else null,
                        modifier = Modifier.clickable { selectedGameTag = "الكل" }
                    ) {
                        Text(
                            text = "الكل",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) primaryColor else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                items(allTabs, key = { it.id }) { tab ->
                    val isSelected = tab.name == selectedGameTag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) primaryColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, primaryColor) else null,
                        modifier = Modifier.clickable { selectedGameTag = tab.name }
                    ) {
                        Text(
                            text = tab.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) primaryColor else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Add Action Buttons (Direct In-Place, no crash)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        creatingNoteType = NoteType.REGULAR
                        isCreatingNote = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("overlay_quick_add_note"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "ملاحظة جديدة", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        creatingNoteType = NoteType.TODO_LIST
                        isCreatingNote = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("overlay_quick_add_todo"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "قائمة مهام", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gemini Assistant Sheet inside overlay
            if (isGeminiActive) {
                GeminiChatSheet(
                    initialPrompt = geminiInitialPrompt,
                    noteTitle = geminiNoteTitle,
                    gameTag = geminiGameTag,
                    onClose = { isGeminiActive = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }

            // Notes List: Clicking any note opens the full Half-Screen Note Viewer!
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لا توجد ملاحظات أو مهام حالياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "اضغط الأزرار أعلاه لتدوين أهدافك",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        // NoteCard with click-to-view in half-screen overlay!
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewingNote = note 
                                    startInEditMode = false
                                },
                            color = Color.Transparent
                        ) {
                            NoteCard(
                                note = note,
                                isCompact = true,
                                onToggleTodo = { todoId ->
                                    coroutineScope.launch {
                                        try {
                                            repository.toggleTodoItem(note.id, todoId)
                                        } catch (e: Exception) {
                                            Log.e("OverlayContent", "Error toggling todo", e)
                                        }
                                    }
                                },
                                onPinAsMiniWidget = { n ->
                                    onSelectTodoList(n.id)
                                },
                                onAskGemini = { n ->
                                    val noteContextText = buildString {
                                        append(n.title)
                                        if (n.content.isNotBlank()) append("\n").append(n.content)
                                    }
                                    geminiInitialPrompt = noteContextText
                                    geminiNoteTitle = n.title
                                    geminiGameTag = n.gameTag
                                    isGeminiActive = true
                                },
                                onEdit = { n ->
                                    // Requirement 10: Open in-overlay editor directly without leaving the game
                                    viewingNote = n
                                    startInEditMode = true
                                    onSetFocusable(true)
                                },
                                onDelete = { n ->
                                    coroutineScope.launch {
                                        try {
                                            repository.deleteNote(n)
                                        } catch (e: Exception) {
                                            Log.e("OverlayContent", "Error deleting note", e)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. Sub-Component: Half-Screen Note Viewer & In-Overlay Editor (Zero-Dialog, Direct DB Save)
@Composable
fun OverlayNoteViewer(
    note: GameNote,
    initialEditMode: Boolean = false,
    onBack: () -> Unit,
    onSetFocusable: (Boolean) -> Unit = {},
    onNoteUpdated: (GameNote) -> Unit = {},
    onToggleTodo: (String) -> Unit,
    onPinAsMiniWidget: () -> Unit,
    onAskGemini: (GameNote) -> Unit,
    onOpenInFullApp: () -> Unit
) {
    val repository = remember { GameNotesApp.instance.repository }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val floatingTaskMode by AppSettingsPreferences.floatingTaskMode.collectAsState()

    var isEditing by remember(note.id, initialEditMode) { mutableStateOf(initialEditMode) }
    var editTitle by remember(note.id, isEditing) { mutableStateOf(note.title) }
    var editContent by remember(note.id, isEditing) { mutableStateOf(note.content) }
    var editTodos by remember(note.id, isEditing) {
        val initialList = if (note.todoItems.isNotEmpty()) {
            note.todoItems
        } else {
            try {
                DocumentBlock.jsonToList(note.blocksJson)
                    .filter { it.type == BlockType.CHECKLIST && it.text.isNotBlank() }
                    .map { TodoItem(id = it.id, text = it.text, isDone = it.isChecked) }
            } catch (e: Exception) {
                emptyList()
            }
        }
        mutableStateOf(initialList)
    }
    var newTodoText by remember { mutableStateOf("") }

    LaunchedEffect(isEditing) {
        onSetFocusable(isEditing)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("overlay_note_viewer")
    ) {
        // Viewer Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = {
                    if (isEditing) {
                        isEditing = false
                        onSetFocusable(false)
                    } else {
                        onBack()
                    }
                }, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = if (isEditing) "تعديل الملاحظة" else note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.gameTag.isNotBlank() && !isEditing) {
                        Text(
                            text = note.gameTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isEditing) {
                    // Toggle Task Display Mode (Inline vs Separated)
                    IconButton(
                        onClick = {
                            val nextMode = if (floatingTaskMode == FloatingTaskDisplayMode.INLINE)
                                FloatingTaskDisplayMode.SEPARATED_BOTTOM
                            else
                                FloatingTaskDisplayMode.INLINE
                            AppSettingsPreferences.setFloatingTaskMode(context, nextMode)
                            Toast.makeText(context, "طريقة العرض: ${nextMode.label}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "تبديل طريقة عرض المهام",
                            tint = if (floatingTaskMode == FloatingTaskDisplayMode.SEPARATED_BOTTOM) primaryColor else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Delete Note (Soft Delete to Trash)
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.softDeleteNote(note.id)
                                Toast.makeText(context, "تم نقل الملاحظة إلى سلة المهملات", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "نقل إلى سلة المهملات",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Quick Edit In Overlay Button (Opens in-place editor inside overlay)
                    IconButton(
                        onClick = {
                            isEditing = true
                            onSetFocusable(true)
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل في النافذة",
                            tint = primaryColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    if (note.isTodoList || note.todoItems.isNotEmpty() || (note.blocksJson.contains("CHECKLIST"))) {
                        IconButton(
                            onClick = {
                                if (AppSettingsPreferences.isMiniTaskListEnabled(context)) {
                                    onPinAsMiniWidget()
                                } else {
                                    Toast.makeText(context, "قائمة المهام المصغرة معطلة في الإعدادات، يتم العرض بالحجم الطبيعي", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "تثبيت كمصغر",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    IconButton(onClick = { onAskGemini(note) }, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "اسأل جيمني",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(onClick = onOpenInFullApp, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "فتح في المحرر الكامل",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                } else {
                    // Save Button in Edit Mode
                    Button(
                        onClick = {
                            val newBlocks = try {
                                val currentBlocks = DocumentBlock.jsonToList(note.blocksJson).toMutableList()
                                if (currentBlocks.isEmpty()) {
                                    val b = mutableListOf<DocumentBlock>()
                                    if (editContent.isNotBlank()) {
                                        b.add(DocumentBlock(type = BlockType.TEXT, text = editContent))
                                    }
                                    editTodos.forEach { t ->
                                        b.add(DocumentBlock(id = t.id, type = BlockType.CHECKLIST, text = t.text, isChecked = t.isDone))
                                    }
                                    b
                                } else {
                                    val nonChecklist = currentBlocks.filter { it.type != BlockType.CHECKLIST }
                                    val updatedChecklist = editTodos.map { t ->
                                        DocumentBlock(id = t.id, type = BlockType.CHECKLIST, text = t.text, isChecked = t.isDone)
                                    }
                                    val result = nonChecklist.toMutableList()
                                    val textIdx = result.indexOfFirst { it.type == BlockType.TEXT }
                                    if (textIdx >= 0) {
                                        result[textIdx] = result[textIdx].copy(text = editContent)
                                    } else if (editContent.isNotBlank()) {
                                        result.add(0, DocumentBlock(type = BlockType.TEXT, text = editContent))
                                    }
                                    result.addAll(updatedChecklist)
                                    result
                                }
                            } catch (e: Exception) {
                                emptyList()
                            }

                            val updated = note.copy(
                                title = editTitle.ifBlank { "ملاحظة بدون عنوان" },
                                content = editContent,
                                todoItems = editTodos,
                                blocksJson = if (newBlocks.isNotEmpty()) DocumentBlock.listToJson(newBlocks) else note.blocksJson,
                                updatedAt = System.currentTimeMillis()
                            )
                            coroutineScope.launch {
                                try {
                                    repository.updateNote(updated)
                                    onNoteUpdated(updated)
                                    isEditing = false
                                    onSetFocusable(false)
                                } catch (e: Exception) {
                                    Log.e("OverlayNoteViewer", "Failed to update note in overlay", e)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("حفظ", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isEditing) {
            // In-Overlay Editing Form
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it.replace("\n", "").replace("\r", "") },
                        label = { Text("عنوان الملاحظة", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("نص الملاحظة", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                        )
                    )
                }

                if (floatingTaskMode == FloatingTaskDisplayMode.INLINE) {
                    // Inline Tasks in Edit Mode
                    item {
                        Text("قائمة المهام (مدمجة بالنص):", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    items(editTodos, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isDone,
                                    onCheckedChange = { isChecked ->
                                        editTodos = editTodos.map { if (it.id == item.id) it.copy(isDone = isChecked) else it }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = primaryColor),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .wrapContentHeight(),
                                    softWrap = true,
                                    maxLines = Int.MAX_VALUE
                                )
                                IconButton(
                                    onClick = { editTodos = editTodos.filter { it.id != item.id } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "حذف المهمة", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newTodoText,
                                onValueChange = { newTodoText = it },
                                placeholder = { Text("أضف مهمة...", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (newTodoText.isNotBlank()) {
                                        editTodos = editTodos + TodoItem(text = newTodoText.trim(), isDone = false)
                                        newTodoText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryColor)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (floatingTaskMode == FloatingTaskDisplayMode.SEPARATED_BOTTOM) {
                // Separated Bottom Pane for Tasks in Edit Mode
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(max = 210.dp)
                        .padding(top = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Checklist, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("المهام (مفصولة بأسفل النافذة):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            Text("${editTodos.count { it.isDone }}/${editTodos.size}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(editTodos, key = { it.id }) { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.05f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = item.isDone,
                                            onCheckedChange = { isChecked ->
                                                editTodos = editTodos.map { if (it.id == item.id) it.copy(isDone = isChecked) else it }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = primaryColor),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f),
                                            softWrap = true
                                        )
                                        IconButton(
                                            onClick = { editTodos = editTodos.filter { it.id != item.id } },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(13.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newTodoText,
                                onValueChange = { newTodoText = it },
                                placeholder = { Text("أضف مهمة...", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (newTodoText.isNotBlank()) {
                                        editTodos = editTodos + TodoItem(text = newTodoText.trim(), isDone = false)
                                        newTodoText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryColor)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // Standard View Mode: Support inline document blocks or separated bottom pane
            val parsedBlocks = remember(note.blocksJson) {
                if (!note.blocksJson.isNullOrBlank() && note.blocksJson != "[]") {
                    try {
                        DocumentBlock.jsonToList(note.blocksJson)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }

            val isSeparated = floatingTaskMode == FloatingTaskDisplayMode.SEPARATED_BOTTOM

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (parsedBlocks.isNotEmpty()) {
                    val displayBlocks = if (isSeparated) {
                        parsedBlocks.filter { it.type != BlockType.CHECKLIST }
                    } else {
                        parsedBlocks
                    }

                    items(displayBlocks, key = { it.id }) { block ->
                        when (block.type) {
                            BlockType.TEXT -> {
                                if (block.text.isNotBlank()) {
                                    Text(
                                        text = block.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        lineHeight = 22.sp,
                                        softWrap = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            BlockType.IMAGE -> {
                                if (block.imageUri != null) {
                                    AsyncImage(
                                        model = block.imageUri,
                                        contentDescription = "صورة الملاحظة",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }
                            BlockType.CHECKLIST -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleTodo(block.id) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = block.isChecked,
                                        onCheckedChange = { onToggleTodo(block.id) },
                                        modifier = Modifier.size(22.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = primaryColor,
                                            uncheckedColor = Color.White.copy(alpha = 0.5f),
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = block.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (block.isChecked) Color.White.copy(alpha = 0.45f) else Color.White,
                                        textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier
                                            .weight(1f)
                                            .wrapContentHeight(),
                                        softWrap = true,
                                        lineHeight = 22.sp,
                                        maxLines = Int.MAX_VALUE
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Fallback to legacy structure
                    if (note.content.isNotBlank()) {
                        item {
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                lineHeight = 22.sp,
                                softWrap = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (note.imageUris.isNotEmpty()) {
                        items(note.imageUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "صورة الملاحظة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    if (!isSeparated && note.todoItems.isNotEmpty()) {
                        item {
                            val totalCount = note.todoItems.size
                            val completedCount = note.todoItems.count { it.isDone }
                            val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الإنجاز: $completedCount من $totalCount مهام",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${(progressFraction * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = primaryColor,
                                        trackColor = Color.White.copy(alpha = 0.12f)
                                    )
                                }
                            }
                        }

                        itemsIndexed(note.todoItems, key = { _, it -> it.id }) { index, item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleTodo(item.id) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (item.isDone) primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isDone) primaryColor else Color.White.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Checkbox(
                                    checked = item.isDone,
                                    onCheckedChange = { onToggleTodo(item.id) },
                                    modifier = Modifier.size(22.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = primaryColor,
                                        uncheckedColor = Color.White.copy(alpha = 0.5f),
                                        checkmarkColor = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.isDone) Color.White.copy(alpha = 0.45f) else Color.White,
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    modifier = Modifier
                                        .weight(1f)
                                        .wrapContentHeight(),
                                    softWrap = true,
                                    lineHeight = 22.sp,
                                    maxLines = Int.MAX_VALUE
                                )
                            }
                        }
                    }
                }
            }

            if (isSeparated) {
                // Separated Bottom Pane for Checklist Tasks in View Mode
                val checklistBlocks = remember(parsedBlocks) { parsedBlocks.filter { it.type == BlockType.CHECKLIST } }
                val hasTasks = checklistBlocks.isNotEmpty() || note.todoItems.isNotEmpty()
                if (hasTasks) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .heightIn(max = 190.dp)
                            .padding(top = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Checklist, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "المهام (مفصولة بأسفل النافذة)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                                Text(
                                    text = "${note.completedTodosCount}/${note.totalTodosCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (checklistBlocks.isNotEmpty()) {
                                    itemsIndexed(checklistBlocks, key = { _, it -> it.id }) { index, block ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White.copy(alpha = 0.04f),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onToggleTodo(block.id) }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                // Numbered Badge
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (block.isChecked) primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "${index + 1}.",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (block.isChecked) primaryColor else Color.White.copy(alpha = 0.8f),
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(4.dp))

                                                Checkbox(
                                                    checked = block.isChecked,
                                                    onCheckedChange = { onToggleTodo(block.id) },
                                                    modifier = Modifier.size(20.dp),
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = primaryColor,
                                                        uncheckedColor = Color.White.copy(alpha = 0.5f),
                                                        checkmarkColor = Color.Black
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = block.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (block.isChecked) Color.White.copy(alpha = 0.45f) else Color.White,
                                                    textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .wrapContentHeight(),
                                                    softWrap = true
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    itemsIndexed(note.todoItems, key = { _, it -> it.id }) { index, item ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White.copy(alpha = 0.04f),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onToggleTodo(item.id) }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                // Numbered Badge
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (item.isDone) primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "${index + 1}.",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (item.isDone) primaryColor else Color.White.copy(alpha = 0.8f),
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(4.dp))

                                                Checkbox(
                                                    checked = item.isDone,
                                                    onCheckedChange = { onToggleTodo(item.id) },
                                                    modifier = Modifier.size(20.dp),
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = primaryColor,
                                                        uncheckedColor = Color.White.copy(alpha = 0.5f),
                                                        checkmarkColor = Color.Black
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = item.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (item.isDone) Color.White.copy(alpha = 0.45f) else Color.White,
                                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .wrapContentHeight(),
                                                    softWrap = true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Sub-Component: In-Place Note Creator inside Half-Screen Overlay (Safe, Zero-Dialog)
@Composable
fun OverlayInPlaceNoteCreator(
    initialType: NoteType,
    availableTabs: List<String>,
    currentSelectedTab: String,
    onCancel: () -> Unit,
    onSave: (GameNote) -> Unit,
    onSetFocusable: (Boolean) -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(currentSelectedTab.ifBlank { availableTabs.firstOrNull() ?: "" }) }
    val isTodoList = initialType == NoteType.TODO_LIST

    val primaryColor = MaterialTheme.colorScheme.primary

    // For TodoList: exact same list state and auto-numbering/Enter behavior as ChecklistEditorScreen
    val todoItems = remember {
        mutableStateListOf<TodoItem>().apply {
            add(TodoItem(text = "", isDone = false))
        }
    }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }
    var pendingFocusIndex by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        onSetFocusable(true)
    }

    LaunchedEffect(pendingFocusIndex, todoItems.size) {
        pendingFocusIndex?.let { index ->
            delay(50)
            focusRequesters[index]?.requestFocus()
            listState.animateScrollToItem((index + 1).coerceAtMost(todoItems.size))
            pendingFocusIndex = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("overlay_inplace_creator")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isTodoList) Icons.Default.Checklist else Icons.Default.Edit,
                    contentDescription = null,
                    tint = if (isTodoList) Color(0xFF38BDF8) else primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (title.isNotBlank()) title else (if (isTodoList) "قائمة مهام جديدة" else "ملاحظة جديدة"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (selectedTab.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gamepad, contentDescription = null, tint = primaryColor, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = selectedTab, style = MaterialTheme.typography.labelSmall, color = primaryColor, fontSize = 10.sp)
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    onSetFocusable(false)
                    onCancel()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color.White.copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Game Tab Selection chips (matches the app's GameTag chips)
        if (availableTabs.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableTabs) { tab ->
                    val isSelected = tab == selectedTab
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) primaryColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) primaryColor else Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.clickable { selectedTab = tab }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = if (isSelected) primaryColor else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tab,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryColor else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (isTodoList) {
            val totalCount = todoItems.size
            val completedCount = todoItems.count { it.isDone }
            val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

            // 1. Title and Dynamic Progress Card (matches ChecklistEditorScreen)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("floating_checklist_title_card")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "عنوان قائمة المهام:",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.replace("\n", "").replace("\r", "") },
                        placeholder = {
                            Text(
                                "مثال: مهام اليوم 31 ستاردو، أو متطلبات بوابة النذر...",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("floating_checklist_title_input"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    // Dynamic Progress Indicator (matching ChecklistEditorScreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإنجاز: $completedCount من $totalCount مهام",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primaryColor,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Main Checklist Items Card (exact same as ChecklistEditorScreen)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("floating_checklist_items_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "عناصر المهام (اضغط Enter للمهمة التالية):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }

                        // Add Task (+) Button
                        IconButton(
                            onClick = {
                                val newIndex = todoItems.size
                                todoItems.add(TodoItem(text = "", isDone = false))
                                pendingFocusIndex = newIndex
                            },
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة مهمة جديدة",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(todoItems, key = { _, item -> item.id }) { index, item ->
                            val requester = focusRequesters.getOrPut(index) { FocusRequester() }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.04f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Auto-numbering: 1., 2., 3., etc. (as in ChecklistEditorScreen)
                                    Surface(
                                        shape = CircleShape,
                                        color = if (item.isDone) primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}.",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isDone) primaryColor else Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Checkbox
                                    Checkbox(
                                        checked = item.isDone,
                                        onCheckedChange = { isChecked ->
                                            todoItems[index] = item.copy(isDone = isChecked)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = primaryColor,
                                            uncheckedColor = Color.White.copy(alpha = 0.5f),
                                            checkmarkColor = Color.Black
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // In-line Task Text Field with Enter -> Next Item
                                    TextField(
                                        value = item.text,
                                        onValueChange = { newText ->
                                            if (newText.contains("\n")) {
                                                val parts = newText.split("\n", limit = 2)
                                                todoItems[index] = item.copy(text = parts[0])
                                                val insertIndex = index + 1
                                                todoItems.add(insertIndex, TodoItem(text = if (parts.size > 1) parts[1] else "", isDone = false))
                                                pendingFocusIndex = insertIndex
                                            } else {
                                                todoItems[index] = item.copy(text = newText)
                                            }
                                        },
                                        placeholder = {
                                            Text(
                                                text = "اكتب المهمة ${index + 1}...",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.4f)
                                            )
                                        },
                                        textStyle = TextStyle(
                                            fontSize = 13.sp,
                                            color = if (item.isDone) Color.White.copy(alpha = 0.45f) else Color.White,
                                            textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                val insertIndex = index + 1
                                                todoItems.add(insertIndex, TodoItem(text = "", isDone = false))
                                                pendingFocusIndex = insertIndex
                                            }
                                        ),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = primaryColor,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(requester)
                                    )

                                    // Delete task button
                                    IconButton(
                                        onClick = {
                                            if (todoItems.size > 1) {
                                                todoItems.removeAt(index)
                                            } else {
                                                todoItems[0] = TodoItem(text = "", isDone = false)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "حذف المهمة",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Note Title Input
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "عنوان الملاحظة:",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.replace("\n", "").replace("\r", "") },
                        placeholder = { Text("عنوان الملاحظة...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note Content Input
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    Text(
                        text = "محتوى وتفاصيل الملاحظة:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("اكتب تفاصيل الملاحظة هنا...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    onSetFocusable(false)
                    onCancel()
                },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text("إلغاء", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { if (isTodoList) "قائمة مهام جديدة" else "ملاحظة جديدة" }
                    val finalNote = if (isTodoList) {
                        val filteredTodos = todoItems.filter { it.text.isNotBlank() }
                        GameNote(
                            title = finalTitle,
                            content = "",
                            gameTag = selectedTab,
                            noteType = NoteType.TODO_LIST.name,
                            todoItems = filteredTodos
                        )
                    } else {
                        GameNote(
                            title = finalTitle,
                            content = content,
                            gameTag = selectedTab,
                            noteType = NoteType.REGULAR.name
                        )
                    }
                    onSetFocusable(false)
                    onSave(finalNote)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("حفظ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
