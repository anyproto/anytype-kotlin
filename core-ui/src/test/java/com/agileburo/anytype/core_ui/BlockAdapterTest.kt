package com.agileburo.anytype.core_ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.BACKGROUND_COLOR_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.FOCUS_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_COLOR_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.FOCUS_TIMEOUT_MILLIS
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.hexColorCode
import kotlinx.android.synthetic.main.item_block_bookmark.view.*
import kotlinx.android.synthetic.main.item_block_bookmark_placeholder.view.*
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_file_error.view.*
import kotlinx.android.synthetic.main.item_block_file_placeholder.view.*
import kotlinx.android.synthetic.main.item_block_file_uploading.view.*
import kotlinx.android.synthetic.main.item_block_page.view.*
import kotlinx.android.synthetic.main.item_block_picture.view.*
import kotlinx.android.synthetic.main.item_block_picture_error.view.*
import kotlinx.android.synthetic.main.item_block_picture_placeholder.view.*
import kotlinx.android.synthetic.main.item_block_picture_uploading.view.*
import kotlinx.android.synthetic.main.item_block_toggle.view.*
import kotlinx.android.synthetic.main.item_block_video.view.*
import kotlinx.android.synthetic.main.item_block_video_empty.view.*
import kotlinx.android.synthetic.main.item_block_video_error.view.*
import kotlinx.android.synthetic.main.item_block_video_uploading.view.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `should return transparent hex code when int color value is zero`() {

        val transparentColor = 0

        val actual = transparentColor.hexColorCode()

        assertEquals(
            expected = "#00000000",
            actual = actual
        )
    }

    @Test
    fun `should create paragraph view holder`() {

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        assertEquals(
            expected = BlockViewHolder.Paragraph::class,
            actual = holder::class
        )
    }

    @Test
    fun `should set text for paragraph holder`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        val text = holder.content.text.toString()

        assertEquals(
            expected = paragraph.text,
            actual = text
        )
    }

    @Test
    fun `should set text color for paragraph holder`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            color = Color.RED.hexColorCode()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        val color = holder.content.currentTextColor

        assertEquals(
            expected = Color.RED,
            actual = color
        )
    }

    @Test
    fun `should update paragraph holder with new text`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = paragraph.text,
            actual = holder.content.text.toString()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            )
        )

        assertEquals(
            expected = updated.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should update paragraph background color`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            backgroundColor = Color.RED.hexColorCode()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = paragraph.text,
            actual = holder.content.text.toString()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(BACKGROUND_COLOR_CHANGED)
                )
            )
        )

        assertEquals(
            expected = Color.parseColor(updated.backgroundColor),
            actual = (holder.root.background as ColorDrawable).color
        )
    }

    @Test
    fun `should update paragraph holder with new text color`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            color = Color.RED.hexColorCode()
        )

        val updated = paragraph.copy(
            color = Color.BLUE.hexColorCode()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = Color.RED,
            actual = holder.content.currentTextColor
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_COLOR_CHANGED)
                )
            )
        )

        assertEquals(
            expected = Color.BLUE,
            actual = holder.content.currentTextColor
        )
    }

    @Test
    fun `should request paragraph focus after delay of 60 ms`() {

        // Setup

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = true
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )
    }

    @Test
    fun `should call back when paragraph view gets focused`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = false
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onFocusChanged = { id, hasFocus ->
                events.add(Pair(id, hasFocus))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        holder.content.requestFocus()

        assertEquals(
            expected = listOf(
                Pair(paragraph.id, true)
            ),
            actual = events
        )
    }

    @Test
    fun `should request paragraph focus if payload changes contain focus-changed event`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = false
        )

        val focused = paragraph.copy(
            focused = true
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onFocusChanged = { id, hasFocus ->
                events.add(Pair(id, hasFocus))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(FOCUS_CHANGED)
                )
            ),
            item = focused
        )

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )

        assertEquals(
            expected = listOf(
                Pair(paragraph.id, true)
            ),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when binding data to paragraph holder`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when updating paragraph text with change payload`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val paragraph = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = paragraph.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(paragraph)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        holder.processChangePayload(
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            ),
            item = updated
        )

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should create title view holder`() {

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = MockDataFactory.randomBoolean()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        assertEquals(
            expected = BlockViewHolder.Title::class,
            actual = holder::class
        )
    }

    @Test
    fun `should set text for title holder`() {

        // Setup

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = MockDataFactory.randomBoolean()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        val text = holder.content.text.toString()

        assertEquals(
            expected = title.text,
            actual = text
        )
    }

    @Test
    fun `should update title holder with new text`() {

        // Setup

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = MockDataFactory.randomBoolean()
        )

        val updated = title.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        // Testing

        assertEquals(
            expected = title.text,
            actual = holder.content.text.toString()
        )

        holder.processPayloads(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            )
        )

        assertEquals(
            expected = updated.text,
            actual = holder.content.text.toString()
        )
    }

    @Test
    fun `should call back when title view gets focused`() {

        // Setup

        val events = mutableListOf<Pair<String, Boolean>>()

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = false
        )

        val views = listOf(title)

        val adapter = buildAdapter(
            views = views,
            onFocusChanged = { id, hasFocus ->
                events.add(Pair(id, hasFocus))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        holder.content.requestFocus()

        assertEquals(
            expected = listOf(
                Pair(title.id, true)
            ),
            actual = events
        )
    }

    @Test
    fun `should not trigger on-text-changed event when binding data to title holder`() {

        // Setup

        val events = mutableListOf<Pair<String, String>>()

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = MockDataFactory.randomBoolean()
        )

        val views = listOf(title)

        val adapter = buildAdapter(
            views = views,
            onTextChanged = { id, editable ->
                events.add(Pair(id, editable.toString()))
            }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        // Testing

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        Robolectric.getForegroundThreadScheduler().apply {
            advanceBy(FOCUS_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }

        assertEquals(
            expected = emptyList<Pair<String, String>>(),
            actual = events
        )
    }

    @Test
    fun `should preserve cursor position after updating paragraph text`() {

        // Setup

        val title = BlockView.Paragraph(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid()
        )

        val updated = title.copy(
            text = MockDataFactory.randomString()
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        // Testing

        assertEquals(
            expected = title.text,
            actual = holder.content.text.toString()
        )

        val cursorBeforeUpdate = holder.content.selectionEnd

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(TEXT_CHANGED)
                )
            )
        )

        val cursorAfterUpdate = holder.content.selectionEnd

        assertEquals(
            expected = cursorBeforeUpdate,
            actual = cursorAfterUpdate
        )
    }

    @Test
    fun `should apply indent to paragraph view`() {

        val paragraph = BlockView.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(paragraph)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PARAGRAPH)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Paragraph)

        val actual = holder.content.paddingLeft

        val expected = paragraph.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to header-one view`() {

        val view = BlockView.HeaderOne(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_ONE)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.HeaderOne)

        val actual = holder.content.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to header-two view`() {

        val view = BlockView.HeaderTwo(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_TWO)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.HeaderTwo)

        val actual = holder.content.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to header-three view`() {

        val view = BlockView.HeaderThree(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_HEADER_THREE)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.HeaderThree)

        val actual = holder.content.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to checkbox view`() {

        val view = BlockView.Checkbox(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_CHECKBOX)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Checkbox)

        val actual = holder.itemView.checkboxIcon.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to toggle view`() {

        val view = BlockView.Toggle(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            toggled = MockDataFactory.randomBoolean(),
            backgroundColor = null,
            color = null,
            focused = false,
            marks = emptyList()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TOGGLE)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Toggle)

        val actual =
            (holder.itemView.guideline.layoutParams as ConstraintLayout.LayoutParams).guideBegin

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to file placeholder view`() {

        val view = BlockView.File.Placeholder(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.File.Placeholder)

        val actual = holder.itemView.filePlaceholderRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to file error view`() {

        val view = BlockView.File.Error(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE_ERROR)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.File.Error)

        val actual = holder.itemView.fileErrorPlaceholderRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to file upload view`() {

        val view = BlockView.File.Upload(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_FILE_UPLOAD)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.File.Upload)

        val actual = holder.itemView.fileUploadingPlaceholderRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video view`() {

        val view = BlockView.Video.View(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt(),
            hash = MockDataFactory.randomString(),
            url = MockDataFactory.randomString(),
            mime = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Video)

        val actual = holder.itemView.playerView.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video placeholder view`() {

        val view = BlockView.Video.Placeholder(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Video.Placeholder)

        val actual = holder.itemView.videoPlaceholderRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video upload view`() {

        val view = BlockView.Video.Upload(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO_UPLOAD)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Video.Upload)

        val actual = holder.itemView.videoUploadingPlaceholderRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to video error view`() {

        val view = BlockView.Video.Error(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_VIDEO_ERROR)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Video.Error)

        val actual = holder.itemView.videoErrorRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to page view`() {

        val view = BlockView.Page(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt(),
            emoji = null,
            isEmpty = MockDataFactory.randomBoolean(),
            isArchived = MockDataFactory.randomBoolean()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PAGE)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Page)

        val actual =
            (holder.itemView.pageGuideline.layoutParams as ConstraintLayout.LayoutParams).guideBegin

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to bookmark view`() {

        val view = BlockView.Bookmark.View(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt(),
            description = MockDataFactory.randomString(),
            title = MockDataFactory.randomString(),
            faviconUrl = MockDataFactory.randomString(),
            imageUrl = MockDataFactory.randomString(),
            url = MockDataFactory.randomString()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BOOKMARK)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Bookmark)

        val actual = (holder.itemView.bookmarkRoot).marginLeft

        val expected = view.indent * holder.dimen(R.dimen.indent) + holder.dimen(R.dimen.dp_16)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to bookmark placeholder view`() {

        val view = BlockView.Bookmark.Placeholder(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_BOOKMARK_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Bookmark.Placeholder)

        val actual = holder.itemView.bookmarkPlaceholderRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture view`() {

        val view = BlockView.Picture.View(
            id = MockDataFactory.randomUuid(),
            hash = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            mime = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            url = MockDataFactory.randomString()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Picture)

        val actual = holder.itemView.pictureRootLayout.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture placeholder view`() {

        val view = BlockView.Picture.Placeholder(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder =
            adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE_PLACEHOLDER)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Picture.Placeholder)

        val actual = holder.itemView.picturePlaceholderRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture error view`() {

        val view = BlockView.Picture.Error(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE_ERROR)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Picture.Error)

        val actual = holder.itemView.pictureErrorRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply indent to picture upload view`() {

        val view = BlockView.Picture.Upload(
            id = MockDataFactory.randomUuid(),
            indent = MockDataFactory.randomInt()
        )

        val views = listOf(view)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val adapter = buildAdapter(views)

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_PICTURE_UPLOAD)

        adapter.bindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Picture.Upload)

        val actual = holder.itemView.pictureUploadRoot.paddingLeft

        val expected = view.indent * holder.dimen(R.dimen.indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should apply focus to title block with payload change`() {

        // Setup

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = false
        )

        val updated = title.copy(
            focused = true
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        // Testing

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(FOCUS_CHANGED)
                )
            )
        )

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )
    }

    @Test
    fun `should remove focus from title block with payload change`() {

        // Setup

        val title = BlockView.Title(
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            focused = true
        )

        val updated = title.copy(
            focused = false
        )

        val views = listOf(title)

        val adapter = buildAdapter(views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_TITLE)

        adapter.onBindViewHolder(holder, 0)

        check(holder is BlockViewHolder.Title)

        // Testing

        assertEquals(
            expected = true,
            actual = holder.content.hasFocus()
        )

        holder.processChangePayload(
            item = updated,
            payloads = listOf(
                BlockViewDiffUtil.Payload(
                    changes = listOf(FOCUS_CHANGED)
                )
            )
        )

        assertEquals(
            expected = false,
            actual = holder.content.hasFocus()
        )
    }

    private fun buildAdapter(
        views: List<BlockView>,
        onFocusChanged: (String, Boolean) -> Unit = { _, _ -> },
        onTextChanged: (String, Editable) -> Unit = { _, _ -> }
    ): BlockAdapter {
        return BlockAdapter(
            blocks = views,
            onNonEmptyBlockBackspaceClicked = {},
            onEmptyBlockBackspaceClicked = {},
            onSplitLineEnterClicked = { _, _ -> },
            onEndLineEnterClicked = { _, _ -> },
            onTextChanged = onTextChanged,
            onCheckboxClicked = {},
            onFocusChanged = onFocusChanged,
            onSelectionChanged = { _, _ -> },
            onFooterClicked = {},
            onPageClicked = {},
            onTextInputClicked = {},
            onDownloadFileClicked = {},
            onPageIconClicked = {},
            onAddLocalFileClick = {},
            onAddLocalPictureClick = {},
            onAddLocalVideoClick = {},
            onAddUrlClick = { _, _ -> },
            onBookmarkPlaceholderClicked = {},
            onTogglePlaceholderClicked = {},
            onToggleClicked = {},
            onMediaBlockMenuClick = {},
            onParagraphTextChanged = { _, _ -> }
        )
    }
}