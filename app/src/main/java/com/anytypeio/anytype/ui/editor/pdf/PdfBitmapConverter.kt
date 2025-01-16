package com.anytypeio.anytype.ui.editor.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfBitmapConverter(
    private val context: Context
) {
    private var renderer: PdfRenderer? = null

    suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            val bitmaps = mutableListOf<Bitmap>()

            renderer?.close()

            context.contentResolver.openFileDescriptor(contentUri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { pdfRenderer ->
                    renderer = pdfRenderer

                    for (index in 0 until pdfRenderer.pageCount) {
                        val page = pdfRenderer.openPage(index)
                        val bitmap = Bitmap.createBitmap(
                            page.width, page.height, Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmaps.add(bitmap)
                    }
                }
            }
            return@withContext bitmaps
        }
    }
}