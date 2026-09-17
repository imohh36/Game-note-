package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object StorageUtils {
    /**
     * Copies an external/temporary content URI to the application's internal filesDir
     * ensuring images persist across device restarts and Scoped Storage revocations.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String {
        return try {
            val scheme = sourceUri.scheme
            // If already pointing to internal file within app directory, keep it
            if (scheme == "file" && sourceUri.path?.contains(context.filesDir.path) == true) {
                return sourceUri.toString()
            }

            val imagesDir = File(context.filesDir, "note_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(imagesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return sourceUri.toString()

            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            Log.e("StorageUtils", "Failed to copy image to internal storage", e)
            sourceUri.toString()
        }
    }

    /**
     * String overload for convenience
     */
    fun saveImageToInternalStorage(context: Context, sourceUriString: String): String {
        if (sourceUriString.isBlank()) return ""
        return try {
            val uri = Uri.parse(sourceUriString)
            saveImageToInternalStorage(context, uri)
        } catch (e: Exception) {
            sourceUriString
        }
    }
}
