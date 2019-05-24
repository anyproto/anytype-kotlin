package com.agileburo.anytype.feature_editor

import android.text.SpannableString
import androidx.recyclerview.widget.DiffUtil
import com.agileburo.anytype.feature_editor.factory.AndroidDataFactory
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.util.BlockViewDiffUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BlockViewDiffUtilTest {


    private lateinit var diffUtil : DiffUtil.Callback

    @Before
    fun init() {

        val firstBlock = BlockView.ParagraphView(
            id = AndroidDataFactory.randomString(),
            text = SpannableString(AndroidDataFactory.randomString())
        )

        val second = firstBlock.copy()

        diffUtil = BlockViewDiffUtil(listOf(firstBlock), listOf(second))
    }

    @Test
    fun blocksShouldBeTheSame() {
        val result = diffUtil.areItemsTheSame(0, 0)
        assert(result)
    }

    @Test
    fun blockShouldBeEquals() {
        val result = diffUtil.areContentsTheSame(0,0)
        Assert.assertTrue(result)
    }

}