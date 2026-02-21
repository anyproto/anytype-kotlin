package com.anytypeio.anytype.feature_os_widgets.persistence

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Caches space icons locally for OS home screen widgets.
 * Images are downloaded when the app is running (and middleware server is available)
 * and stored locally so widgets can display them even when the app is closed.
 */
class OsWidgetIconCache(private val context: Context) {

    private val cacheDir: File by lazy {
        File(context.filesDir, ICONS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Downloads an image from the given URL and caches it locally.
     * @param url The HTTP URL to download from (middleware server URL)
     * @param spaceId The space ID used as filename
     * @return Local file path if successful, null otherwise
     */
    suspend fun cacheIcon(url: String, spaceId: String): String? = withContext(Dispatchers.IO) {
        try {
            Timber.tag(TAG).d("Attempting to cache icon for space $spaceId from URL: $url")
            val file = File(cacheDir, "$spaceId$FILE_EXTENSION")
            Timber.tag(TAG).d("Target file: ${file.absolutePath}")
            
            // Download and decode as bitmap to ensure valid image
            val connection = URL(url).openConnection().apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }
            
            Timber.tag(TAG).d("Opening connection...")
            connection.getInputStream().use { input ->
                Timber.tag(TAG).d("Got input stream, decoding bitmap...")
                val bitmap = BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    Timber.tag(TAG).d("Bitmap decoded: ${bitmap.width}x${bitmap.height}")
                    FileOutputStream(file).use { output ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    }
                    bitmap.recycle()
                    Timber.tag(TAG).d("Successfully cached icon for space $spaceId to ${file.absolutePath}")
                    file.absolutePath
                } else {
                    Timber.tag(TAG).w("Failed to decode bitmap for space $spaceId")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to cache icon for space $spaceId from $url")
            null
        }
    }

    /**
     * Gets the cached icon file path if it exists.
     */
    fun getCachedIconPath(spaceId: String): String? {
        val file = File(cacheDir, "$spaceId$FILE_EXTENSION")
        return if (file.exists()) file.absolutePath else null
    }

    /**
     * Downloads an image from the given URL and caches it for a shortcut widget.
     * @param url The HTTP URL to download from (middleware server URL)
     * @param widgetId The widget ID used as filename (ensures uniqueness per widget)
     * @param prefix A prefix to distinguish space vs object shortcuts
     * @return Local file path if successful, null otherwise
     */
    suspend fun cacheShortcutIcon(url: String, widgetId: Int, prefix: String): String? = withContext(Dispatchers.IO) {
        try {
            val shortcutDir = File(context.filesDir, SHORTCUT_ICONS_DIR).apply {
                if (!exists()) mkdirs()
            }
            val file = File(shortcutDir, "${prefix}_${widgetId}$FILE_EXTENSION")
            
            val connection = URL(url).openConnection().apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }
            
            connection.getInputStream().use { input ->
                val bytes = input.readBytes()
                
                // Check if it's an SVG file
                val isSvg = bytes.size > 4 && String(bytes.take(100).toByteArray()).contains("<svg")
                
                val bitmap = if (isSvg) {
                    decodeSvgToBitmap(bytes, ICON_SIZE)
                } else {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                
                if (bitmap != null) {
                    FileOutputStream(file).use { output ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    }
                    bitmap.recycle()
                    file.absolutePath
                } else {
                    Timber.tag(TAG).w("Failed to decode bitmap for widget $widgetId")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to cache shortcut icon for widget $widgetId")
            null
        }
    }

    /**
     * Removes cached icon for a shortcut widget.
     */
    fun removeShortcutIcon(widgetId: Int, prefix: String) {
        try {
            val shortcutDir = File(context.filesDir, SHORTCUT_ICONS_DIR)
            val file = File(shortcutDir, "${prefix}_${widgetId}$FILE_EXTENSION")
            if (file.exists()) {
                file.delete()
                Timber.tag(TAG).d("Removed shortcut icon for widget $widgetId")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to remove shortcut icon for widget $widgetId")
        }
    }

    /**
     * Clears all cached icons.
     */
    fun clearAll() {
        try {
            cacheDir.listFiles()?.forEach { it.delete() }
            File(context.filesDir, SHORTCUT_ICONS_DIR).listFiles()?.forEach { it.delete() }
            Timber.tag(TAG).d("Cleared all OS widget icon cache")
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to clear OS widget icon cache")
        }
    }

    /**
     * Removes icons for spaces not in the given list.
     */
    fun cleanupStaleIcons(activeSpaceIds: Set<String>) {
        try {
            cacheDir.listFiles()?.forEach { file ->
                val spaceId = file.nameWithoutExtension
                if (spaceId !in activeSpaceIds) {
                    file.delete()
                    Timber.tag(TAG).d("Removed stale icon for space $spaceId")
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to cleanup stale icons")
        }
    }

    /**
     * Decodes SVG bytes to a Bitmap.
     */
    private fun decodeSvgToBitmap(bytes: ByteArray, size: Int): Bitmap? {
        return try {
            val svg = SVG.getFromString(String(bytes))
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Get the intrinsic size of the SVG
            val svgWidth = svg.documentWidth
            val svgHeight = svg.documentHeight
            
            // If SVG has no intrinsic size, use viewBox or default
            val actualWidth = if (svgWidth > 0) svgWidth else svg.documentViewBox?.width() ?: size.toFloat()
            val actualHeight = if (svgHeight > 0) svgHeight else svg.documentViewBox?.height() ?: size.toFloat()
            
            // Calculate scale to fit the target size
            val scale = minOf(size / actualWidth, size / actualHeight)
            
            // Center the SVG in the canvas
            val translateX = (size - actualWidth * scale) / 2f
            val translateY = (size - actualHeight * scale) / 2f
            
            canvas.translate(translateX, translateY)
            canvas.scale(scale, scale)
            
            svg.renderToCanvas(canvas)
            bitmap
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to decode SVG")
            null
        }
    }

    companion object {
        private const val TAG = "OsWidget"
        private const val ICONS_DIR = "os_widget_icons"
        private const val SHORTCUT_ICONS_DIR = "os_widget_shortcut_icons"
        private const val FILE_EXTENSION = ".png"
        private const val TIMEOUT_MS = 5000
        private const val ICON_SIZE = 128 // Size in pixels for cached icons
        
        const val PREFIX_SPACE = "space"
        const val PREFIX_OBJECT = "object"
    }
}
