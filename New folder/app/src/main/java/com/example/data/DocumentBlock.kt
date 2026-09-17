package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class BlockType {
    TEXT,
    IMAGE,
    CHECKLIST
}

data class DocumentBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.TEXT,
    val text: String = "",
    val imageUri: String = "",
    val secondImageUri: String = "", // For side-by-side 2-image block
    val imageAlignment: String = "RIGHT", // "RIGHT" or "LEFT"
    val imageWidthPercent: Float = 1.0f, // 0.35f (small), 0.65f (medium), 1.0f (full width)
    val isChecked: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type.name)
        put("text", text)
        put("imageUri", imageUri)
        put("secondImageUri", secondImageUri)
        put("imageAlignment", imageAlignment)
        put("imageWidthPercent", imageWidthPercent.toDouble())
        put("isChecked", isChecked)
    }

    companion object {
        fun fromJson(obj: JSONObject): DocumentBlock {
            return DocumentBlock(
                id = obj.optString("id", UUID.randomUUID().toString()),
                type = try {
                    BlockType.valueOf(obj.optString("type", BlockType.TEXT.name))
                } catch (e: Exception) {
                    BlockType.TEXT
                },
                text = obj.optString("text", ""),
                imageUri = obj.optString("imageUri", ""),
                secondImageUri = obj.optString("secondImageUri", ""),
                imageAlignment = obj.optString("imageAlignment", "RIGHT"),
                imageWidthPercent = obj.optDouble("imageWidthPercent", 1.0).toFloat(),
                isChecked = obj.optBoolean("isChecked", false)
            )
        }

        fun listToJson(blocks: List<DocumentBlock>): String {
            val array = JSONArray()
            blocks.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun jsonToList(json: String): List<DocumentBlock> {
            if (json.isBlank()) return emptyList()
            val list = mutableListOf<DocumentBlock>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
