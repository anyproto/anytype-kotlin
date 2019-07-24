package com.agileburo.anytype.feature_desktop

import com.agileburo.anytype.feature_desktop.mvvm.DesktopView
import com.agileburo.anytype.feature_desktop.utils.DesktopDiffUtil
import org.junit.Test
import kotlin.test.assertEquals

class DesktopDiffUtilTest {

    @Test
    fun placeholderItemsAndContentsAreTheSame() {

        val old = listOf(DesktopView.NewDocument)
        val new = listOf(DesktopView.NewDocument)

        val util = DesktopDiffUtil(old, new)

        val firstResult = util.areItemsTheSame(0, 0)

        assertEquals(
            expected = true,
            actual = firstResult
        )

        val secondResult = util.areContentsTheSame(0, 0)

        assertEquals(
            expected = true,
            actual = secondResult
        )
    }

    @Test
    fun itemsAndContentsAreTheSame() {

        val id = DataFactory.randomUuid()
        val title = DataFactory.randomString()

        val old = listOf(
            DesktopView.Document(
                id = id,
                title = title
            )
        )

        val new = listOf(
            DesktopView.Document(
                id = id,
                title = title
            )
        )

        val util = DesktopDiffUtil(old, new)

        val firstResult = util.areItemsTheSame(0, 0)

        assertEquals(
            expected = true,
            actual = firstResult
        )

        val secondResult = util.areContentsTheSame(0, 0)

        assertEquals(
            expected = true,
            actual = secondResult
        )
    }

    @Test
    fun itemsAreNotTheSame() {

        val old = listOf(DesktopView.NewDocument)

        val new = listOf(
            DesktopView.Document(
                id = DataFactory.randomUuid(),
                title = DataFactory.randomString()
            )
        )

        val util = DesktopDiffUtil(old, new)

        val firstResult = util.areItemsTheSame(0, 0)

        assertEquals(
            expected = false,
            actual = firstResult
        )
    }

    @Test
    fun itemsAreTheSameButNotContents() {

        val id = DataFactory.randomUuid()

        val old = listOf(
            DesktopView.Document(
                id = id,
                title = DataFactory.randomString()
            )
        )

        val new = listOf(
            DesktopView.Document(
                id = id,
                title = DataFactory.randomString()
            )
        )

        val util = DesktopDiffUtil(old, new)

        val firstResult = util.areItemsTheSame(0, 0)

        assertEquals(
            expected = true,
            actual = firstResult
        )

        val secondResult = util.areContentsTheSame(0, 0)

        assertEquals(
            expected = false,
            actual = secondResult
        )
    }
}