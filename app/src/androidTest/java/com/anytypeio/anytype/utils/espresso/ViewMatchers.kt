package com.anytypeio.anytype.utils.espresso

import android.view.View
import androidx.core.view.marginStart
import com.anytypeio.anytype.core_utils.ext.dimen
import org.hamcrest.Description
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