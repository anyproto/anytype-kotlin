package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetDocSearchEngineToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.afterTextChanges
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.editorActionEvents
import com.anytypeio.anytype.core_utils.ext.focusAndShowKeyboard
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import kotlinx.coroutines.flow.*
import com.anytypeio.anytype.presentation.editor.editor.search.SearchInDocEvent as Event

class SearchToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = WidgetDocSearchEngineToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
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

    fun events(): Flow<Event> = with(binding) {
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
        binding.docSearchInputField.focusAndShowKeyboard()
    }

    private fun clearSearchInput() {
        binding.docSearchInputField.setText("")
    }
}