package com.anytypeio.anytype.feature_chats.tools

import com.anytypeio.anytype.core_models.Block
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkDetectorTest {

    @Test
    fun `test URL detection with https protocol`() {
        val text = "Check out https://www.example.com for more info"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("https://www.example.com", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
        assertEquals(10, links[0].start)
        assertEquals(33, links[0].end)  // exclusive end index
    }

    @Test
    fun `test URL detection with www prefix`() {
        val text = "Visit www.example.com today"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("www.example.com", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
        assertEquals(6, links[0].start)
        assertEquals(21, links[0].end)
    }

    @Test
    fun `test multiple URL detection`() {
        val text = "Visit https://github.com and www.google.com for resources"
        val links = LinkDetector.detectLinks(text)

        assertEquals(2, links.size)
        assertEquals("https://github.com", links[0].text)
        assertEquals("www.google.com", links[1].text)
    }

    @Test
    fun `test email detection`() {
        val text = "Contact me at john.doe@example.com for details"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("john.doe@example.com", links[0].text)
        assertEquals(LinkDetector.LinkType.EMAIL, links[0].type)
        assertEquals(14, links[0].start)
        assertEquals(34, links[0].end)
    }

    @Test
    fun `test email with plus sign`() {
        val text = "My email is user+tag@example.com"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("user+tag@example.com", links[0].text)
        assertEquals(LinkDetector.LinkType.EMAIL, links[0].type)
    }

    @Test
    fun `test mixed content detection`() {
        val text = "Email john@example.com or visit https://example.com or call +15551234"
        val links = LinkDetector.detectLinks(text)

        assertEquals(3, links.size)

        // Check they're sorted by start position
        assertEquals(LinkDetector.LinkType.EMAIL, links[0].type)
        assertEquals("john@example.com", links[0].text)

        assertEquals(LinkDetector.LinkType.URL, links[1].type)
        assertEquals("https://example.com", links[1].text)

        assertEquals(LinkDetector.LinkType.PHONE, links[2].type)
        assertEquals("+15551234", links[2].text)
    }

    @Test
    fun `test email inside URL is not detected separately`() {
        val text = "Visit https://user@example.com/path"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
        assertEquals("https://user@example.com/path", links[0].text)
    }

    @Test
    fun `test addLinkMarksToText with URL`() {
        val text = "Visit www.example.com today"
        val marks = LinkDetector.addLinkMarksToText(text, emptyList())

        assertEquals(1, marks.size)
        assertEquals(Block.Content.Text.Mark.Type.LINK, marks[0].type)
        assertEquals("https://www.example.com", marks[0].param)
        assertEquals(6..21, marks[0].range)
    }

    @Test
    fun `test addLinkMarksToText with email`() {
        val text = "Email me@example.com"
        val marks = LinkDetector.addLinkMarksToText(text, emptyList())

        assertEquals(1, marks.size)
        assertEquals(Block.Content.Text.Mark.Type.LINK, marks[0].type)
        assertEquals("mailto:me@example.com", marks[0].param)
        assertEquals(6..20, marks[0].range)
    }

    @Test
    fun `test addLinkMarksToText preserves existing marks`() {
        val text = "Visit www.example.com today"
        val existingMark = Block.Content.Text.Mark(
            type = Block.Content.Text.Mark.Type.BOLD,
            range = 0..5
        )

        val marks = LinkDetector.addLinkMarksToText(text, listOf(existingMark))

        assertEquals(2, marks.size)
        assertTrue(marks.contains(existingMark))
        assertTrue(marks.any { it.type == Block.Content.Text.Mark.Type.LINK })
    }

    @Test
    fun `test addLinkMarksToText doesn't duplicate existing link marks`() {
        val text = "Visit www.example.com today"
        val existingLinkMark = Block.Content.Text.Mark(
            type = Block.Content.Text.Mark.Type.LINK,
            param = "https://www.example.com",
            range = 6..21
        )

        val marks = LinkDetector.addLinkMarksToText(text, listOf(existingLinkMark))

        assertEquals(1, marks.size)
        assertEquals(existingLinkMark, marks[0])
    }

    @Test
    fun `test URL with query parameters`() {
        val text = "Search https://google.com/search?q=kotlin&lang=en"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("https://google.com/search?q=kotlin&lang=en", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
    }

    @Test
    fun `test URL with hash fragment`() {
        val text = "Documentation at https://docs.example.com/api#section-1"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("https://docs.example.com/api#section-1", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
    }

    @Test
    fun `test email with subdomain`() {
        val text = "Send to admin@mail.example.com"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("admin@mail.example.com", links[0].text)
        assertEquals(LinkDetector.LinkType.EMAIL, links[0].type)
    }

    @Test
    fun `test invalid email is not detected`() {
        val text = "Not an email: @example.com or example@"
        val links = LinkDetector.detectLinks(text)

        assertEquals(0, links.size)
    }

    @Test
    fun `test URL at beginning of text`() {
        val text = "https://example.com is a great site"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("https://example.com", links[0].text)
        assertEquals(0, links[0].start)
        assertEquals(19, links[0].end)
    }

    @Test
    fun `test URL at end of text`() {
        val text = "Check out this site: https://example.com"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("https://example.com", links[0].text)
        assertEquals(21, links[0].start)
        assertEquals(40, links[0].end)  // exclusive end index
    }

    @Test
    fun `test consecutive URLs are detected separately`() {
        val text = "https://first.com https://second.com"
        val links = LinkDetector.detectLinks(text)

        assertEquals(2, links.size)
        assertEquals("https://first.com", links[0].text)
        assertEquals("https://second.com", links[1].text)
    }

    @Test
    fun `test empty text returns empty list`() {
        val links = LinkDetector.detectLinks("")
        assertEquals(0, links.size)

        val marks = LinkDetector.addLinkMarksToText("", emptyList())
        assertEquals(0, marks.size)
    }

    @Test
    fun `test text with no links returns empty list`() {
        val text = "This is just plain text with no links"
        val links = LinkDetector.detectLinks(text)
        assertEquals(0, links.size)
    }

    @Test
    fun `test URL with port number`() {
        val text = "Server at https://localhost:8080/api"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("https://localhost:8080/api", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
    }

    @Test
    fun `test complex URL with authentication`() {
        val text = "Access https://user:pass@example.com/secure"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("https://user:pass@example.com/secure", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
    }

    @Test
    fun `test partial overlapping ranges are handled correctly`() {
        val text = "Email admin@example.com via https://example.com/contact"
        val links = LinkDetector.detectLinks(text)

        assertEquals(2, links.size)
        assertEquals(LinkDetector.LinkType.EMAIL, links[0].type)
        assertEquals("admin@example.com", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[1].type)
        assertEquals("https://example.com/contact", links[1].text)
    }

    @Test
    fun `test detect multiple types in order`() {
        val text = "Start https://first.com then email@test.com finally +15551234"
        val links = LinkDetector.detectLinks(text)

        assertEquals(3, links.size)
        // Should be ordered by start position
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
        assertEquals(6, links[0].start)

        assertEquals(LinkDetector.LinkType.EMAIL, links[1].type)
        assertTrue(links[1].start > links[0].end)

        assertEquals(LinkDetector.LinkType.PHONE, links[2].type)
        assertEquals("+15551234", links[2].text)
        assertTrue(links[2].start > links[1].end)
    }

    @Test
    fun `test addLinkMarksToText with existing conflicting link mark prevents duplication`() {
        val text = "Visit www.example.com"
        val existingMark = Block.Content.Text.Mark(
            type = Block.Content.Text.Mark.Type.LINK,
            param = "https://different.com",
            range = 6..21  // Same range as detected link
        )

        val marks = LinkDetector.addLinkMarksToText(text, listOf(existingMark))

        // Should keep existing mark and not add new one
        assertEquals(1, marks.size)
        assertEquals(existingMark, marks[0])
        assertEquals("https://different.com", marks[0].param)
    }

    //region Phone number tests
    @Test
    fun `test phone number detection with international format`() {
        val text = "Call me at +15551234567"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("+15551234567", links[0].text)
        assertEquals(LinkDetector.LinkType.PHONE, links[0].type)
    }

    @Test
    fun `test phone number detection with dashes`() {
        val text = "Call me at +1-555-123-4567"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("+1-555-123-4567", links[0].text)
        assertEquals(LinkDetector.LinkType.PHONE, links[0].type)
    }

    @Test
    fun `test phone numbers without plus are ignored`() {
        val text = "Code is 12345 or call 5551234567"
        val links = LinkDetector.detectLinks(text)

        assertEquals(0, links.size)
    }

    @Test
    fun `test addLinkMarksToText with phone`() {
        val text = "Call +15551234567"
        val marks = LinkDetector.addLinkMarksToText(text, emptyList())

        assertEquals(1, marks.size)
        assertEquals(Block.Content.Text.Mark.Type.LINK, marks[0].type)
        assertEquals("tel:+15551234567", marks[0].param)
    }

    @Test
    fun `test addLinkMarksToText with phone removes dashes`() {
        val text = "Call +1-555-123-4567"
        val marks = LinkDetector.addLinkMarksToText(text, emptyList())

        assertEquals(1, marks.size)
        assertEquals(Block.Content.Text.Mark.Type.LINK, marks[0].type)
        assertEquals("tel:+15551234567", marks[0].param)
    }

    @Test
    fun `test international phone with country code`() {
        val text = "UK number: +442071234567"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("+442071234567", links[0].text)
        assertEquals(LinkDetector.LinkType.PHONE, links[0].type)
    }

    @Test
    fun `test phone number must have plus prefix`() {
        val text = "No plus: 1234567890"
        val links = LinkDetector.detectLinks(text)

        assertEquals(0, links.size)
    }

    @Test
    fun `test phone with different country code`() {
        val text = "International: +33142868326"
        val links = LinkDetector.detectLinks(text)

        assertEquals(1, links.size)
        assertEquals("+33142868326", links[0].text)
        assertEquals(LinkDetector.LinkType.PHONE, links[0].type)
    }

    @Test
    fun `test phone number formats tel link correctly`() {
        val text = "Call +1-555-123-4567 or +18005551234"
        val marks = LinkDetector.addLinkMarksToText(text, emptyList())

        assertEquals(2, marks.size)

        // First phone number (with dashes, should remove them in tel link)
        assertEquals("tel:+15551234567", marks[0].param)

        // Second phone number (no dashes)
        assertEquals("tel:+18005551234", marks[1].param)
    }

    @Test
    fun `test custom scheme detection for anytype`() {
        val text = "Visit anytype://object/123"
        val links = LinkDetector.detectLinks(text)
        
        // Custom schemes should be detected as URLs
        assertEquals(1, links.size)
        assertEquals("anytype://object/123", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
    }

    @Test
    fun `test file path detection`() {
        val text = "Open file:///path/to/file.txt"
        val links = LinkDetector.detectLinks(text)
        
        // File paths should be detected as URLs
        assertEquals(1, links.size)
        assertEquals("file:///path/to/file.txt", links[0].text)
        assertEquals(LinkDetector.LinkType.URL, links[0].type)
    }
    //endregion
}