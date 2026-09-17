package com.example.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ColorOption(val name: String, val color: Color)

object ThemePreferences {
    private const val PREFS_NAME = "gamenotes_theme_prefs"
    private const val KEY_TEXT_COLOR = "key_text_color"
    private const val KEY_BG_COLOR = "key_bg_color"

    // Default: Bright Gamer Emerald text on Deep Gamer Obsidian background
    val DEFAULT_TEXT_COLOR = Color(0xFF10B981)
    val DEFAULT_BG_COLOR = Color(0xFF0B0F17)

    val textColorPresets = listOf(
        ColorOption("زمردي (افتراضي)", Color(0xFF10B981)),
        ColorOption("سماوي نيون", Color(0xFF06B6D4)),
        ColorOption("ذهبي متوهج", Color(0xFFF59E0B)),
        ColorOption("بنفسجي سايبر", Color(0xFFA855F7)),
        ColorOption("أبيض ناصع", Color(0xFFF8FAFC)),
        ColorOption("أزرق ثلجي", Color(0xFF38BDF8)),
        ColorOption("أحمر ناري", Color(0xFFF43F5E)),
        ColorOption("أخضر ليموني", Color(0xFF84CC16)),
        ColorOption("فحمي داكن", Color(0xFF0F172A))
    )

    val bgColorPresets = listOf(
        ColorOption("أوبسيديان (افتراضي)", Color(0xFF0B0F17)),
        ColorOption("أسود نقي AMOLED", Color(0xFF000000)),
        ColorOption("رمادي ليلي", Color(0xFF18181B)),
        ColorOption("أزرق الفضاء", Color(0xFF0F172A)),
        ColorOption("بنفسجي كوني", Color(0xFF1E112A)),
        ColorOption("كحلي عميق", Color(0xFF0A192F)),
        ColorOption("رمادي فاتح (نهاري)", Color(0xFFF1F5F9))
    )

    private val _textColor = MutableStateFlow(DEFAULT_TEXT_COLOR)
    val textColor = _textColor.asStateFlow()

    private val _bgColor = MutableStateFlow(DEFAULT_BG_COLOR)
    val bgColor = _bgColor.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val textInt = prefs.getInt(KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR.toArgb())
        val bgInt = prefs.getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR.toArgb())
        _textColor.value = Color(textInt)
        _bgColor.value = Color(bgInt)
    }

    fun setTextColor(context: Context, color: Color) {
        _textColor.value = color
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TEXT_COLOR, color.toArgb())
            .apply()
    }

    fun setBgColor(context: Context, color: Color) {
        _bgColor.value = color
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_BG_COLOR, color.toArgb())
            .apply()
    }

    fun resetToDefaults(context: Context) {
        setTextColor(context, DEFAULT_TEXT_COLOR)
        setBgColor(context, DEFAULT_BG_COLOR)
    }

    // Helper to calculate readable contrasting content color (for text inside TopBar)
    fun getContrastingContentColor(background: Color): Color {
        val luminance = 0.299 * background.red + 0.587 * background.green + 0.114 * background.blue
        return if (luminance > 0.5) Color(0xFF090D14) else Color(0xFFF8FAFC)
    }
}
