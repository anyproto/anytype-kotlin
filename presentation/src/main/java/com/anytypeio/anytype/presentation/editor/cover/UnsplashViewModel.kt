package com.anytypeio.anytype.presentation.editor.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UnsplashImage
import com.anytypeio.anytype.domain.unsplash.SearchUnsplashImage
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.common.Delegator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class UnsplashViewModel(
    private val search: SearchUnsplashImage,
    private val delegator: Delegator<Action>
) : BaseViewModel() {

    val isCompleted = MutableStateFlow(false)

    private val input = MutableStateFlow("")

    private val query = input.take(1).onCompletion {
        emitAll(
            input.debounce(DEBOUNCE_DURATION).distinctUntilChanged()
        )
    }

    val images = MutableStateFlow<List<UnsplashImage>>(emptyList())
    val isLoading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            query.mapLatest { q ->
                isLoading.value = true
                search(
                    SearchUnsplashImage.Params(
                        query = q
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Error while searching unsplash pictures")
                    },
                    success = {
                        images.value = it
                    }
                ).also {
                    isLoading.value = false
                }
            }.collect()
        }
    }

    fun onImageSelected(ctx: Id, img: UnsplashImage) {
        viewModelScope.launch {
            delegator.delegate(
                Action.SetUnsplashImage(img.id)
            )
            isCompleted.value = true
        }
    }

    fun onQueryChanged(query: String) {
        viewModelScope.launch {
            input.emit(query)
        }
    }

    class Factory(
        private val search: SearchUnsplashImage,
        private val delegator: Delegator<Action>,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UnsplashViewModel(
                search = search,
                delegator = delegator,
            ) as T
        }
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
    }
}