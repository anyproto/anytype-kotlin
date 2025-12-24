package com.anytypeio.anytype.presentation.sharing

import android.content.Intent
import android.webkit.URLUtil
import com.anytypeio.anytype.core_utils.ext.parseActionSendMultipleUris
import com.anytypeio.anytype.core_utils.ext.parseActionSendUri
import timber.log.Timber

/**
 * Utility object for converting Android share intents to [SharedContent].
 *
 * This extracts the intent parsing logic that was previously in SharingFragment,
 * allowing it to be used from anywhere (ViewModel, Activity, etc.) without
 * requiring a Fragment context.
 *
 * Supports all MIME types: text, images, videos, audio, PDF, and generic files.
 */
object IntentToSharedContentConverter {

    // MIME type constants
    private const val MIME_TEXT_PLAIN = "text/plain"
    private const val MIME_TEXT_PREFIX = "text/"
    private const val MIME_IMAGE_PREFIX = "image/"
    private const val MIME_VIDEO_PREFIX = "video/"
    private const val MIME_AUDIO_PREFIX = "audio/"
    private const val MIME_APPLICATION_PREFIX = "application/"
    private const val MIME_PDF = "application/pdf"

    /**
     * Converts an Android Intent to [SharedContent].
     * Handles all MIME types: text, images, videos, audio, PDF, and files.
     *
     * @param intent The share intent from Android system
     * @return The parsed [SharedContent] representing the shared data
     */
    fun convert(intent: Intent): SharedContent {
        val mimeType = intent.type

        Timber.d("Converting intent to SharedContent. MIME type: $mimeType, action: ${intent.action}")

        return when {
            // No MIME type - try to extract text
            mimeType == null -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                if (URLUtil.isValidUrl(text)) {
                    SharedContent.Url(text)
                } else {
                    SharedContent.Text(text)
                }
            }

            // Text content (plain text, URLs)
            mimeType == MIME_TEXT_PLAIN -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                if (URLUtil.isValidUrl(text)) {
                    SharedContent.Url(text)
                } else {
                    SharedContent.Text(text)
                }
            }

            // Images
            mimeType.startsWith(MIME_IMAGE_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.IMAGE)
            }

            // Videos
            mimeType.startsWith(MIME_VIDEO_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.VIDEO)
            }

            // Audio (music, voice memos, podcasts)
            mimeType.startsWith(MIME_AUDIO_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.AUDIO)
            }

            // PDF specifically
            mimeType == MIME_PDF -> {
                parseMediaIntent(intent, SharedContent.MediaType.PDF)
            }

            // Other application files (zip, doc, etc.)
            mimeType.startsWith(MIME_APPLICATION_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.FILE)
            }

            // Other text types (html, csv, xml) - treat as file
            mimeType.startsWith(MIME_TEXT_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.FILE)
            }

            // Fallback for unknown types
            else -> {
                Timber.w("Unknown MIME type: $mimeType, treating as generic file")
                parseMediaIntent(intent, SharedContent.MediaType.FILE)
            }
        }
    }

    /**
     * Parses media content from an Intent, handling both single and multiple items.
     *
     * @param intent The share intent
     * @param type The media type classification
     * @return [SharedContent.SingleMedia] for single items, [SharedContent.MultipleMedia] for multiple
     */
    private fun parseMediaIntent(intent: Intent, type: SharedContent.MediaType): SharedContent {
        return if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            val uris = intent.parseActionSendMultipleUris()
            if (uris.isNotEmpty()) {
                SharedContent.MultipleMedia(uris = uris, type = type)
            } else {
                Timber.w("No URIs found in ACTION_SEND_MULTIPLE intent")
                SharedContent.Text("")
            }
        } else {
            val uri = intent.parseActionSendUri()
            if (uri != null) {
                SharedContent.SingleMedia(uri = uri, type = type)
            } else {
                // Fallback: try to get text content
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                SharedContent.Text(text)
            }
        }
    }
}
