package com.anytypeio.anytype.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.utils.TestUtils.withRecyclerView
import org.hamcrest.Matchers.not

fun <T : BlockViewHolder> Int.scrollTo(position: Int) {
    onView(withId(this)).perform(RecyclerViewActions.scrollToPosition<T>(position))
}

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

fun ViewInteraction.checkHasMarginStart(dimen: Int, coefficient: Int) {
    check(matches(WithMarginStart(dimen, coefficient)))
}

fun ViewInteraction.checkHasPaddingLeft(dimen: Int, coefficient: Int) {
    check(matches(WithPaddingLeft(dimen, coefficient)))
}

fun ViewInteraction.checkIsSelected() {
    check(matches(ViewMatchers.isSelected()))
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

fun ViewInteraction.checkHasText(resId: Int) {
    check(matches(ViewMatchers.withText(resId)))
}

fun ViewInteraction.checkHasTextColor(color: Int) {
    check(matches(WithTextColor(color)))
}

fun ViewInteraction.checkHasBackgroundColor(color: Int) {
    check(matches(WithBackgroundColor(color)))
}

fun ViewInteraction.checkHasNoBackground() {
    check(matches(WithoutBackgroundColor()))
}

fun ViewInteraction.checkHasChildViewCount(count: Int) : ViewInteraction {
    return check(matches(WithChildViewCount(count)))
}

fun Int.rVMatcher(): RecyclerViewMatcher = RecyclerViewMatcher(this)

fun Int.checkRecyclerItemCount(expected: Int) = matchView().check(RecyclerViewItemCountAssertion(expected))

fun RecyclerViewMatcher.onItemView(pos: Int,target: Int): ViewInteraction {
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