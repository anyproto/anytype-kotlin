package com.anytypeio.anytype.presentation.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

class AddToAnytypeViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    fun onCreateBookmark(url: String) {
        viewModelScope.launch {
            createBookmarkObject(
                CreateBookmarkObject.Params(
                    space = spaceManager.get(),
                    url = url
                )
            )
        }
    }

    class Factory @Inject constructor(
        private val createBookmarkObject: CreateBookmarkObject,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddToAnytypeViewModel(
                createBookmarkObject = createBookmarkObject,
                spaceManager = spaceManager
            ) as T
        }
    }
}