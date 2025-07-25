package com.crosspaste.image.coil

import coil3.Bitmap
import coil3.size.Size
import coil3.transform.Transformation
import com.crosspaste.platform.Platform
import org.jetbrains.skia.IRect
import kotlin.math.roundToInt

class CropTransformation(
    currentPlatform: Platform,
    syncPlatform: Platform?,
) : Transformation() {

    private val cropTop: Float
    private val cropBottom: Float
    private val cropLeft: Float
    private val cropRight: Float

    init {
        if ((syncPlatform ?: currentPlatform).isMacos()) {
            // macOS specific crop values
            cropTop = 0.18f
            cropBottom = 0.18f
            cropLeft = 0f
            cropRight = 0.18f
        } else {
            // Other platforms (Windows, Linux, or unknown)
            cropTop = 0f
            cropBottom = 0f
            cropLeft = 0f
            cropRight = 0.18f
        }
    }

    override val cacheKey: String =
        "CropTransformation($cropTop,$cropBottom,$cropLeft,$cropRight)"

    override suspend fun transform(
        input: Bitmap,
        size: Size,
    ): Bitmap {
        // 1) Clamp the percentages to 0..1 to avoid overflow
        val topPct = cropTop.coerceIn(0f, 1f)
        val bottomPct = cropBottom.coerceIn(0f, 1f)
        val leftPct = cropLeft.coerceIn(0f, 1f)
        val rightPct = cropRight.coerceIn(0f, 1f)

        // 2) Calculate pixel coordinates
        val left = (input.width * leftPct).roundToInt()
        val top = (input.height * topPct).roundToInt()
        val right = (input.width - input.width * rightPct).roundToInt()
        val bottom = (input.height - input.height * bottomPct).roundToInt()

        val width = (right - left).coerceAtLeast(1)
        val height = (bottom - top).coerceAtLeast(1)

        // If the dimensions haven't changed, return the original bitmap
        if (width == input.width && height == input.height) return input

        val subsetRect = IRect.Companion.makeXYWH(left, top, width, height)

        // 3) Try extractSubset first (zero-copy when possible)
        val result = coil3.Bitmap()
        val ok = input.extractSubset(result, subsetRect)
        if (ok) return result

        // 4) Fallback: if extractSubset fails (rare), copy the pixels manually
        val copy = coil3.Bitmap()
        copy.allocN32Pixels(width, height)
        input.readPixels(srcX = left, srcY = top)
        return copy
    }
}
