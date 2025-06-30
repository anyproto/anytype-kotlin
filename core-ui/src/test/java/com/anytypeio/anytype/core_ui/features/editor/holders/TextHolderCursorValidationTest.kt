package com.anytypeio.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import org.mockito.kotlin.*
import org.junit.Test
import org.junit.Before

/**
 * Tests for DROID-3763 fixes in TextHolder cursor validation logic.
 * 
 * This commit introduced safety checks to prevent invalid cursor positioning
 * that could cause StringIndexOutOfBoundsException when the cursor position
 * exceeds text boundaries.
 */
class TextHolderCursorValidationTest {

    private lateinit var mockTextInputWidget: TextInputWidget
    private lateinit var textHolder: TestTextHolder

    class TestTextHolder(override val content: TextInputWidget) : TextHolder {
        override val root: View = mock()
        override val selectionView: View = mock()
        // Implementation of setCursor from the commit
        override fun setCursor(item: BlockView.Cursor) {
            timber.log.Timber.d("Setting cursor: $item")
            item.cursor?.let { cursor ->
                val length = content.text?.length ?: 0
                val validCursor = cursor.coerceIn(0, length)
                if (length > 0 && validCursor <= length) {
                    content.setSelection(validCursor)
                } else {
                    timber.log.Timber.w("Invalid cursor position: cursor=$cursor, textLength=$length")
                }
            }
        }
    }

    @Before
    fun setup() {
        mockTextInputWidget = mock()
        textHolder = TestTextHolder(mockTextInputWidget)
    }

    @Test
    fun `should set cursor at valid position within text bounds`() {
        // Given
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = 5)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(5)
    }

    @Test
    fun `should coerce negative cursor position to zero`() {
        // Given
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = -3)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(0) // coerced to 0
    }

    @Test
    fun `should coerce cursor position that exceeds text length`() {
        // Given
        val textContent = "Hello"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = 15)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(5) // coerced to textLength
    }

    @Test
    fun `should set cursor at end of text when position equals text length`() {
        // Given
        val textContent = "Hello"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = 5)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(5) // at end of text
    }

    @Test
    fun `should not set cursor when text is empty`() {
        // Given
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(0)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = 3)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget, never()).setSelection(any())
    }

    @Test
    fun `should not set cursor when text is null`() {
        // Given
        whenever(mockTextInputWidget.text).thenReturn(null)
        val cursorItem = createCursorItem(position = 3)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget, never()).setSelection(any())
    }

    @Test
    fun `should handle cursor at beginning of text`() {
        // Given
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = 0)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(0)
    }

    @Test
    fun `should handle null cursor gracefully`() {
        // Given
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = null)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget, never()).setSelection(any())
    }

    @Test
    fun `should handle very large cursor position by coercing to text length`() {
        // Given
        val textContent = "Hi"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = Int.MAX_VALUE)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(2) // coerced to textLength
    }

    @Test
    fun `should handle very large negative cursor position by coercing to zero`() {
        // Given
        val textContent = "Hello World"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = Int.MIN_VALUE)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(0) // coerced to 0
    }

    @Test
    fun `should handle single character text correctly`() {
        // Given
        val textContent = "A"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = 1)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(1) // at end of single character
    }

    @Test
    fun `should handle cursor position beyond single character by coercing`() {
        // Given
        val textContent = "A"
        val mockEditable = mock<Editable>()
        whenever(mockEditable.length).thenReturn(textContent.length)
        whenever(mockTextInputWidget.text).thenReturn(mockEditable)
        val cursorItem = createCursorItem(position = 5)
        
        // When
        textHolder.setCursor(cursorItem)
        
        // Then
        verify(mockTextInputWidget).setSelection(1) // coerced to textLength
    }

    // Helper method to create cursor items

    private fun createCursorItem(position: Int?): BlockView.Cursor {
        return object : BlockView.Cursor {
            override val cursor: Int? = position
        }
    }
}