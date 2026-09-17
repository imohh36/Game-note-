package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FloatingTaskDisplayMode(
    val label: String,
    val description: String
) {
    INLINE(
        label = "مدمجة بالنص (Inline)",
        description = "تظهر المهام مدمجة بتسلسل الملاحظة الطبيعي مع النصوص والصور"
    ),
    SEPARATED_BOTTOM(
        label = "مفصولة بأسفل النافذة (Separated)",
        description = "تثبيت قائمة المهام في قسم مخصص بأسفل النافذة العائمة لسهولة الوصول السريع"
    )
}

enum class AiModelOption(
    val modelId: String,
    val displayName: String,
    val apiModelName: String,
    val description: String,
    val badge: String
) {
    GEMINI_FLASH_3_5(
        modelId = "gemini-flash-3.5",
        displayName = "Gemini Flash 3.5",
        apiModelName = "gemini-3.5-flash",
        description = "استهلاك خفيف وسريع.",
        badge = "خفيف وسريع"
    ),
    GEMINI_FLASH_LITE_3_8(
        modelId = "gemini-flash-lite-3.8",
        displayName = "Gemini Flash Lite 3.8",
        apiModelName = "gemini-3.1-flash-lite-preview",
        description = "استهلاك متوسط.",
        badge = "استهلاك متوسط"
    ),
    GEMINI_PRO_3_1(
        modelId = "gemini-pro-3.1",
        displayName = "Gemini Pro 3.1",
        apiModelName = "gemini-3.1-pro-preview",
        description = "استهلاك عالي.",
        badge = "استهلاك عالي"
    );

    val id: String get() = modelId

    companion object {
        val FLASH get() = GEMINI_FLASH_3_5
        val FLASH_LITE get() = GEMINI_FLASH_LITE_3_8
        val PRO get() = GEMINI_PRO_3_1

        fun fromString(name: String?): AiModelOption {
            if (name == null) return GEMINI_FLASH_3_5
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.modelId.equals(name, ignoreCase = true) }
                ?: if (name.contains("pro", ignoreCase = true)) GEMINI_PRO_3_1
                else if (name.contains("lite", ignoreCase = true)) GEMINI_FLASH_LITE_3_8
                else GEMINI_FLASH_3_5
        }
    }
}

object AppSettingsPreferences {
    private const val PREFS_NAME = "gamenotes_app_settings"
    private const val KEY_FLOATING_TASK_MODE = "key_floating_task_mode"
    private const val KEY_AI_MODEL = "key_ai_model"
    private const val KEY_MINI_TASK_LIST_ENABLED = "key_mini_task_list_enabled"
    private const val KEY_USER_GEMINI_API_KEY = "key_user_gemini_api_key"

    private val _floatingTaskMode = MutableStateFlow(FloatingTaskDisplayMode.INLINE)
    val floatingTaskMode = _floatingTaskMode.asStateFlow()

    private val _aiModel = MutableStateFlow(AiModelOption.GEMINI_FLASH_3_5)
    val aiModel = _aiModel.asStateFlow()

    private val _isMiniTaskListEnabled = MutableStateFlow(true)
    val isMiniTaskListEnabled = _isMiniTaskListEnabled.asStateFlow()

    private val _userGeminiApiKey = MutableStateFlow("")
    val userGeminiApiKey = _userGeminiApiKey.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val taskModeName = prefs.getString(KEY_FLOATING_TASK_MODE, FloatingTaskDisplayMode.INLINE.name)
        val aiModelName = prefs.getString(KEY_AI_MODEL, AiModelOption.GEMINI_FLASH_3_5.name)
        val miniEnabled = prefs.getBoolean(KEY_MINI_TASK_LIST_ENABLED, true)
        val userApiKey = prefs.getString(KEY_USER_GEMINI_API_KEY, "") ?: ""

        _floatingTaskMode.value = try {
            FloatingTaskDisplayMode.valueOf(taskModeName ?: FloatingTaskDisplayMode.INLINE.name)
        } catch (e: Exception) {
            FloatingTaskDisplayMode.INLINE
        }

        _aiModel.value = AiModelOption.fromString(aiModelName)
        _isMiniTaskListEnabled.value = miniEnabled
        _userGeminiApiKey.value = userApiKey.trim()
    }

    fun isMiniTaskListEnabled(context: Context): Boolean {
        return _isMiniTaskListEnabled.value
    }

    fun setMiniTaskListEnabled(context: Context, enabled: Boolean) {
        _isMiniTaskListEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MINI_TASK_LIST_ENABLED, enabled)
            .apply()
    }

    fun setFloatingTaskMode(context: Context, mode: FloatingTaskDisplayMode) {
        _floatingTaskMode.value = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FLOATING_TASK_MODE, mode.name)
            .apply()
    }

    fun setAiModel(context: Context, model: AiModelOption) {
        _aiModel.value = model
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AI_MODEL, model.name)
            .apply()
    }

    fun getUserGeminiApiKey(): String {
        return _userGeminiApiKey.value.trim()
    }

    fun setUserGeminiApiKey(context: Context, key: String) {
        val cleanKey = key.trim()
        _userGeminiApiKey.value = cleanKey
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_GEMINI_API_KEY, cleanKey)
            .apply()
    }

    fun isAiEnabled(): Boolean {
        return _userGeminiApiKey.value.isNotBlank()
    }
}
