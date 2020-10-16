package com.anytypeio.anytype.sample.search

import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.sample.R
import kotlinx.android.synthetic.main.activity_search_on_page.*

class SearchOnPageActivity : AppCompatActivity(R.layout.activity_search_on_page) {

    private val items = mutableListOf<SearchOnPageAdapter.Item>().apply {
        repeat(10) {
            add(
                SearchOnPageAdapter.Item(
                    id = it,
                    txt = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                )
            )
        }
    }

    private val mockAdapter = SearchOnPageAdapter(
        items = items
    )

    override fun onStart() {
        super.onStart()
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mockAdapter
        }
        search.doOnTextChanged { text, start, before, count ->

        }
    }
}