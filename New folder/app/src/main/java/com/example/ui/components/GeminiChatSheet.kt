package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.collectAsState
import com.example.ai.ChatMessage
import com.example.ai.GeminiRepository
import com.example.data.AiModelOption
import com.example.data.AppSettingsPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiChatSheet(
    initialPrompt: String = "",
    noteTitle: String = "",
    gameTag: String = "Game",
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geminiRepository = remember { GeminiRepository() }
    val currentAiModel by AppSettingsPreferences.aiModel.collectAsState()
    val userGeminiApiKey by AppSettingsPreferences.userGeminiApiKey.collectAsState()
    val isAiEnabled = userGeminiApiKey.isNotBlank()

    // CRITICAL REQUIREMENT:
    // When "Ask Gemini" is clicked, text is loaded into the input box WITHOUT auto-sending!
    // The user can review, edit, or add context before pressing Send themselves.
    var promptInput by remember { mutableStateOf(initialPrompt) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val messages = remember {
        mutableStateListOf<ChatMessage>().apply {
            add(
                ChatMessage(
                    sender = ChatMessage.Sender.GEMINI,
                    text = "مرحباً يا بطل! أنا مساعدك الذكي في ألعابك ($gameTag). تم نقل نص ملاحظتك إلى المربع بالأسفل؛ يمكنك تعديله أو إضافة سؤالك قبل الضغط على إرسال!"
                )
            )
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val trimmed = promptInput.trim()
        if (trimmed.isBlank() || isLoading) return

        val userMessage = ChatMessage(sender = ChatMessage.Sender.USER, text = trimmed)
        messages.add(userMessage)
        val promptToSend = trimmed
        promptInput = ""
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            val result = geminiRepository.askGemini(promptToSend, messages, modelOption = currentAiModel)
            isLoading = false
            result.onSuccess { reply ->
                messages.add(ChatMessage(sender = ChatMessage.Sender.GEMINI, text = reply))
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "حدث خطأ أثناء التواصل مع Gemini"
                messages.add(
                    ChatMessage(
                        sender = ChatMessage.Sender.GEMINI,
                        text = "عذراً، تعذر الحصول على رد: ${err.localizedMessage}"
                    )
                )
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_chat_sheet"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مساعد جيمني",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Interactive Model Switcher Chip (Cycles through all 3 requested models)
                            Surface(
                                onClick = {
                                    val nextModel = when (currentAiModel) {
                                        AiModelOption.GEMINI_FLASH_3_5 -> AiModelOption.GEMINI_FLASH_LITE_3_8
                                        AiModelOption.GEMINI_FLASH_LITE_3_8 -> AiModelOption.GEMINI_PRO_3_1
                                        AiModelOption.GEMINI_PRO_3_1 -> AiModelOption.GEMINI_FLASH_3_5
                                    }
                                    AppSettingsPreferences.setAiModel(context, nextModel)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = when (currentAiModel) {
                                    AiModelOption.GEMINI_PRO_3_1 -> MaterialTheme.colorScheme.tertiaryContainer
                                    AiModelOption.GEMINI_FLASH_LITE_3_8 -> MaterialTheme.colorScheme.secondaryContainer
                                    AiModelOption.GEMINI_FLASH_3_5 -> MaterialTheme.colorScheme.primaryContainer
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (currentAiModel) {
                                        AiModelOption.GEMINI_PRO_3_1 -> MaterialTheme.colorScheme.tertiary
                                        AiModelOption.GEMINI_FLASH_LITE_3_8 -> MaterialTheme.colorScheme.secondary
                                        AiModelOption.GEMINI_FLASH_3_5 -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (currentAiModel) {
                                            AiModelOption.GEMINI_PRO_3_1 -> Icons.Default.Psychology
                                            AiModelOption.GEMINI_FLASH_LITE_3_8 -> Icons.Default.Speed
                                            AiModelOption.GEMINI_FLASH_3_5 -> Icons.Default.Bolt
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = when (currentAiModel) {
                                            AiModelOption.GEMINI_PRO_3_1 -> MaterialTheme.colorScheme.onTertiaryContainer
                                            AiModelOption.GEMINI_FLASH_LITE_3_8 -> MaterialTheme.colorScheme.onSecondaryContainer
                                            AiModelOption.GEMINI_FLASH_3_5 -> MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = currentAiModel.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (currentAiModel) {
                                            AiModelOption.GEMINI_PRO_3_1 -> MaterialTheme.colorScheme.onTertiaryContainer
                                            AiModelOption.GEMINI_FLASH_LITE_3_8 -> MaterialTheme.colorScheme.onSecondaryContainer
                                            AiModelOption.GEMINI_FLASH_3_5 -> MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                }
                            }
                        }
                        if (noteTitle.isNotBlank()) {
                            Text(
                                text = "مرتبط بـ: $noteTitle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_gemini_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick suggestion chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val suggestions = listOf(
                    "كيف أنفذ هذا بأفضل طريقة؟",
                    "أين أجد المواد المطلوبة؟",
                    "ما هي التوقيتات المناسبة؟"
                )
                suggestions.forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clickable {
                            promptInput = if (promptInput.isBlank()) chipText else "$promptInput ($chipText)"
                        }
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(min = 120.dp, max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.sender == ChatMessage.Sender.USER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isUser) 0.85f else 0.92f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (isUser) 14.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 14.dp
                                    )
                                )
                                .background(
                                    if (isUser) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isUser) "أنت" else "جيمني",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                                if (!isUser && msg.text.length > 30) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Gemini Gaming Tip", msg.text)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "تم نسخ الرد!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "نسخ",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "جيمني يحلل خطتك في اللعبة...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isAiEnabled) {
                // Warning / Lock banner when BYOK key is missing
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ميزات الذكاء الاصطناعي مقفلة: يرجى إدخال مفتاح Gemini API الخاص بك في صفحة الإعدادات للمتابعة.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                // Notice badge reminding user: No Auto-send
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 النص منسوخ للمربع بالأسفل، يمكنك قراءته وتعديله ثم الضغط على إرسال بنفسك.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Input Row: Editable input box + Send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { 
                        Text(
                            if (isAiEnabled) "اكتب أو عدل سؤالك لـ جيمني..." else "أدخل مفتاحك في الإعدادات لفتح الذكاء الاصطناعي",
                            fontSize = 13.sp
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gemini_prompt_input"),
                    maxLines = 3,
                    enabled = isAiEnabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendMessage() },
                    enabled = isAiEnabled && promptInput.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAiEnabled && promptInput.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("gemini_send_button")
                ) {
                    Icon(
                        imageVector = if (isAiEnabled) Icons.AutoMirrored.Filled.Send else Icons.Default.Lock,
                        contentDescription = "إرسال إلى جيمني",
                        tint = if (isAiEnabled && promptInput.isNotBlank() && !isLoading) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
