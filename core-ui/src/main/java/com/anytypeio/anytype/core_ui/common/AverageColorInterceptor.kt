package com.anytypeio.anytype.core_ui.common

import coil3.Bitmap
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.request.SuccessResult
import coil3.toBitmap

const val AVG_COLOR_EXTRA = "avg_color"

class AverageColorInterceptor(
    private val memoryCache: MemoryCache?,
    private val compute: (Bitmap) -> Int
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): coil3.request.ImageResult {
        // Let Coil load/decode first.
        val result = chain.proceed()

        if (result is SuccessResult) {
            val key = result.memoryCacheKey
            val cache = memoryCache
            if (key != null && cache != null) {
                val existing = cache[key]
                val cached = (existing?.extras?.get(AVG_COLOR_EXTRA) as? Int)

                if (cached == null) {
                    val avg = compute(result.image.toBitmap())
                    val updated = if (existing != null) {
                        existing.copy(extras = existing.extras + (AVG_COLOR_EXTRA to avg))
                    } else {
                        // If the image wasn’t put in mem cache (policy/size), you can opt to add it.
                        MemoryCache.Value(result.image, extras = mapOf(AVG_COLOR_EXTRA to avg))
                    }
                    cache[key] = updated
                }
            }
        }
        return result
    }
}

fun buildImageLoader(context: android.content.Context, compute: (Bitmap) -> Int): ImageLoader {
    // Build the memory cache once and share it with both the loader and interceptor.
    val mem = MemoryCache.Builder().maxSizePercent(context, 0.25).build()

    return ImageLoader.Builder(context)
        .memoryCache { mem }
        .components {
            add(AverageColorInterceptor(memoryCache = mem, compute = compute))
        }
        .build()
}