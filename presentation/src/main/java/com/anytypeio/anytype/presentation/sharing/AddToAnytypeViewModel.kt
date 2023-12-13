package com.anytypeio.anytype.presentation.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AddToAnytypeViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val createPrefilledNote: CreatePrefilledNote,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val navigation = MutableSharedFlow<OpenObjectNavigation>()

    fun onCreateBookmark(url: String) {
        viewModelScope.launch {
            createBookmarkObject(
                CreateBookmarkObject.Params(
                    space = spaceManager.get(),
                    url = url
                )
            ).process(
                success = { obj ->
                    navigation.emit(OpenObjectNavigation.OpenEditor(obj))
                },
                failure = {
                    Timber.d(it, "Error while creating bookmark")
                    sendToast("Error while creating bookmark: ${it.msg()}")
                }
            )
        }
    }

    fun onCreateNote(text: String) {
        viewModelScope.launch {
            createPrefilledNote.async(
                CreatePrefilledNote.Params(text)
            ).fold(
                onSuccess = { result ->
                    navigation.emit(OpenObjectNavigation.OpenEditor(result))
                },
                onFailure = {
                    Timber.d(it, "Error while creating note")
                    sendToast("Error while creating note: ${it.msg()}")
                }
            )
        }
    }

    class Factory @Inject constructor(
        private val createBookmarkObject: CreateBookmarkObject,
        private val createPrefilledNote: CreatePrefilledNote,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddToAnytypeViewModel(
                createBookmarkObject = createBookmarkObject,
                spaceManager = spaceManager,
                createPrefilledNote = createPrefilledNote
            ) as T
        }
    }
}