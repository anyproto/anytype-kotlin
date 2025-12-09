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

    /**
     * Checks if the text content needs to be truncated for chat messages.
     * @return true if text content exceeds the maximum chat message length
     */
    fun requiresTruncation(): Boolean = when (this) {
        is Text -> text.length > MAX_CHAT_MESSAGE_LENGTH
        is Mixed -> (text?.length ?: 0) > MAX_CHAT_MESSAGE_LENGTH
        else -> false
    }

    /**
     * Returns the total number of media attachments in this content.
     */
    fun mediaCount(): Int = when (this) {
        is SingleMedia -> 1
        is MultipleMedia -> uris.size
        is Mixed -> mediaUris.size
        else -> 0
    }

    /**
     * Checks if this content has any media attachments.
     */
    fun hasMedia(): Boolean = mediaCount() > 0

    /**
     * Checks if this content requires batching due to attachment limits.
     * @return true if media count exceeds max attachments per message
     */
    fun requiresBatching(): Boolean = mediaCount() > MAX_ATTACHMENTS_PER_MESSAGE

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
