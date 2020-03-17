package com.agileburo.anytype.presentation.page.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.page.bookmark.SetupBookmark
import com.agileburo.anytype.presentation.page.bookmark.CreateBookmarkViewModel.ViewState

class CreateBookmarkViewModel(
    private val setupBookmark: SetupBookmark
) : ViewStateViewModel<ViewState>() {

    fun onCreateBookmarkClicked(
        context: String,
        target: String,
        url: String
    ) {
        setupBookmark.invoke(
            scope = viewModelScope,
            params = SetupBookmark.Params(
                context = context,
                target = target,
                url = url
            )
        ) { result ->
            result.either(
                fnL = { update(ViewState.Error(it.message ?: toString())) },
                fnR = { update(ViewState.Exit) }
            )
        }
    }

    sealed class ViewState {
        data class Error(val message: String) : ViewState()
        object Exit : ViewState()
    }

    class Factory(
        private val setupBookmark: SetupBookmark
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = CreateBookmarkViewModel(
            setupBookmark = setupBookmark
        ) as T
    }
}