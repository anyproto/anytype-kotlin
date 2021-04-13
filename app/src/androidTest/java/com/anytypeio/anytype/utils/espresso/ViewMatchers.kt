package com.anytypeio.anytype.utils

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import com.anytypeio.anytype.core_utils.ext.dimen
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


class WithTextColor(private val expectedColor: Int) : BoundedMatcher<View, TextView>(TextView::class.java) {
    override fun matchesSafely(item: TextView) = item.currentTextColor == expectedColor
    override fun describeTo(description: Description) {
        description.appendText("with text color:")
        description.appendValue(expectedColor)
    }
}

class WithBackgroundColor(private val expected: Int) : BoundedMatcher<View, View>(View::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("with background color:")
        description.appendValue(expected)
    }

    override fun matchesSafely(item: View): Boolean {
        val actual = (item.background as ColorDrawable).color
        return actual == expected
    }
}

class WithoutBackgroundColor : BoundedMatcher<View, View>(View::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("with background color:")
    }

    override fun matchesSafely(item: View): Boolean {
        return item.background == null
    }
}

class WithChildViewCount(private val expectedCount: Int) : BoundedMatcher<View, ViewGroup>(ViewGroup::class.java) {
    override fun matchesSafely(item: ViewGroup): Boolean = item.childCount == expectedCount
    override fun describeTo(description: Description) {
        description.appendText("ViewGroup with child-count = $expectedCount");
    }
}

class HasChildViewWithText(private val pos: Int, val text: String, val target: Int) : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

    private var actual: String? = null

    override fun matchesSafely(item: RecyclerView): Boolean {
        val holder = item.findViewHolderForLayoutPosition(pos)
        checkNotNull(holder) { throw IllegalStateException("No holder at position: $pos") }
        val target = holder.itemView.findViewById<TextView>(target)
        actual = target.text.toString()
        return actual == text
    }
    override fun describeTo(description: Description) {
        if (actual != null) {
            description.appendText("Should have text [$text] at position: $pos but was: $actual");
        }
    }
}

class WithMarginStart(private val dimen: Int, private val coefficient: Int) : TypeSafeMatcher<View>() {

    var actual: Int = 0
    var expected: Int = 0

    override fun describeTo(description: Description) {
        description.appendText("with actual margin start:")
        description.appendValue(actual)
        description.appendText("with expected margin start:")
        description.appendValue(expected)
    }
    override fun matchesSafely(item: View): Boolean {
        actual = item.marginStart
        expected = (item.context.dimen(dimen) * coefficient).toInt()
        return actual == expected
    }
}

class WithPaddingLeft(private val dimen: Int, private val coefficient: Int) : TypeSafeMatcher<View>() {

    var actual: Int = 0
    var expected: Int = 0

    override fun describeTo(description: Description) {
        description.appendText("with actual padding start:")
        description.appendValue(actual)
        description.appendText("with expected padding start:")
        description.appendValue(expected)
    }
    override fun matchesSafely(item: View): Boolean {
        actual = item.paddingLeft
        expected = (item.context.dimen(dimen) * coefficient).toInt()
        return actual == expected
    }
}

class WithTextColorRes(private val expectedColorRes: Int) : BoundedMatcher<View, TextView>(TextView::class.java) {
    override fun matchesSafely(item: TextView): Boolean {
        val color = ContextCompat.getColor(item.context, expectedColorRes)
        return item.currentTextColor == color
    }
    override fun describeTo(description: Description) {
        description.appendText("with text color:")
        description.appendValue(expectedColorRes)
    }
}

class TextLineCountMatcher(private val lines : Int): TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("isTextInLines")
    }

    override fun matchesSafely(item: View?): Boolean {
        return (item as TextView).lineCount == lines
    }
}
