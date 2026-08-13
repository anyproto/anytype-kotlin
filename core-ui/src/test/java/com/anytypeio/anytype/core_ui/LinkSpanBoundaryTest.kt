package com.anytypeio.anytype.core_ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.common.setMarkup
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * A link span covers its own text only: text typed at its end must stay
 * outside the link. A formatting span (bold) grows on purpose — text typed
 * at its end continues the style.
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class LinkSpanBoundaryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun editableWith(markup: Markup): SpannableStringBuilder {
        val editable = SpannableStringBuilder(markup.body)
        editable.setMarkup(
            markup = markup,
            context = context,
            click = {},
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 12f,
            textColor = Color.BLACK,
            underlineHeight = 1f
        )
        return editable
    }

    @Test
    fun `should not extend a link span over text typed at its end`() {
        val url = "https://anytype.io"
        val markup = object : Markup {
            override val body: String = url
            override var marks: List<Markup.Mark> = listOf(
                Markup.Mark.Link(from = 0, to = url.length, param = url)
            )
        }

        val editable = editableWith(markup)

        editable.insert(editable.length, "x")

        val link = editable.marks().filterIsInstance<Markup.Mark.Link>().single()

        assertEquals(0, link.from)
        assertEquals(url.length, link.to)
    }

    @Test
    fun `should extend a bold span over text typed at its end`() {
        val markup = object : Markup {
            override val body: String = "Bold"
            override var marks: List<Markup.Mark> = listOf(
                Markup.Mark.Bold(from = 0, to = 4)
            )
        }

        val editable = editableWith(markup)

        editable.insert(editable.length, "x")

        val bold = editable.marks().filterIsInstance<Markup.Mark.Bold>().single()

        assertEquals(0, bold.from)
        assertEquals(5, bold.to)
    }

    @Test
    fun `should not extend an object link span over text typed at its end`() {
        val markup = object : Markup {
            override val body: String = "My object"
            override var marks: List<Markup.Mark> = listOf(
                Markup.Mark.Object(from = 0, to = 9, param = "object-id", isArchived = false)
            )
        }

        val editable = editableWith(markup)

        editable.insert(editable.length, "x")

        val link = editable.marks().filterIsInstance<Markup.Mark.Object>().single()

        assertEquals(0, link.from)
        assertEquals(9, link.to)
    }
}
