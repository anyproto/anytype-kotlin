package com.anytypeio.anytype.test_utils.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers

class RecyclerViewItemCountAssertion(val expected: Int) : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) throw noViewFoundException
        val recycler = view as RecyclerView
        val adapter = recycler.adapter
        checkNotNull(adapter) { "Adapter wasn't set" }
        ViewMatchers.assertThat(adapter.itemCount, CoreMatchers.`is`(expected))
    }
}