package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Bulleted
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BULLET
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class BlockAdapterTextColorTest : BlockAdapterTestSetup() {

    @Test
    fun `should update bulleted text color to default via payload change`() {

        // SETUP

        val bulleted = BlockView.Text.Bulleted(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            isFocused = false,
            color = ThemeColor.BLUE
        )

        val updated = BlockView.Text.Bulleted(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString(),
            isFocused = false
        )

        val views = listOf(bulleted)

        val adapter = buildAdapter(views = views)

        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        val holder = adapter.onCreateViewHolder(recycler, HOLDER_BULLET)

        adapter.onBindViewHolder(holder, 0)

        // TESTING

        check(holder is Bulleted)

        // Checking that initial text color is initial text color

        assertEquals(
            actual = holder.content.currentTextColor,
            expected = context.resources.dark(ThemeColor.BLUE)
        )

        val payload: MutableList<Any> = mutableListOf(
            BlockViewDiffUtil.Payload(
                changes = listOf(BlockViewDiffUtil.TEXT_COLOR_CHANGED)
            )
        )

        adapter.updateWithDiffUtil(listOf(updated))

        adapter.onBindViewHolder(holder, 0, payloads = payload)

        // Checking that text color change has been applied.

        assertEquals(
            actual = holder.content.currentTextColor,
            expected = context.resources.dark(ThemeColor.DEFAULT)
        )
    }
}