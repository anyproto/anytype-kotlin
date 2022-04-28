package com.anytypeio.anytype.utils.espresso

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.anytypeio.anytype.core_utils.ext.dimen
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


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