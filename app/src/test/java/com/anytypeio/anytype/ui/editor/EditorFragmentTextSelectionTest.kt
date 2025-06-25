package com.anytypeio.anytype.ui.editor

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anytypeio.anytype.databinding.FragmentEditorBinding
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class EditorFragmentTextSelectionTest {

    private lateinit var fragment: EditorFragment
    private lateinit var binding: FragmentEditorBinding
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setup() {
        fragment = spyk(EditorFragment())
        binding = mockk(relaxed = true)
        recyclerView = mockk(relaxed = true)
        
        every { binding.recycler } returns recyclerView
        every { fragment.binding } returns binding
    }

    @Test
    fun `clearActiveTextSelections should clear focus from recycler view`() {
        // Given
        val focusedView = mockk<TextView>(relaxed = true)
        every { recyclerView.findFocus() } returns focusedView

        // When
        fragment.invokePrivate("clearActiveTextSelections")

        // Then
        verify { recyclerView.clearFocus() }
        verify { focusedView.clearFocus() }
    }

    @Test
    fun `clearActiveTextSelections should handle TextView with selection`() {
        // Given
        val textView = mockk<TextView>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        
        every { recyclerView.findFocus() } returns null
        every { recyclerView.childCount } returns 1
        every { recyclerView.getChildAt(0) } returns viewGroup
        every { viewGroup.childCount } returns 1
        every { viewGroup.getChildAt(0) } returns textView
        every { textView.hasSelection() } returns true

        // When
        fragment.invokePrivate("clearActiveTextSelections")

        // Then
        verify { textView.clearFocus() }
        verify { textView.clearComposingText() }
        verify { textView.customSelectionActionModeCallback = null }
        verify { textView.customInsertionActionModeCallback = null }
    }

    @Test
    fun `clearActiveTextSelections should handle EditText with selection`() {
        // Given
        val editText = mockk<EditText>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        val spannableText = mockk<android.text.SpannableStringBuilder>(relaxed = true)
        
        every { recyclerView.findFocus() } returns null
        every { recyclerView.childCount } returns 1
        every { recyclerView.getChildAt(0) } returns viewGroup
        every { viewGroup.childCount } returns 1
        every { viewGroup.getChildAt(0) } returns editText
        every { editText.hasSelection() } returns true
        every { editText.text } returns spannableText
        every { spannableText.isNotEmpty() } returns true

        // When
        fragment.invokePrivate("clearActiveTextSelections")

        // Then
        verify { editText.clearFocus() }
        verify { editText.clearComposingText() }
        verify { editText.setSelection(0, 0) }
        verify { editText.customSelectionActionModeCallback = null }
        verify { editText.customInsertionActionModeCallback = null }
    }

    @Test
    fun `clearActiveTextSelections should handle EditText with empty text safely`() {
        // Given
        val editText = mockk<EditText>(relaxed = true)
        val viewGroup = mockk<ViewGroup>(relaxed = true)
        
        every { recyclerView.findFocus() } returns null
        every { recyclerView.childCount } returns 1
        every { recyclerView.getChildAt(0) } returns viewGroup
        every { viewGroup.childCount } returns 1
        every { viewGroup.getChildAt(0) } returns editText
        every { editText.hasSelection() } returns true
        every { editText.text } returns null

        // When
        fragment.invokePrivate("clearActiveTextSelections")

        // Then
        verify { editText.clearFocus() }
        verify { editText.clearComposingText() }
        verify(exactly = 0) { editText.setSelection(any(), any()) }
        verify { editText.customSelectionActionModeCallback = null }
        verify { editText.customInsertionActionModeCallback = null }
    }

    @Test
    fun `clearActiveTextSelections should handle nested ViewGroups recursively`() {
        // Given
        val textView = mockk<TextView>(relaxed = true)
        val innerViewGroup = mockk<ViewGroup>(relaxed = true)
        val outerViewGroup = mockk<ViewGroup>(relaxed = true)
        
        every { recyclerView.findFocus() } returns null
        every { recyclerView.childCount } returns 1
        every { recyclerView.getChildAt(0) } returns outerViewGroup
        every { outerViewGroup.childCount } returns 1
        every { outerViewGroup.getChildAt(0) } returns innerViewGroup
        every { innerViewGroup.childCount } returns 1
        every { innerViewGroup.getChildAt(0) } returns textView
        every { textView.hasSelection() } returns false

        // When
        fragment.invokePrivate("clearActiveTextSelections")

        // Then
        verify { textView.clearFocus() }
        verify { textView.customSelectionActionModeCallback = null }
        verify { textView.customInsertionActionModeCallback = null }
    }

    @Test
    fun `clearActiveTextSelections should handle exceptions gracefully`() {
        // Given
        every { recyclerView.clearFocus() } throws RuntimeException("Test exception")

        // When - should not throw exception
        fragment.invokePrivate("clearActiveTextSelections")

        // Then
        verify { recyclerView.clearFocus() }
    }

    @Test
    fun `clearActiveTextSelections should be called in onDestroyView`() {
        // Given
        val fragment = spyk(EditorFragment())
        every { fragment.binding } returns binding
        every { fragment.invokePrivate("clearActiveTextSelections") } just Runs

        // When
        fragment.onDestroyView()

        // Then
        verify { fragment.invokePrivate("clearActiveTextSelections") }
    }

    @Test
    fun `clearActiveTextSelections should be called in exitScrollAndMove`() {
        // Given
        val fragment = spyk(EditorFragment())
        every { fragment.binding } returns binding
        every { fragment.invokePrivate("clearActiveTextSelections") } just Runs
        every { recyclerView.removeItemDecoration(any()) } just Runs
        every { recyclerView.removeOnScrollListener(any()) } just Runs

        // When
        fragment.invokePrivate("exitScrollAndMove")

        // Then
        verify { fragment.invokePrivate("clearActiveTextSelections") }
    }

    private fun Any.invokePrivate(methodName: String, vararg args: Any?) {
        val method = this::class.java.getDeclaredMethod(methodName, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
        method.isAccessible = true
        method.invoke(this, *args)
    }
}