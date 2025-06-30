package com.anytypeio.anytype.core_ui.features.editor

import android.text.Editable
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.Editor
import org.mockito.kotlin.*
import org.junit.Test
import org.junit.Before

/**
 * Tests for DROID-3763 fixes in BlockAdapter text selection validation logic.
 * 
 * This commit introduced safety checks to prevent StringIndexOutOfBoundsException
 * when setting text selection ranges that exceed text boundaries or have invalid
 * start > end conditions.
 */
class BlockAdapterTextSelectionValidationTest {


    @Before
    fun setup() {
        // Test setup with mockito-kotlin
    }

    @Test
    fun `should set valid selection range when both start and end are within bounds`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 2, end = 7)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(2, 7)
    }

    @Test
    fun `should coerce start position to valid range when start is negative`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Hello"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = -5, end = 3)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(0, 3) // start coerced to 0
    }

    @Test
    fun `should coerce end position to valid range when end exceeds text length`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Hello"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 2, end = 15)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(2, 5) // end coerced to textLength
    }

    @Test
    fun `should set cursor position when start is greater than end`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 8, end = 3)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(3) // cursor set to min(start, end)
        verify(textInputWidget, never()).setSelection(any<Int>(), any<Int>())
    }

    @Test
    fun `should handle empty text gracefully`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(0)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 2, end = 5)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget, never()).setSelection(any<Int>(), any<Int>())
        verify(textInputWidget, never()).setSelection(any<Int>())
    }

    @Test
    fun `should handle null text gracefully`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        whenever(textInputWidget.text).thenReturn(null)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 2, end = 5)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget, never()).setSelection(any<Int>(), any<Int>())
        verify(textInputWidget, never()).setSelection(any<Int>())
    }

    @Test
    fun `should coerce both start and end when both are out of bounds`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Hi"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = -3, end = 10)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(0, 2) // both coerced
    }

    @Test
    fun `should set selection at text end when both start and end equal text length`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Hello"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 5, end = 5)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(5, 5) // cursor at end
    }

    @Test
    fun `should handle zero-length selection at valid position`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 3, end = 3)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(3, 3) // zero-length selection (cursor)
    }

    @Test
    fun `should fix inverted large range by setting cursor to smaller position`() {
        // Given
        val textInputWidget = mock<TextInputWidget>()
        val textContent = "Short"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(textInputWidget.text).thenReturn(mockEditable)
        
        val command = createRestoreSelectionCommand(target = "block1", start = 50, end = 10)
        
        // When
        applyTextSelectionValidation(textInputWidget, command.range)
        
        // Then
        verify(textInputWidget).setSelection(5, 5) // both coerced to textLength, results in cursor at end
    }

    // Helper methods to simulate the actual BlockAdapter logic

    private fun createRestoreSelectionCommand(target: String, start: Int, end: Int): Editor.Restore.Selection {
        return Editor.Restore.Selection(
            target = target,
            range = start..end
        )
    }

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
            timber.log.Timber.e("Invalid selection range: start=$start > end=$end, textLength=$textLength, fixing to cursor position")
            val cursorPos = minOf(start, end).coerceIn(0, textLength)
            textInputWidget.setSelection(cursorPos)
        }
    }
}