package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.afterTextChanges
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import kotlinx.android.synthetic.main.widget_doc_search_engine_toolbar.view.*
import kotlinx.coroutines.flow.*
import com.anytypeio.anytype.presentation.editor.editor.search.SearchInDocEvent as Event

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
        setupFocusListener()
    }

    private fun setupFocusListener() {
//        docSearchInputField.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                Timber.d("View has focus!")
//                context.imm().showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
//            }
//        }
    }

    fun events(): Flow<Event> {
        val next = docSearchNextSearchResult.clicks().map { Event.Next }
        val previous = docSearchPreviousSearchResult.clicks().map { Event.Previous }
        val cancel = docSearchCancelButton.clicks().map { Event.Cancel }
        val clear = docSearchClearIcon.clicks().onEach { clearSearchInput() }.map { Event.Clear }
        val queries = docSearchInputField.afterTextChanges().map { Event.Query(it.toString()) }
        val action = docSearchInputField
            .editorActionEvents { (it == EditorInfo.IME_ACTION_SEARCH) }
            .onEach {
                docSearchInputField.clearFocus()
                docSearchInputField.hideKeyboard()
            }
            .map { Event.Search }

        return flowOf(cancel, clear, queries, next, previous, action).flattenMerge()
    }

    fun clear() {
        clearSearchInput()
    }

    fun focus() {
        docSearchInputField.focusAndShowKeyboard()
    }

    private fun clearSearchInput() {
        docSearchInputField.setText("")
    }

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_doc_search_engine_toolbar, this)
    }

    companion object
}