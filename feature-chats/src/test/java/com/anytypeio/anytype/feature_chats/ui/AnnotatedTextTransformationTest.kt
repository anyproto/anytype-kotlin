package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AnnotatedTextTransformationTest {

    @Test
    fun `should combine decorations for same range`() {
        // Given: two decorations on the same range [0, 5]
        val spans = listOf(
            ChatBoxSpan.Markup(
                style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                start = 0,
                end = 5,
                type = ChatBoxSpan.Markup.STRIKETHROUGH
            ),
            ChatBoxSpan.Markup(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
                start = 0,
                end = 5,
                type = ChatBoxSpan.Markup.UNDERLINE
            )
        )
        
        val transformation = AnnotatedTextTransformation(spans)
        val input = AnnotatedString("Hello world")
        
        // When: transformation is applied
        val result = transformation.filter(input)
        
        // Then: both decorations should be present on the range
        val spanStyles = result.text.spanStyles
        assertEquals(1, spanStyles.size, "Should have exactly one combined span style")
        
        val appliedStyle = spanStyles.first()
        assertEquals(0, appliedStyle.start)
        assertEquals(5, appliedStyle.end)
        
        // Check that both decorations are present
        assertNotNull(appliedStyle.item.textDecoration)
        val decoration = appliedStyle.item.textDecoration!!
        
        // Both decorations should be combined
        assertEquals(
            TextDecoration.combine(listOf(TextDecoration.LineThrough, TextDecoration.Underline)),
            decoration
        )
    }
    
    @Test
    fun `should handle non-overlapping decorations independently`() {
        // Given: two decorations on different ranges
        val spans = listOf(
            ChatBoxSpan.Markup(
                style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                start = 0,
                end = 5,
                type = ChatBoxSpan.Markup.STRIKETHROUGH
            ),
            ChatBoxSpan.Markup(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
                start = 6,
                end = 11,
                type = ChatBoxSpan.Markup.UNDERLINE
            )
        )
        
        val transformation = AnnotatedTextTransformation(spans)
        val input = AnnotatedString("Hello world")
        
        // When: transformation is applied
        val result = transformation.filter(input)
        
        // Then: should have two separate span styles
        val spanStyles = result.text.spanStyles
        assertEquals(2, spanStyles.size, "Should have two separate span styles")
        
        // Verify first span (strikethrough on "Hello")
        val first = spanStyles.find { it.start == 0 && it.end == 5 }
        assertNotNull(first)
        assertEquals(TextDecoration.LineThrough, first.item.textDecoration)
        
        // Verify second span (underline on "world")
        val second = spanStyles.find { it.start == 6 && it.end == 11 }
        assertNotNull(second)
        assertEquals(TextDecoration.Underline, second.item.textDecoration)
    }
    
    @Test
    fun `should handle mixed decoration and non-decoration on same range`() {
        // Given: bold (no decoration) + strikethrough (decoration) on same range
        val spans = listOf(
            ChatBoxSpan.Markup(
                style = SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                start = 0,
                end = 5,
                type = ChatBoxSpan.Markup.BOLD
            ),
            ChatBoxSpan.Markup(
                style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                start = 0,
                end = 5,
                type = ChatBoxSpan.Markup.STRIKETHROUGH
            )
        )
        
        val transformation = AnnotatedTextTransformation(spans)
        val input = AnnotatedString("Hello world")
        
        // When: transformation is applied
        val result = transformation.filter(input)
        
        // Then: should have one span with bold + one with decoration
        val spanStyles = result.text.spanStyles
        assertEquals(2, spanStyles.size, "Should have two span styles")
        
        // Verify bold is applied
        val boldSpan = spanStyles.find { it.item.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold }
        assertNotNull(boldSpan)
        
        // Verify strikethrough is applied
        val decorationSpan = spanStyles.find { it.item.textDecoration == TextDecoration.LineThrough }
        assertNotNull(decorationSpan)
    }
}
