package com.agileburo.anytype.core_ui

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.Span
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.tools.ClipboardInterceptor
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class HighlightingBlockTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val clipboardInterceptor: ClipboardInterceptor = mock()

    @Test
    fun `should be bold markup in highlighting block`() {
        val headerOne = BlockView.Highlight(
            text = "Test highlighting string with spans",
            id = MockDataFactory.randomUuid(),
            mode = BlockView.Mode.EDIT,
            marks = listOf(Markup.Mark(type = Markup.Type.BOLD, from = 3, to = 10)),
            indent = 0,
            color = null,
            backgroundColor = null
        )

        val views = listOf(headerOne)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HIGHLIGHT)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Highlight)

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
        onEndLineEnterTitleClicked: (Editable) -> Unit = {},
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = { _, _, _ -> },
            onEndLineEnterClicked = { _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onFooterClicked = {},
            onTextInputClicked = {},
            onPageIconClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onParagraphTextChanged = { _, _ -> },
            onTitleTextChanged = onTitleTextChanged,
            onEndLineEnterTitleClicked = onEndLineEnterTitleClicked,
            onMarkupActionClicked = { _, _ -> },
            onTitleTextInputClicked = {},
            onClickListener = {},
            onProfileIconClicked = {},
            clipboardInterceptor = clipboardInterceptor
        )
    }
}