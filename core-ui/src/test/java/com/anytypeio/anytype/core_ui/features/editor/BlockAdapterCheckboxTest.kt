package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import androidx.core.text.getSpans
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.CheckedCheckboxColorSpan
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Bulleted
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Checkbox
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BULLET
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_CHECKBOX
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterCheckboxTest : BlockAdapterTestSetup() {

    @Test
    fun `should be default color when not checked, no text color and no text color markups`() {

        val checkbox = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            isFocused = false,
            isChecked = false
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Checkbox)

        assertEquals(
            actual = holder.content.currentTextColor,
            expected = context.resources.getColor(R.color.text_primary, null)
        )
    }

    @Test
    fun `should have checkbox highlight span when checked`() {

        val checkbox = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            isFocused = false,
            isChecked = true
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Checkbox)

        assertEquals(
            actual = holder.content.currentTextColor,
            expected = context.resources.getColor(R.color.text_primary, null)
        )

        val spans = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0)

        assertEquals(
            expected = 1,
            actual = spans.size
        )
    }

    @Test
    fun `should not have checkboxhighlight span when not checked`() {

        val checkbox = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            isFocused = false,
            isChecked = false
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Checkbox)

        val spans = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0)

        assertEquals(
            expected = 0,
            actual = spans.size
        )
    }

    @Test
    fun `should have checkboxhighlight when payload text changed and is checked`() {

        val text = "First"

        val checkbox = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = text,
            isFocused = false,
            isChecked = true
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Checkbox)

        assertEquals(
            expected = 1,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )

        holder.processChangePayload(
            payloads = listOf(BlockViewDiffUtil.Payload(changes = listOf(BlockViewDiffUtil.TEXT_CHANGED))),
            item = checkbox.copy(text = "$text Second"),
            clicked = {},
        )

        assertEquals(
            expected = 1,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )
    }

    @Test
    fun `should have checkboxhighlight when payload markup changed and is checked`() {

        val text = "First"

        val checkbox = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = text,
            isFocused = false,
            isChecked = true
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Checkbox)

        assertEquals(
            expected = 1,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )

        holder.processChangePayload(
            payloads = listOf(BlockViewDiffUtil.Payload(changes = listOf(BlockViewDiffUtil.MARKUP_CHANGED))),
            item = checkbox.copy(
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color = "lime"
                    )
                )
            ),
            clicked = {},
        )

        assertEquals(
            expected = 1,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )
    }

    @Test
    fun `should not have checkboxhighlight when payload markup changed and is not checked`() {

        val text = "First"

        val checkbox = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = text,
            isFocused = false,
            isChecked = false
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Checkbox)

        assertEquals(
            expected = 0,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )

        holder.processChangePayload(
            payloads = listOf(BlockViewDiffUtil.Payload(changes = listOf(BlockViewDiffUtil.MARKUP_CHANGED))),
            item = checkbox.copy(
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color = "lime"
                    )
                )
            ),
            clicked = {},
        )

        assertEquals(
            expected = 0,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )
    }

    @Test
    fun `should not have checkboxhighlight when payload text changed and is not checked`() {

        val text = "First"

        val checkbox = BlockView.Text.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = text,
            isFocused = false,
            isChecked = false
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_CHECKBOX)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Checkbox)

        assertEquals(
            expected = 0,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )

        holder.processChangePayload(
            payloads = listOf(BlockViewDiffUtil.Payload(changes = listOf(BlockViewDiffUtil.TEXT_CHANGED))),
            item = checkbox.copy(text = "$text Second"),
            clicked = {},
        )

        assertEquals(
            expected = 0,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )
    }

    @Test
    fun `should not have checkboxhighlight when payload text changed and item is not checkbox `() {

        val text = "First"

        val checkbox = BlockView.Text.Bulleted(
            id = MockDataFactory.randomUuid(),
            text = text,
            isFocused = false
        )

        val views = listOf(checkbox)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_BULLET)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Bulleted)

        assertEquals(
            expected = 0,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )

        holder.processChangePayload(
            payloads = listOf(BlockViewDiffUtil.Payload(changes = listOf(BlockViewDiffUtil.TEXT_CHANGED))),
            item = checkbox.copy(text = "$text Second"),
            clicked = {},
        )

        assertEquals(
            expected = 0,
            actual = holder.content.text!!.getSpans<CheckedCheckboxColorSpan>(0).size
        )
    }
}