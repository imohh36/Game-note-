import re

with open('app/src/main/java/com/example/ai/GeminiRepository.kt', 'r') as f:
    content = f.read()

target = """            val request = GenerateContentRequest(
                contents = contentList,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            
            val selectedModel = com.example.data.AppSettingsPreferences.aiModel.value.apiModelName
            val response = try {
                GeminiClient.service.generateContent(selectedModel, apiKey, request)
            } catch (e: Exception) {
                // Fallback to flash if pro fails or unavailable
                GeminiClient.service.generateContent("gemini-2.5-flash", apiKey, request)
            }
            
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text"""

replacement = """            val request = GenerateContentRequest(
                contents = contentList,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            
            val response = GeminiClient.service.generateContent("gemini-2.5-flash", apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text"""
content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ai/GeminiRepository.kt', 'w') as f:
    f.write(content)
print("Done")
