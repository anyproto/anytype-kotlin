package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Featured relations (object type, tags, ...) used to live in a row whose height was
 * hardcoded to 18dp, while the text inside is sized in sp. At the system font scales
 * above 1.0 the text no longer fitted and was drawn clipped (DROID-4563).
 *
 * [GraphicsMode.Mode.NATIVE] is required: with the legacy shadow graphics Robolectric
 * reports font metrics that do not depend on the text size, so the clipping is invisible.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RelationValueListWidgetFontScaleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun before() {
        context.setTheme(R.style.Theme_MaterialComponents)
    }

    private class Measurement(val rowHeight: Int, val textHeight: Int, val textNeeds: Int)

    private fun measureAtFontScale(scale: Float): Measurement {
        val configuration = Configuration(context.resources.configuration).apply {
            fontScale = scale
        }
        val widget = RelationValueListWidget(context.createConfigurationContext(configuration))
        widget.setRelation(
            relation = ObjectRelationView.ObjectType.Base(
                id = "relation-id",
                key = "type",
                name = "Page",
                featured = true,
                system = false,
                readOnly = false,
                type = "type-id"
            )
        )
        widget.measure(
            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val text = widget.findViewById<TextView>(R.id.text1)
        return Measurement(
            rowHeight = widget.measuredHeight,
            textHeight = text.measuredHeight,
            textNeeds = requireNotNull(text.layout) { "Text must be laid out" }.height
        )
    }

    @Test
    fun `object type is not clipped at any system font scale`() {
        listOf(1f, 1.15f, 1.3f, 1.5f, 2f).forEach { scale ->
            val measurement = measureAtFontScale(scale)
            assertTrue(
                actual = measurement.textHeight >= measurement.textNeeds,
                message = "At font scale $scale the text view is ${measurement.textHeight}px " +
                    "tall but needs ${measurement.textNeeds}px, so the text is clipped"
            )
            assertTrue(
                actual = measurement.rowHeight >= measurement.textNeeds,
                message = "At font scale $scale the relation row is ${measurement.rowHeight}px " +
                    "tall but its text needs ${measurement.textNeeds}px"
            )
        }
    }

    @Test
    fun `relation row grows with the system font scale`() {
        val default = measureAtFontScale(1f).rowHeight
        val largest = measureAtFontScale(2f).rowHeight
        assertTrue(
            actual = largest > default,
            message = "Row must grow with the font scale but was ${default}px at 1.0 " +
                "and ${largest}px at 2.0"
        )
    }

    @Test
    fun `relation row keeps its designed height at the default font scale`() {
        val expected = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            18f,
            context.resources.displayMetrics
        ).toInt()
        val actual = measureAtFontScale(1f).rowHeight
        assertTrue(
            actual = actual == expected,
            message = "Row must still be ${expected}px at the default font scale but was ${actual}px"
        )
    }
}
