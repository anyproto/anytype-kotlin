package com.agileburo.anytype.feature_editor.presentation

import androidx.recyclerview.widget.DiffUtil
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.Mark
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.util.BlockViewDiffUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BlockViewDiffUtilTest {

    val block1 = BlockView(
        id = "c0301f2b-b532-55e1-92ba-2b2fc4af237e",
        contentType = ContentType.P,
        content = BlockView.Content.Text(
            text = "Первый и второй",
            param = BlockView.ContentParam(map = mapOf("0" to false))
        )
    )

    val block2 = BlockView(
        id = "c0301f2b-b532-55e1-92ba-2b2fc4af237e",
        contentType = ContentType.P,
        content = BlockView.Content.Text(
            text = "Первый и второй",
            param = BlockView.ContentParam(map = mapOf("0" to false))
        )
    )

    lateinit var diffUtil : DiffUtil.Callback

    @Before
    fun init() {
        diffUtil = BlockViewDiffUtil(old = listOf(block1), new = listOf(block2))
    }

    @Test
    fun `blocks should be the same`() {
        val result = diffUtil.areItemsTheSame(0, 0)
        assert(result)
    }

    @Test
    fun `blocks should be equals`() {
        val result = diffUtil.areContentsTheSame(0,0)
        Assert.assertTrue(result)
    }

}