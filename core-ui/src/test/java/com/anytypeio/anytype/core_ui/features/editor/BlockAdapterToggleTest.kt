package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Toggle
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_TOGGLE
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterToggleTest : BlockAdapterTestSetup() {

    @Test
    fun `toggle click should be intercepted when switching from read to edit mode`() {

        // Setup

        var triggerCount = 0

        val text = MockDataFactory.randomString()

        val block = BlockView.Text.Toggle(
            mode = BlockView.Mode.READ,
            text = text,
            id = MockDataFactory.randomUuid(),
            isFocused = false
        )

        val views = listOf(block)

        val adapter = buildAdapter(
            views = views,
            onToggleClicked = { triggerCount = triggerCount.inc() }
        )

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_TOGGLE)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Toggle)

        // Performing click in read-mode

        holder.toggle.performClick()

        assertEquals(
            actual = triggerCount,
            expected = 1
        )

        // Updating views

        adapter.updateWithDiffUtil(
            listOf(
                block.copy(
                    mode = BlockView.Mode.EDIT
                )
            )
        )

        val payload: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(BlockViewDiffUtil.READ_WRITE_MODE_CHANGED)
            )
        )

        adapter.onBindViewHolder(holder, 0, payload)

        // Performing click in edit mode

        holder.toggle.performClick()

        assertEquals(
            actual = triggerCount,
            expected = 2
        )
    }
}