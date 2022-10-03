package com.anytypeio.anytype.test_utils.utils.view_action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher

class ExtractViewAction <V: View> : ViewAction {

    lateinit var view: V

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(View::class.java)
    }

    override fun getDescription(): String {
        return "Text of the view"
    }

    override fun perform(uiController: UiController, view: View) {
        this.view = view as V
    }
}

inline fun <reified V: View> ViewInteraction.extractView(): V {
    val viewAction = ExtractViewAction<V>()
    perform(viewAction)
    return viewAction.view
}
