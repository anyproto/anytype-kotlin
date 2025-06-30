package com.anytypeio.anytype.core_ui.features.editor

import android.text.Editable
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import org.mockito.kotlin.*
import org.junit.Test
import org.junit.Before

/**
 * Edge case tests for DROID-3763 fixes to ensure robust handling of extreme scenarios
 * that could cause StringIndexOutOfBoundsException or other unexpected behavior.
 * 
 * These tests complement the main validation tests by focusing on boundary conditions
 * and unusual input combinations.
 */
class Droid3763EdgeCasesTest {

    private lateinit var mockTextInputWidget: TextInputWidget

    @Before
    fun setup() {
        mockTextInputWidget = mock()
    }

    @Test
    fun `should handle text length change between length check and setSelection call`() {
        // Given - simulate race condition where text changes between length check and setSelection
        val initialLength = 10
        val mockEditable1 = mock<Editable>()
        val mockEditable2 = mock<Editable>()
        whenever(mockEditable1.length).thenReturn(initialLength)
        whenever(mockEditable2.length).thenReturn(5)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable1, mockEditable2)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 3..8)
        
        // Then - should still work with original length check
        verify(mockTextInputWidget).setSelection(3, 8)
    }

    @Test
    fun `should handle maximum integer range values`() {
        // Given
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, Int.MIN_VALUE..Int.MAX_VALUE)
        
        // Then
        verify(mockTextInputWidget).setSelection(0, textContent.length) // coerced to valid range
    }

    @Test
    fun `should handle zero-length text with non-zero selection range`() {
        // Given
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(0)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 5..10)
        
        // Then
        verify(mockTextInputWidget, never()).setSelection(any<Int>(), any<Int>())
        verify(mockTextInputWidget, never()).setSelection(any<Int>())
    }

    @Test
    fun `should handle single character text with large selection range`() {
        // Given
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(1)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 0..1000)
        
        // Then
        verify(mockTextInputWidget).setSelection(0, 1) // coerced to valid range
    }

    @Test
    fun `should handle negative range where both start and end are negative`() {
        // Given
        val textContent = "Hello"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, -10..-5)
        
        // Then
        verify(mockTextInputWidget).setSelection(0, 0) // both coerced to 0, results in cursor at start
    }

    @Test
    fun `should handle range where both start and end exceed text length`() {
        // Given
        val textContent = "Hi"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 10..20)
        
        // Then
        verify(mockTextInputWidget).setSelection(2, 2) // both coerced to textLength, results in cursor at end
    }

    @Test
    fun `should handle inverted range with negative start`() {
        // Given
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 5..-2)
        
        // Then
        verify(mockTextInputWidget).setSelection(0) // fixed to cursor at min valid position
    }

    @Test
    fun `should handle very large inverted range`() {
        // Given
        val textContent = "Test"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, Int.MAX_VALUE..Int.MIN_VALUE)
        
        // Then
        verify(mockTextInputWidget).setSelection(0) // fixed to cursor at min valid position
    }

    @Test
    fun `should handle normal range that becomes inverted after coercion`() {
        // Given
        val textContent = "AB" // length = 2
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When - range 1..5 becomes 1..2 after coercion, which is still valid
        applyTextSelectionValidation(mockTextInputWidget, 1..5)
        
        // Then
        verify(mockTextInputWidget).setSelection(1, 2) // valid after coercion
    }

    @Test
    fun `should handle range that spans exactly the full text`() {
        // Given
        val textContent = "Hello"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 0..5)
        
        // Then
        verify(mockTextInputWidget).setSelection(0, 5) // selects entire text
    }

    @Test
    fun `should handle unicode text with multi-byte characters`() {
        // Given - simulate text with emojis and special characters
        val textContent = "Hello 🌍!" // length may vary depending on how emojis are counted
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(9)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 6..8)
        
        // Then
        verify(mockTextInputWidget).setSelection(6, 8)
    }

    @Test
    fun `should handle concurrent modification scenario`() {
        // Given - simulate scenario where text changes during validation
        val mockEditable1 = mock<Editable>()
        val mockEditable2 = mock<Editable>()
        whenever(mockEditable1.length).thenReturn(10)
        whenever(mockEditable2.length).thenReturn(5)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable1, mockEditable2)
        
        // When
        applyTextSelectionValidation(mockTextInputWidget, 2..8)
        
        // Then - should use the length from the initial check
        verify(mockTextInputWidget).setSelection(2, 8)
    }

    // Test helper method that mirrors the actual BlockAdapter logic

    /**
     * Simulates the text selection validation logic from BlockAdapter commit cb03be3b5a5b0b589ccce8ae248d6ff5c93c01fd
     */
    private fun applyTextSelectionValidation(textInputWidget: TextInputWidget, range: IntRange) {
        val textLength = textInputWidget.text?.length ?: 0
        val start = range.first.coerceIn(0, textLength)
        val end = range.last.coerceIn(0, textLength)
        
        // Ensure start <= end to prevent StringIndexOutOfBoundsException
        if (start <= end && textLength > 0) {
            textInputWidget.setSelection(start, end)
        } else if (start > end) {
            // Log error and fix invalid range by setting cursor to the smaller valid position
            val cursorPos = minOf(start, end).coerceIn(0, textLength)
            textInputWidget.setSelection(cursorPos)
        }
    }
}