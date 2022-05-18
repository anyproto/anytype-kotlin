package com.anytypeio.anytype.test_utils.utils

import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withInputType
import com.anytypeio.anytype.test_utils.utils.TestUtils.withRecyclerView
import com.anytypeio.anytype.test_utils.utils.espresso.HasChildViewWithText
import com.anytypeio.anytype.test_utils.utils.espresso.HasViewGroupChildViewWithText
import com.anytypeio.anytype.test_utils.utils.espresso.WithBackgroundColor
import com.anytypeio.anytype.test_utils.utils.espresso.WithChildViewCount
import com.anytypeio.anytype.test_utils.utils.espresso.WithTextColor
import com.anytypeio.anytype.test_utils.utils.espresso.WithoutBackgroundColor
import org.hamcrest.Matchers.not

fun Int.findItemAt(position: Int, layoutId: Int): ViewInteraction {
    return onView(withRecyclerView(this).atPositionOnView(position, layoutId))
}

fun ViewInteraction.performClick(): ViewInteraction = perform(ViewActions.click())
fun Int.matchView(): ViewInteraction = onView(withId(this))
fun Int.performClick(): ViewInteraction = matchView().performClick()
fun Int.type(text: String) = matchView().perform(click(), typeText(text))

fun ViewInteraction.checkHasText(text: String) {
    check(matches(ViewMatchers.withText(text)))
}

fun ViewInteraction.checkHasHintText(text: Int) {
    check(matches(withHint(text)))
}

fun ViewInteraction.checkHasInputType(type: Int) {
    check(matches(withInputType(type)))
}

fun ViewInteraction.checkIsSelected() {
    check(matches(ViewMatchers.isSelected()))
}

fun ViewInteraction.checkIsFocused() {
    check(matches(ViewMatchers.isFocused()))
}

fun ViewInteraction.checkIsNotFocused() {
    check(matches(not(ViewMatchers.isFocused())))
}

fun ViewInteraction.checkIsDisplayed() {
    check(matches(isDisplayed()))
}

fun ViewInteraction.checkIsNotDisplayed() {
    check(matches(not(isDisplayed())))
}

fun ViewInteraction.checkIsNotSelected() {
    check(matches(not(ViewMatchers.isSelected())))
}

fun ViewInteraction.checkHasText(@IdRes resId: Int) {
    check(matches(ViewMatchers.withText(resId)))
}

fun ViewInteraction.checkHasTextColor(@ColorInt color: Int) {
    check(matches(WithTextColor(color)))
}

fun ViewInteraction.checkHasBackgroundColor(@ColorInt color: Int) {
    check(matches(WithBackgroundColor(color)))
}

fun ViewInteraction.checkHasNoBackground() {
    check(matches(WithoutBackgroundColor()))
}

fun ViewInteraction.checkHasChildViewCount(count: Int) : ViewInteraction {
    return check(matches(WithChildViewCount(count)))
}

fun Int.rVMatcher(): RecyclerViewMatcher =
    RecyclerViewMatcher(this)

fun Int.checkRecyclerItemCount(expected: Int) = matchView().check(RecyclerViewItemCountAssertion(expected))

fun RecyclerViewMatcher.onItemView(pos: Int, target: Int): ViewInteraction {
    return onView(atPositionOnView(pos, target))
}

fun ViewInteraction.checkHasChildViewWithText(
    pos: Int,
    text: String,
    target: Int
) : ViewInteraction {
    return check(matches(HasChildViewWithText(pos, text, target)))
}

fun ViewInteraction.checkHasViewGroupChildWithText(
    pos: Int,
    text: String
) : ViewInteraction {
    return check(matches(HasViewGroupChildViewWithText(pos, text)))
}

fun RecyclerViewMatcher.onItem(pos: Int): ViewInteraction {
    return onView(atPosition(pos))
}

fun RecyclerViewMatcher.checkIsRecyclerSize(expected: Int) {
    recyclerViewId.matchView().check(RecyclerViewItemCountAssertion(expected))
}