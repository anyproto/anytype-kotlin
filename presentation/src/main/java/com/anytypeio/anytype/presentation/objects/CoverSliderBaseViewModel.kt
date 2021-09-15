package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverColor
import com.anytypeio.anytype.domain.cover.SetDocCoverGradient
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class CoverSliderBaseViewModel(
    private val setCoverImage: SetDocCoverImage,
    private val setCoverColor: SetDocCoverColor,
    private val setCoverGradient: SetDocCoverGradient,
    private val removeCover: RemoveDocCover,
    private val dispatcher: Dispatcher<Payload>
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)

    fun onImagePicked(ctx: Id, path: String) {
        if (path.endsWith(EditorViewModel.FORMAT_WEBP, true)) {
            Timber.d("onDocCoverImagePicked, not allowed to add WEBP1 format")
            return
        }
        viewModelScope.launch {
            setCoverImage(
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
            setCoverImage(
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
            removeCover(
                RemoveDocCover.Params(ctx)
            ).process(
                success = { dispatcher.send(it).also { isDismissed.value = true } },
                failure = {}
            )
        }
    }

    fun onSolidColorSelected(
        ctx: Id,
        color: CoverColor
    ) {
        viewModelScope.launch {
            setCoverColor(
                SetDocCoverColor.Params(
                    ctx = ctx,
                    color = color.code
                )
            ).proceed(
                failure = { Timber.e(it, "Error while updating document's cover color") },
                success = {
                    dispatcher.send(it)
                    onDetailsColor(ctx, color)
                    isDismissed.emit(true)
                }
            )
        }
    }

    fun onGradientColorSelected(ctx: Id, gradient: String) {
        viewModelScope.launch {
            setCoverGradient(
                SetDocCoverGradient.Params(
                    ctx = ctx,
                    gradient = gradient
                )
            ).proceed(
                failure = { Timber.e(it, "Error while updating document's cover gradient") },
                success = {
                    dispatcher.send(it)
                    onDetailsGradient(ctx, gradient)
                    isDismissed.emit(true)
                }
            )
        }
    }

    abstract fun onDetailsColor(ctx: Id, color: CoverColor)
    abstract fun onDetailsGradient(ctx: Id, gradient: String)
}

class CoverSliderObjectViewModel(
    private val setCoverImage: SetDocCoverImage,
    private val setCoverColor: SetDocCoverColor,
    private val setCoverGradient: SetDocCoverGradient,
    private val removeCover: RemoveDocCover,
    private val dispatcher: Dispatcher<Payload>,
    private val details: DetailModificationManager
) : CoverSliderBaseViewModel(
    setCoverColor = setCoverColor,
    setCoverImage = setCoverImage,
    setCoverGradient = setCoverGradient,
    removeCover = removeCover,
    dispatcher = dispatcher
) {

    override fun onDetailsColor(ctx: Id, color: CoverColor) {
        viewModelScope.launch {
            details.setDocCoverColor(
                target = ctx,
                color = color.code
            )
        }
    }

    override fun onDetailsGradient(ctx: Id, gradient: String) {
        viewModelScope.launch {
            details.setDocCoverGradient(
                target = ctx,
                gradient = gradient
            )
        }
    }

    class Factory(
        private val setCoverImage: SetDocCoverImage,
        private val setCoverColor: SetDocCoverColor,
        private val setCoverGradient: SetDocCoverGradient,
        private val removeCover: RemoveDocCover,
        private val dispatcher: Dispatcher<Payload>,
        private val details: DetailModificationManager
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CoverSliderObjectViewModel(
                setCoverColor = setCoverColor,
                setCoverImage = setCoverImage,
                setCoverGradient = setCoverGradient,
                removeCover = removeCover,
                dispatcher = dispatcher,
                details = details
            ) as T
        }
    }
}

class CoverSliderObjectSetViewModel(
    private val setCoverImage: SetDocCoverImage,
    private val setCoverColor: SetDocCoverColor,
    private val setCoverGradient: SetDocCoverGradient,
    private val removeCover: RemoveDocCover,
    private val dispatcher: Dispatcher<Payload>
) : CoverSliderBaseViewModel(
    setCoverColor = setCoverColor,
    setCoverImage = setCoverImage,
    setCoverGradient = setCoverGradient,
    removeCover = removeCover,
    dispatcher = dispatcher
) {

    override fun onDetailsColor(ctx: Id, color: CoverColor) {}
    override fun onDetailsGradient(ctx: Id, gradient: String) {}

    class Factory(
        private val setCoverImage: SetDocCoverImage,
        private val setCoverColor: SetDocCoverColor,
        private val setCoverGradient: SetDocCoverGradient,
        private val removeCover: RemoveDocCover,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CoverSliderObjectSetViewModel(
                setCoverColor = setCoverColor,
                setCoverImage = setCoverImage,
                setCoverGradient = setCoverGradient,
                removeCover = removeCover,
                dispatcher = dispatcher
            ) as T
        }
    }
}