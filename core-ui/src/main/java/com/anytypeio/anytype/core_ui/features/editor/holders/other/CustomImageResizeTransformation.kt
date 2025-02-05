package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest
import timber.log.Timber

class CustomImageResizeTransformation(
    private val maxWidth: Int,
    private val maxHeight: Int
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return try {
            val imageWidth = toTransform.width
            val imageHeight = toTransform.height
            val targetAspectRatio = maxWidth.toFloat() / maxHeight

            when {
                imageWidth > maxWidth && imageHeight > maxHeight -> {
                    val imageAspectRatio = imageWidth.toFloat() / imageHeight

                    if (imageAspectRatio > targetAspectRatio) {
                        val cropWidth = (imageHeight * targetAspectRatio).toInt()
                        val cropStartX = (imageWidth - cropWidth) / 2
                        Bitmap.createBitmap(toTransform, cropStartX, 0, cropWidth, imageHeight)
                    } else {
                        val cropHeight = (imageWidth / targetAspectRatio).toInt()
                        val cropStartY = (imageHeight - cropHeight) / 2
                        Bitmap.createBitmap(toTransform, 0, cropStartY, imageWidth, cropHeight)
                    }
                }
                imageWidth > maxWidth && imageHeight <= maxHeight -> {
                    val scaleFactor = maxWidth.toFloat() / imageWidth
                    val newHeight = (imageHeight * scaleFactor).toInt()
                    Bitmap.createScaledBitmap(toTransform, maxWidth, newHeight, true)
                }
                imageHeight > maxHeight && imageWidth <= maxWidth -> {
                    val cropHeight = (imageWidth / targetAspectRatio).toInt()
                    val cropStartY = (imageHeight - cropHeight) / 2
                    Bitmap.createBitmap(toTransform, 0, cropStartY, imageWidth, cropHeight)
                }
                else -> toTransform
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(
                e,
                "Failed to transform bitmap: Invalid dimensions or parameters provided. Width: ${toTransform.width}, Height: ${toTransform.height}, MaxWidth: $maxWidth, MaxHeight: $maxHeight"
            )
            toTransform
        } catch (e: OutOfMemoryError) {
            Timber.e(
                e,
                "Failed to transform bitmap: Insufficient memory to process the image."
            )
            toTransform
        }
    }

    override fun equals(other: Any?) = other is CustomImageResizeTransformation
    override fun hashCode() = "CustomImageResizeTransformation".hashCode()
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("CustomImageResizeTransformation".toByteArray())
    }
}