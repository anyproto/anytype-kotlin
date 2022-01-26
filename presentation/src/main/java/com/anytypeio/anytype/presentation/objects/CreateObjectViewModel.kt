package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.page.CreatePage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateObjectViewModel(private val createPage: CreatePage) : ViewModel(){

    val createObjectStatus = MutableSharedFlow<State>(replay = 0)

    fun onStart(type: String) {
        onCreatePage(type)
    }

    private fun onCreatePage(type: String) {
        val params = CreatePage.Params(
            ctx = null,
            isDraft = true,
            type = type,
            emoji = null
        )
        createPage.invoke(viewModelScope, params) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while creating a new page") },
                fnR = { id ->
                    viewModelScope.launch {
                        createObjectStatus.emit(State.Success(id))
                    }
                }
            )
        }
    }

    sealed class State {
        data class Success(val id: String): State()
        data class Error(val msg: String): State()
    }

    class Factory(
        private val createPage: CreatePage
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateObjectViewModel(createPage = createPage) as T
        }
    }
}