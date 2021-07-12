package com.anytypeio.anytype.presentation.`object`

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectCoverPickerViewModel(
    private val removeDocCover: RemoveDocCover,
    private val setDocCoverImage: SetDocCoverImage,
    private val dispatcher: Dispatcher<Payload>
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)

    fun onImagePicked(ctx: Id, path: String) {
        if (path.endsWith(PageViewModel.FORMAT_WEBP, true)) {
            Timber.d("onDocCoverImagePicked, not allowed to add WEBP1 format")
            return
        }
        viewModelScope.launch {
            setDocCoverImage(
                SetDocCoverImage.Params.FromPath(
                    context = ctx,
                    path = path
                )
            ).proceed(
                failure = { Timber.e(it, "Error while setting doc cover image") },
                success = { dispatcher.send(it).also { isDismissed.value = true } }
            )
        }
    }

    fun onImageSelected(ctx: Id, hash: String) {
        viewModelScope.launch {
            setDocCoverImage(
                SetDocCoverImage.Params.FromHash(
                    context = ctx,
                    hash = hash
                )
            ).proceed(
                failure = { Timber.e(it, "Error while setting doc cover image") },
                success = { dispatcher.send(it).also { isDismissed.value = true } }
            )
        }
    }

    fun onRemoveCover(ctx: Id) {
        viewModelScope.launch {
            removeDocCover(
                RemoveDocCover.Params(ctx)
            ).process(
                success = { dispatcher.send(it).also { isDismissed.value = true } },
                failure = {}
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val removeDocCover: RemoveDocCover,
        private val setDocCoverImage: SetDocCoverImage,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectCoverPickerViewModel(
                removeDocCover = removeDocCover,
                setDocCoverImage = setDocCoverImage,
                dispatcher = dispatcher
            ) as T
        }
    }
}