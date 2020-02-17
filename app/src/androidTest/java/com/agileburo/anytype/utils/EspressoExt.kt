package com.agileburo.anytype.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.utils.TestUtils.withRecyclerView

fun <T : BlockViewHolder> Int.scrollTo(position: Int) {
    onView(withId(this)).perform(RecyclerViewActions.scrollToPosition<T>(position))
}

fun Int.findItemAt(position: Int, layoutId: Int): ViewInteraction {
    return onView(withRecyclerView(this).atPositionOnView(position, layoutId))
}