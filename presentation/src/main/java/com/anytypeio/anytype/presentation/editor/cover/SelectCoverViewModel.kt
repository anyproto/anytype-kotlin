package com.anytypeio.anytype.presentation.editor.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.cover.*
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class SelectCoverViewModel(
    private val setCoverImage: SetDocCoverImage,
    private val setCoverColor: SetDocCoverColor,
    private val setCoverGradient: SetDocCoverGradient,
    private val removeCover: RemoveDocCover,
    private val dispatcher: Dispatcher<Payload>,
    private val getCoverGradientCollection: GetCoverGradientCollection
) : ViewModel() {

    val views = MutableStateFlow<List<DocCoverGalleryView>>(emptyList())
    val isDismissed = MutableStateFlow(false)

    init {
        render()
    }

    private fun render() {
        val result = mutableListOf<DocCoverGalleryView>()
        result.add(DocCoverGalleryView.Section.Color)
        result.addAll(
            CoverColor.values().map {
                DocCoverGalleryView.Color(it)
            }
        )
        result.add(DocCoverGalleryView.Section.Gradient)
        result.addAll(
            getCoverGradientCollection.provide().map {
                DocCoverGalleryView.Gradient(it)
            }
        )
        views.value = result.toList()
    }

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

class SelectCoverObjectViewModel(
    private val setCoverImage: SetDocCoverImage,
    private val setCoverColor: SetDocCoverColor,
    private val setCoverGradient: SetDocCoverGradient,
    private val removeCover: RemoveDocCover,
    private val dispatcher: Dispatcher<Payload>,
    private val details: DetailModificationManager,
    private val getCoverGradientCollection: GetCoverGradientCollection
) : SelectCoverViewModel(
    setCoverColor = setCoverColor,
    setCoverImage = setCoverImage,
    setCoverGradient = setCoverGradient,
    removeCover = removeCover,
    dispatcher = dispatcher,
    getCoverGradientCollection = getCoverGradientCollection
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
        private val details: DetailModificationManager,
        private val getCoverGradientCollection: GetCoverGradientCollection
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectCoverObjectViewModel(
                setCoverColor = setCoverColor,
                setCoverImage = setCoverImage,
                setCoverGradient = setCoverGradient,
                removeCover = removeCover,
                dispatcher = dispatcher,
                details = details,
                getCoverGradientCollection = getCoverGradientCollection
            ) as T
        }
    }
}

class SelectCoverObjectSetViewModel(
    private val setCoverImage: SetDocCoverImage,
    private val setCoverColor: SetDocCoverColor,
    private val setCoverGradient: SetDocCoverGradient,
    private val removeCover: RemoveDocCover,
    private val dispatcher: Dispatcher<Payload>,
    private val getCoverGradientCollection: GetCoverGradientCollection
) : SelectCoverViewModel(
    setCoverColor = setCoverColor,
    setCoverImage = setCoverImage,
    setCoverGradient = setCoverGradient,
    removeCover = removeCover,
    dispatcher = dispatcher,
    getCoverGradientCollection = getCoverGradientCollection
) {

    override fun onDetailsColor(ctx: Id, color: CoverColor) {}
    override fun onDetailsGradient(ctx: Id, gradient: String) {}

    class Factory(
        private val setCoverImage: SetDocCoverImage,
        private val setCoverColor: SetDocCoverColor,
        private val setCoverGradient: SetDocCoverGradient,
        private val removeCover: RemoveDocCover,
        private val dispatcher: Dispatcher<Payload>,
        private val getCoverGradientCollection: GetCoverGradientCollection
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectCoverObjectSetViewModel(
                setCoverColor = setCoverColor,
                setCoverImage = setCoverImage,
                setCoverGradient = setCoverGradient,
                removeCover = removeCover,
                dispatcher = dispatcher,
                getCoverGradientCollection = getCoverGradientCollection
            ) as T
        }
    }
}