package com.example.download

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.WeddingPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PhotoDownloadManager {

    private const val TAG = "PhotoDownloadManager"

    /**
     * Saves a photo Bitmap to device external storage / MediaStore gallery.
     */
    suspend fun savePhotoToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String,
        watermarkText: String? = null
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val processedBitmap = if (!watermarkText.isNullOrBlank()) {
                addWatermark(bitmap, watermarkText)
            } else {
                bitmap
            }

            val filename = "Wedding_${System.currentTimeMillis()}_${title.take(15).replace(" ", "_")}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PerfectCapture")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext null

                resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                uri
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "PerfectCapture"
                )
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, filename)
                FileOutputStream(file).use { out ->
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving photo to gallery", e)
            null
        }
    }

    /**
     * Adds an elegant photographer watermark to downloaded image if requested.
     */
    private fun addWatermark(src: Bitmap, watermark: String): Bitmap {
        val result = src.copy(src.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 180
            textSize = (result.height * 0.035f).coerceIn(24f, 72f)
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
        }

        val bounds = Rect()
        paint.getTextBounds(watermark, 0, watermark.length, bounds)
        val x = result.width - bounds.width() - (result.width * 0.05f)
        val y = result.height - (result.height * 0.05f)

        canvas.drawText(watermark, x, y, paint)
        return result
    }

    /**
     * Creates a share intent with photo details and photographer contact.
     */
    fun shareSelectionSummary(
        context: Context,
        coupleNames: String,
        selectedPhotos: List<WeddingPhoto>,
        photographerPhone: String,
        albumType: String,
        clientName: String
    ) {
        val summaryText = buildString {
            append("📸 *পারফেক্ট ক্যাপচার ফটোগ্রাফি - অ্যালবাম সিলেকশন*\n")
            append("------------------------------------\n")
            append("👰 বর ও কনে: $coupleNames\n")
            append("👤 কাস্টমার: $clientName\n")
            append("📖 অ্যালবামের ধরন: $albumType\n")
            append("✨ মোট পছন্দকৃত ছবি: ${selectedPhotos.size} টি\n\n")
            append("📋 সিলেকশন তালিকা ও নোটস:\n")
            selectedPhotos.forEachIndexed { index, photo ->
                append("${index + 1}. [ছবি #${photo.id}] ${photo.caption}")
                if (photo.clientNotes.isNotBlank()) {
                    append(" - নোট: \"${photo.clientNotes}\"")
                }
                append("\n")
            }
            append("\nফটোগ্রাফার যোগাযোগ: $photographerPhone\n")
            append("অ্যালবাম তৈরির জন্য প্রস্তুত!")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summaryText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "সিলেকশন রিপোর্ট পাঠান / Share Selection")
        context.startActivity(shareIntent)
    }
}
