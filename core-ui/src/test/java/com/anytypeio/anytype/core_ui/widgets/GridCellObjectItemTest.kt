package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.R
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class GridCellObjectItemTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun before() {
        context.setTheme(R.style.Theme_MaterialComponents)
    }

    @Test
    @Config(qualifiers = "night")
    fun `object name is rendered with primary text color in dark mode`() {
        val item = GridCellObjectItem(context)

        item.setup(name = "Page", icon = ObjectIcon.None)

        // @color/black is #2C2B27 in every configuration, so in dark mode it was
        // almost indistinguishable from the background of a set or collection row.
        assertEquals(
            context.getColor(R.color.text_primary),
            item.binding.tvName.currentTextColor
        )
    }

    @Test
    fun `object name is rendered with primary text color in light mode`() {
        val item = GridCellObjectItem(context)

        item.setup(name = "Page", icon = ObjectIcon.None)

        assertEquals(
            context.getColor(R.color.text_primary),
            item.binding.tvName.currentTextColor
        )
    }

    @Test
    @Config(qualifiers = "night")
    fun `non existent object is rendered with tertiary text color in dark mode`() {
        val item = GridCellObjectItem(context)

        item.setupAsNonExistent()

        assertEquals(
            context.getColor(R.color.text_tertiary),
            item.binding.tvName.currentTextColor
        )
    }

    @Test
    @Config(qualifiers = "night")
    fun `recycled non existent cell is restored to primary text color`() {
        val item = GridCellObjectItem(context)

        item.setupAsNonExistent()
        item.setup(name = "Page", icon = ObjectIcon.None)

        assertEquals(
            context.getColor(R.color.text_primary),
            item.binding.tvName.currentTextColor
        )
    }
}
