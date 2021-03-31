package com.anytypeio.anytype.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.`is`


class RecyclerViewItemCountAssertion(val expected: Int) : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) throw noViewFoundException
        val recycler = view as RecyclerView
        val adapter = recycler.adapter
        checkNotNull(adapter) { "Adapter wasn't set" }
        assertThat(adapter.itemCount, `is`(expected))
    }
}