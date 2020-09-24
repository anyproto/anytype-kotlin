package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.MockDataFactory
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Numbered
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterNumberedListTest : BlockAdapterTestSetup() {

    @Test
    fun `should update number and add dot at the end when number and indent are changed`() {

        // Setup


        val a = BlockView.Text.Numbered(
            mode = BlockView.Mode.READ,
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = false,
            indent = 0,
            number = 1
        )

        val b = BlockView.Text.Numbered(
            mode = BlockView.Mode.READ,
            text = MockDataFactory.randomString(),
            id = MockDataFactory.randomUuid(),
            isFocused = false,
            indent = 0,
            number = 2
        )

        val views = listOf(a, b)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val aHolder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_NUMBERED)
        val bHolder = adapter.onCreateViewHolder(recycler, BlockViewHolder.HOLDER_NUMBERED)

        adapter.onBindViewHolder(aHolder, 0)
        adapter.onBindViewHolder(bHolder, 1)

        // TESTING

        check(aHolder is Numbered)
        check(bHolder is Numbered)

        assertEquals(
            expected = "1.",
            actual = aHolder.number.text.toString()
        )

        assertEquals(
            expected = "2.",
            actual = bHolder.number.text.toString()
        )

        adapter.updateWithDiffUtil(
            listOf(
                a,
                b.copy(
                    indent = 1,
                    number = 1
                )
            )
        )

        val payload: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(
                    BlockViewDiffUtil.INDENT_CHANGED,
                    BlockViewDiffUtil.NUMBER_CHANGED,
                )
            )
        )

        adapter.onBindViewHolder(aHolder, 0, payload)
        adapter.onBindViewHolder(bHolder, 1, payload)

        assertEquals(
            expected = "1.",
            actual = aHolder.number.text.toString()
        )

        assertEquals(
            expected = "1.",
            actual = bHolder.number.text.toString()
        )
    }

}