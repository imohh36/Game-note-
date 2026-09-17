package com.example.ai

import com.example.data.AiModelOption
import com.example.data.AppSettingsPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {
    private val systemPrompt = """
        أنت المساعد الذكي المخصص للاعبين في تطبيق GameNotes.
        تخصصك هو مساعدة اللاعبين في ألعاب الفيديو مثل:
        - Stardew Valley (مواعيد الزراعة والمواسم، هدايا القرويين، مهام المجتمع Community Center، وصفات الطبخ، الدفيئة).
        - Minecraft (وصفات الكرافتينج، بوابات النذر، أفضل ارتفاعات تعدين الماس Y=-58، التحصينات وتهيئة الإكسير).
        - وألعاب المغامرات وتقمص الأدوار (RPG).
        
        تعليمات الإجابة:
        1. كن موجزاً ودقيقاً ومركزاً على الحل المباشر للاعب حتى لا تشتت انتباهه أثناء اللعب.
        2. استخدم نقاطاً واضحة وجداول مبسطة عند الحاجة.
        3. أجب بنفس لغة سؤال المستخدم (العربية أو الإنجليزية).
    """.trimIndent()

    suspend fun askGemini(
        prompt: String,
        previousChat: List<ChatMessage> = emptyList(),
        modelOption: AiModelOption? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("ميزات الذكاء الاصطناعي مقفلة. يرجى إدخال مفتاح Gemini API الخاص بك في الإعدادات.")
            )
        }

        try {
            val contentList = mutableListOf<Content>()
            
            // Add up to 6 recent messages for conversational context
            val recentMessages = previousChat.takeLast(6)
            for (msg in recentMessages) {
                val role = if (msg.sender == ChatMessage.Sender.USER) "user" else "model"
                contentList.add(Content(parts = listOf(Part(text = msg.text)), role = role))
            }

            // Add current prompt if not already the last turn
            if (contentList.isEmpty() || contentList.last().parts.firstOrNull()?.text != prompt) {
                contentList.add(Content(parts = listOf(Part(text = prompt)), role = "user"))
            }

            val request = GenerateContentRequest(
                contents = contentList,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            val activeOption = modelOption ?: AppSettingsPreferences.aiModel.value
            val primaryModel = activeOption.apiModelName

            val response = try {
                GeminiClient.service.generateContent(primaryModel, apiKey, request)
            } catch (e: Exception) {
                // Fallback attempt with standard model in case of preview version availability
                when (activeOption) {
                    AiModelOption.GEMINI_PRO_3_1 -> {
                        try {
                            GeminiClient.service.generateContent("gemini-2.5-pro", apiKey, request)
                        } catch (e2: Exception) {
                            GeminiClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                        }
                    }
                    AiModelOption.GEMINI_FLASH_LITE_3_8 -> {
                        try {
                            GeminiClient.service.generateContent("gemini-2.5-flash", apiKey, request)
                        } catch (e2: Exception) {
                            GeminiClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                        }
                    }
                    AiModelOption.GEMINI_FLASH_3_5 -> {
                        try {
                            GeminiClient.service.generateContent("gemini-2.5-flash", apiKey, request)
                        } catch (e2: Exception) {
                            throw e
                        }
                    }
                }
            }
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "لم يتم استلام رد من النموذج."

            Result.success(reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
