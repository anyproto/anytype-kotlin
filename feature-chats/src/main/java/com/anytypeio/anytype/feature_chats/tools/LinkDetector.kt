package com.anytypeio.anytype.feature_chats.tools

import com.anytypeio.anytype.core_models.Block

object LinkDetector {
    
    private val URL_REGEX = Regex(
        "(https?://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
        RegexOption.IGNORE_CASE
    )
    
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        RegexOption.IGNORE_CASE
    )
    
    private val PHONE_REGEX = Regex(
        "(?:\\+?[1-9]\\d{0,2}[\\s.-]?)?(?:\\(?\\d{1,4}\\)?[\\s.-]?)?\\d{1,4}[\\s.-]?\\d{1,4}[\\s.-]?\\d{0,9}",
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
            val matchText = match.value.trim()
            // Filter out invalid phone numbers (too short or just digits without proper formatting)
            if (matchText.length >= 7 && matchText.count { it.isDigit() } >= 7) {
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
                            "https://${link.text}"
                        } else {
                            link.text
                        }
                    }
                    LinkType.EMAIL -> "mailto:${link.text}"
                    LinkType.PHONE -> "tel:${link.text.filter { it.isDigit() || it == '+' }}"
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