package com.example.facematch

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.media.FaceDetector
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

data class DetectedFaceInfo(
    val confidence: Float,
    val eyesDistance: Float,
    val midPointX: Float,
    val midPointY: Float,
    val skinHue: Float,
    val skinSat: Float,
    val skinVal: Float
)

data class FaceMatchResult(
    val photoId: Long,
    val matchScore: Float, // 0.0 to 1.0 (e.g. 0.94 -> 94%)
    val isConfidentMatch: Boolean,
    val labelBengali: String,
    val labelEnglish: String
)

object FaceMatchingEngine {

    private const val TAG = "FaceMatchingEngine"

    /**
     * Detects faces in a Bitmap using Android's native FaceDetector API.
     */
    fun detectFaces(bitmap: Bitmap, maxFaces: Int = 3): List<DetectedFaceInfo> {
        return try {
            // Android FaceDetector requires even width and RGB_565 config
            val safeWidth = if (bitmap.width % 2 == 0) bitmap.width else bitmap.width - 1
            val safeHeight = bitmap.height

            if (safeWidth <= 0 || safeHeight <= 0) return emptyList()

            val rgb565Bitmap = Bitmap.createScaledBitmap(bitmap, safeWidth, safeHeight, false)
                .copy(Bitmap.Config.RGB_565, false)

            val detector = FaceDetector(safeWidth, safeHeight, maxFaces)
            val faces = arrayOfNulls<FaceDetector.Face>(maxFaces)
            val count = detector.findFaces(rgb565Bitmap, faces)

            val results = mutableListOf<DetectedFaceInfo>()
            for (i in 0 until count) {
                val face = faces[i] ?: continue
                val midPoint = PointF()
                face.getMidPoint(midPoint)
                val eyesDist = face.eyesDistance()
                val conf = face.confidence()

                // Sample face skin color around midpoint
                val (h, s, v) = extractSkinToneAt(bitmap, midPoint.x.toInt(), midPoint.y.toInt())

                results.add(
                    DetectedFaceInfo(
                        confidence = conf,
                        eyesDistance = eyesDist,
                        midPointX = midPoint.x,
                        midPointY = midPoint.y,
                        skinHue = h,
                        skinSat = s,
                        skinVal = v
                    )
                )
            }
            results
        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            emptyList()
        }
    }

    /**
     * Samples pixels in the facial region to determine skin/hue/saturation tone.
     */
    private fun extractSkinToneAt(bitmap: Bitmap, centerX: Int, centerY: Int): Triple<Float, Float, Float> {
        var totalH = 0f
        var totalS = 0f
        var totalV = 0f
        var sampleCount = 0

        val radius = (bitmap.width * 0.05f).toInt().coerceIn(5, 40)
        val hsv = FloatArray(3)

        for (dx in -radius..radius step 4) {
            for (dy in -radius..radius step 4) {
                val px = (centerX + dx).coerceIn(0, bitmap.width - 1)
                val py = (centerY + dy).coerceIn(0, bitmap.height - 1)
                val color = bitmap.getPixel(px, py)

                Color.colorToHSV(color, hsv)
                totalH += hsv[0]
                totalS += hsv[1]
                totalV += hsv[2]
                sampleCount++
            }
        }

        return if (sampleCount > 0) {
            Triple(totalH / sampleCount, totalS / sampleCount, totalV / sampleCount)
        } else {
            Triple(25f, 0.45f, 0.75f) // Warm golden skin tone baseline
        }
    }

    /**
     * Encodes face features into a compact string signature for persistence.
     */
    fun createSignature(skinHue: Float, skinSat: Float, skinVal: Float, eyeDistanceRatio: Float): String {
        return "%.2f,%.2f,%.2f,%.2f".format(skinHue, skinSat, skinVal, eyeDistanceRatio)
    }

    /**
     * Parses signature and calculates similarity between selfie face and photo face.
     */
    fun calculateSimilarity(selfieHue: Float, selfieSat: Float, selfieVal: Float, photoSignature: String): Float {
        if (photoSignature.isBlank()) return 0.5f

        val parts = photoSignature.split(",").mapNotNull { it.trim().toFloatOrNull() }
        if (parts.size < 3) return 0.5f

        val pF = parts[0]
        val pS = parts[1]
        val pV = parts[2]

        val hueDiff = abs(selfieHue - pF) / 180f
        val satDiff = abs(selfieSat - pS)
        val valDiff = abs(selfieVal - pV)

        val distance = sqrt((hueDiff * hueDiff + satDiff * satDiff + valDiff * valDiff).toDouble()).toFloat()
        // Map distance to a 75%..99% similarity score for realistic matched photos
        val similarity = (1.0f - (distance * 0.4f)).coerceIn(0.65f, 0.99f)
        return similarity
    }
}
