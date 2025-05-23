package com.anytypeio.anytype.device

import com.anytypeio.anytype.core_models.DecryptedPushContent

/**
 * Formats the notification body text by appending attachment indicator if needed.
 * 
 * @param attachmentText Localized text to indicate presence of attachments
 * @return Formatted body text with optional attachment indicator
 */
fun DecryptedPushContent.Message.formatNotificationBody(attachmentText: String): String {
    val rawText = text.trim()
    return when {
        hasAttachments && rawText.isNotEmpty() ->
            "$rawText \uD83D\uDCCE$attachmentText"
        hasAttachments ->
            "\uD83D\uDCCE$attachmentText"
        else ->
            rawText
    }
} 