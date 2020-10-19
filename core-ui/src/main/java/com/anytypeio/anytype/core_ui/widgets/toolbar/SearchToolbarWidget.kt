package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.afterTextChanges
import com.anytypeio.anytype.core_ui.reactive.clicks
import kotlinx.android.synthetic.main.widget_doc_search_engine_toolbar.view.*
import kotlinx.coroutines.flow.*

class SearchToolbarWidget : ConstraintLayout {

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    fun events(): Flow<Event> {
        val next = docSearchNextSearchResult.clicks().map { Event.Next }
        val previous = docSearchPreviousSearchResult.clicks().map { Event.Previous }
        val cancel = docSearchCancelButton.clicks().map { Event.Cancel }
        val clear = docSearchClearIcon.clicks().onEach { clearSearchInput() }.map { Event.Clear }
        val queries = docSearchInputField.afterTextChanges().map { Event.Query(it.toString()) }
        return flowOf(cancel, clear, queries, next, previous).flattenMerge()
    }

    fun clear() {
        clearSearchInput()
    }

    private fun clearSearchInput() {
        docSearchInputField.setText("")
    }

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_doc_search_engine_toolbar, this)
    }

    sealed class Event {
        object Clear : Event()
        object Cancel : Event()
        object Next : Event()
        object Previous : Event()
        data class Query(val query: String) : Event()
    }

    companion object
}