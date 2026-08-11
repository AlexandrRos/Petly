package ru.alexandrros.petly.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * Reads and compresses an image from a URI to fit within [maxSizeBytes] bytes.
 * Returns the compressed byte array, or null on failure.
 */
fun readAndCompressImage(context: Context, uri: Uri, maxSizeBytes: Int): ByteArray? {
    return try {
        // First try reading raw bytes
        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        rawBytes?.let { compressBytes(it, maxSizeBytes) }
    } catch (e: Exception) {
        Log.e("ImageUtils", "Failed to read/compress image", e)
        null
    }
}

private fun compressBytes(bytes: ByteArray, maxSizeBytes: Int): ByteArray {
    if (bytes.size <= maxSizeBytes) return bytes
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    val baos = ByteArrayOutputStream()
    // Use a loop to achieve target size
    var quality = 50  // aggressive compression
    do {
        baos.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        quality -= 10
    } while (baos.size() > maxSizeBytes && quality > 10)
    bitmap.recycle()
    return baos.toByteArray()
}