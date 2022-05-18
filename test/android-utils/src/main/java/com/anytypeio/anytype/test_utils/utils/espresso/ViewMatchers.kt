package com.anytypeio.anytype.test_utils.utils.espresso

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class WithTextColor(
    @ColorInt
    private val expectedColor: Int
) :
    BoundedMatcher<View, TextView>(TextView::class.java) {
    override fun matchesSafely(item: TextView) = item.currentTextColor == expectedColor
    override fun describeTo(description: Description) {
        description.appendText("with text color:")
        description.appendValue(expectedColor)
    }
}

class WithBackgroundColor(
    @ColorInt
    private val expected: Int
) : BoundedMatcher<View, View>(View::class.java) {
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

class WithChildViewCount(private val expectedCount: Int) :
    BoundedMatcher<View, ViewGroup>(ViewGroup::class.java) {
    override fun matchesSafely(item: ViewGroup): Boolean = item.childCount == expectedCount
    override fun describeTo(description: Description) {
        description.appendText("ViewGroup with child-count = $expectedCount");
    }
}

class HasViewGroupChildViewWithText(private val pos: Int, val text: String) :
    BoundedMatcher<View, ViewGroup>(ViewGroup::class.java) {

    private var actual: String? = null

    override fun matchesSafely(item: ViewGroup): Boolean {
        val child = item.getChildAt(pos)
        checkNotNull(child) { throw IllegalStateException("No view child at position: $pos") }
        check(child is TextView) { throw IllegalStateException("Child view is not text view at position: $pos, but: ${child::class.java.canonicalName}") }
        actual = child.text.toString()
        return actual == text
    }

    override fun describeTo(description: Description) {
        if (actual != null) {
            description.appendText("Should have text [$text] at position: $pos but was: $actual");
        }
    }
}

class HasChildViewWithText(private val pos: Int, val text: String, val target: Int) :
    BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

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

class WithTextColorRes(
    @ColorRes
    private val expectedColorRes: Int
) :
    BoundedMatcher<View, TextView>(TextView::class.java) {
    override fun matchesSafely(item: TextView): Boolean {
        val color = ContextCompat.getColor(item.context, expectedColorRes)
        return item.currentTextColor == color
    }

    override fun describeTo(description: Description) {
        description.appendText("with text color:")
        description.appendValue(expectedColorRes)
    }
}

class TextLineCountMatcher(private val lines: Int) : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.appendText("isTextInLines")
    }

    override fun matchesSafely(item: View?): Boolean {
        return (item as TextView).lineCount == lines
    }
}

class SetEditTextSelectionAction(private val selection: Int) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
    }

    override fun getDescription(): String {
        return "set selection to $selection"
    }

    override fun perform(uiController: UiController, view: View) {
        (view as EditText).setSelection(selection)
    }
}
