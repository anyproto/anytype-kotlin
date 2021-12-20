package com.anytypeio.anytype.core_ui

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorDragAndDropListener
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Highlight
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HIGHLIGHT
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class HighlightingBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val clipboardInterceptor: ClipboardInterceptor = object : ClipboardInterceptor {
        override fun onClipboardAction(action: ClipboardInterceptor.Action) = Unit
    }

    @Test
    fun `should be bold markup in highlighting block`() {
        val headerOne = BlockView.Text.Highlight(
            text = "Test highlighting string with spans",
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            marks = listOf(Markup.Mark.Bold(from = 3, to = 10)),
            indent = 0,
            color = null,
            backgroundColor = null
        )

        val views = listOf(headerOne)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_HIGHLIGHT)

        adapter.onBindViewHolder(holder, 0)

        check(holder is Highlight)

        // Testing

        val spannableText =
            holder.root.findViewById<TextView>(R.id.highlightContent).text as Spannable

        val spans = spannableText.getSpans(
            0, spannableText.length,
            Any::class.java
        )

        assertNotEquals(illegal = 0, actual = spans.size)
        assertEquals(
            expected = true,
            actual = holder.content.isTextSelectable
        )

        var start = -1
        var end = -1

        for (span in spans) {
            if (span is Span.Bold) {
                start = spannableText.getSpanStart(span)
                end = spannableText.getSpanEnd(span)
            }
        }

        assertEquals(expected = 3, actual = start)
        assertEquals(expected = 10, actual = end)
    }

    private fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTitleTextChanged: (Editable) -> Unit = {},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            restore = LinkedList(),
            blocks = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = { _, _, _ -> },
            onSplitDescription = { _, _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onTextInputClicked = {},
            onPageIconClicked = {},
            onProfileIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onTextBlockTextChanged = {},
            onTitleBlockTextChanged = {},
            onContextMenuStyleClick = {},
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = clipboardInterceptor,
            onMentionEvent = {},
            onBackPressedCallback = { false },
            onCoverClicked = {},
            onSlashEvent = {},
            onKeyPressedEvent = {},
            onDragListener = EditorDragAndDropListener(
                onDragEnded = {},
                onDragExited = {},
                onDragLocation = { _,_ -> },
                onDrop = { _,_ -> }
            ),
            onDragAndDropTrigger = { true }
        )
    }
}