package com.anytypeio.anytype.feature_chats.tools

import com.anytypeio.anytype.core_models.Block

object LinkDetector {
    
    const val HTTPS_PREFIX = "https://"
    const val MAILTO_PREFIX = "mailto:"
    const val TEL_PREFIX = "tel:"
    const val ANYTYPE_PREFIX = "anytype://"
    const val FILE_PREFIX = "file://"
    
    private val URL_REGEX = Regex(
        "(https?://|www\\.|[a-zA-Z][a-zA-Z0-9+.-]*://)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
        RegexOption.IGNORE_CASE
    )
    
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        RegexOption.IGNORE_CASE
    )
    
    private val PHONE_REGEX = Regex(
        "\\+\\d{1,3}[\\d-]{7,15}",
        RegexOption.IGNORE_CASE
    )
    
    data class DetectedLink(
        val text: String,
        val start: Int,
        val end: Int,
        val type: LinkType
    )
    
    enum class LinkType {
        URL,
        EMAIL,
        PHONE
    }
    
    fun detectLinks(text: String): List<DetectedLink> {
        val links = mutableListOf<DetectedLink>()
        
        // Detect URLs
        URL_REGEX.findAll(text).forEach { match ->
            links.add(
                DetectedLink(
                    text = match.value,
                    start = match.range.first,
                    end = match.range.last + 1,
                    type = LinkType.URL
                )
            )
        }
        
        // Detect emails (only if not already part of a URL)
        EMAIL_REGEX.findAll(text).forEach { match ->
            val isPartOfUrl = links.any { link ->
                link.type == LinkType.URL && 
                match.range.first >= link.start && 
                match.range.last < link.end
            }
            if (!isPartOfUrl) {
                links.add(
                    DetectedLink(
                        text = match.value,
                        start = match.range.first,
                        end = match.range.last + 1,
                        type = LinkType.EMAIL
                    )
                )
            }
        }
        
        // Detect phone numbers (only if not already part of a URL or email)
        PHONE_REGEX.findAll(text).forEach { match ->
            val isPartOfOtherLink = links.any { link ->
                match.range.first >= link.start && match.range.last < link.end
            }
            if (!isPartOfOtherLink) {
                links.add(
                    DetectedLink(
                        text = match.value,
                        start = match.range.first,
                        end = match.range.last + 1,
                        type = LinkType.PHONE
                    )
                )
            }
        }
        
        return links.sortedBy { it.start }
    }
    
    fun addLinkMarksToText(
        text: String,
        existingMarks: List<Block.Content.Text.Mark>
    ): List<Block.Content.Text.Mark> {
        val detectedLinks = detectLinks(text)
        val newMarks = mutableListOf<Block.Content.Text.Mark>()
        
        // Add existing marks
        newMarks.addAll(existingMarks)
        
        // Add new link marks for detected links (only if not already marked)
        detectedLinks.forEach { link ->
            val hasExistingLinkMark = existingMarks.any { mark ->
                mark.type == Block.Content.Text.Mark.Type.LINK &&
                mark.range.first <= link.start &&
                mark.range.last >= link.end
            }
            
            if (!hasExistingLinkMark) {
                val linkUrl = when (link.type) {
                    LinkType.URL -> {
                        if (link.text.startsWith("www.")) {
                            "$HTTPS_PREFIX${link.text}"
                        } else {
                            link.text
                        }
                    }
                    LinkType.EMAIL -> "$MAILTO_PREFIX${link.text}"
                    LinkType.PHONE -> "$TEL_PREFIX${link.text.replace("-", "")}"
                }
                
                newMarks.add(
                    Block.Content.Text.Mark(
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = linkUrl,
                        range = link.start..link.end
                    )
                )
            }
        }
        
        return newMarks
    }
}