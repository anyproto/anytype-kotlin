package com.anytypeio.anytype.presentation.sharing

/**
 * Represents the different types of content that can be shared to Anytype.
 * This sealed class handles all sharing scenarios including text, URLs, media, and mixed content.
 */
sealed class SharedContent {

    /**
     * Plain text content shared from another app.
     * @property text The shared text content
     */
    data class Text(val text: String) : SharedContent()

    /**
     * A URL/link shared from another app.
     * @property url The shared URL
     */
    data class Url(val url: String) : SharedContent()

    /**
     * A single media file (image, video, or generic file).
     * @property uri The content URI of the media file
     * @property type The type of media
     */
    data class SingleMedia(
        val uri: String,
        val type: MediaType
    ) : SharedContent()

    /**
     * Multiple media files of the same type.
     * @property uris List of content URIs for the media files
     * @property type The type of media (all files share the same type)
     */
    data class MultipleMedia(
        val uris: List<String>,
        val type: MediaType
    ) : SharedContent()

    /**
     * Mixed content containing any combination of text, URL, and media.
     * Used when user shares multiple different types of content at once.
     * @property text Optional text content
     * @property url Optional URL content
     * @property mediaUris List of media file URIs
     */
    data class Mixed(
        val text: String? = null,
        val url: String? = null,
        val mediaUris: List<String> = emptyList()
    ) : SharedContent()

    /**
     * Media type classification for shared files.
     */
    enum class MediaType {
        IMAGE,
        VIDEO,
        FILE,
        PDF,
        AUDIO
    }

    companion object {
        /**
         * Maximum character limit for chat messages.
         * Text exceeding this limit will be truncated.
         */
        const val MAX_CHAT_MESSAGE_LENGTH = 2000

        /**
         * Maximum number of attachments allowed per chat message.
         * Media will be batched into multiple messages if this limit is exceeded.
         */
        const val MAX_ATTACHMENTS_PER_MESSAGE = 10
    }
}
