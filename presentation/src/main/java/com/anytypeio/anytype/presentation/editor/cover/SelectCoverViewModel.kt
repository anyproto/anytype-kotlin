package com.anytypeio.anytype.presentation.editor.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.cover.GetCoverGradientCollection
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverColor
import com.anytypeio.anytype.domain.cover.SetDocCoverGradient
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveCoverEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSetCoverEvent
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
    private val getCoverGradientCollection: GetCoverGradientCollection,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val views = MutableStateFlow<List<DocCoverGalleryView>>(emptyList())
    val isDismissed = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)

    init {
        render()
    }

    private fun render() {
        views.value = buildList {
            add(DocCoverGalleryView.Section.Gradient)
            addAll(
                getCoverGradientCollection.provide().map {
                    DocCoverGalleryView.Gradient(it)
                }
            )
            add(DocCoverGalleryView.Section.Color)
            addAll(
                CoverColor.values().map {
                    DocCoverGalleryView.Color(it)
                }
            )
        }
    }

    fun onImagePicked(ctx: Id, path: String) {
        viewModelScope.launch {
            isLoading.emit(true)
            setCoverImage(
                SetDocCoverImage.Params.FromPath(
                    context = ctx,
                    path = path,
                    // TODO use space provided in arguments
                    space = SpaceId(spaceManager.get())
                )
            ).proceed(
                failure = {
                    isLoading.emit(false)
                    sendToast("Error while setting doc cover image, ${it.message}")
                    Timber.e(it, "Error while setting doc cover image")
                },
                success = {
                    isLoading.emit(false)
                    sendAnalyticsSetCoverEvent(analytics)
                    dispatcher.send(it)
                    isDismissed.emit(true)
                }
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
                failure = {
                    sendToast("Error while setting doc cover image, ${it.message}")
                    Timber.e(it, "Error while setting doc cover image")
                },
                success = {
                    sendAnalyticsSetCoverEvent(analytics)
                    dispatcher.send(it)
                    isDismissed.emit(true)
                }
            )
        }
    }

    fun onRemoveCover(ctx: Id) {
        viewModelScope.launch {
            removeCover(
                RemoveDocCover.Params(ctx)
            ).process(
                success = {
                    sendAnalyticsRemoveCoverEvent(analytics)
                    dispatcher.send(it)
                    isDismissed.emit(true)
                },
                failure = {
                    sendToast("Error while removing cover image, ${it.message}")
                    Timber.e(it, "Error while removing doc cover image")
                }
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
                failure = {
                    sendToast("Error while updating document's cover color, ${it.message}")
                    Timber.e(it, "Error while updating document's cover color")
                },
                success = {
                    sendAnalyticsSetCoverEvent(analytics)
                    dispatcher.send(it)
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
                failure = {
                    sendToast("Error while updating document's cover gradient, ${it.message}")
                    Timber.e(it, "Error while updating document's cover gradient")
                },
                success = {
                    sendAnalyticsSetCoverEvent(analytics)
                    dispatcher.send(it)
                    isDismissed.emit(true)
                }
            )
        }
    }
}

class SelectCoverObjectViewModel(
    private val setCoverImage: SetDocCoverImage,
    private val setCoverColor: SetDocCoverColor,
    private val setCoverGradient: SetDocCoverGradient,
    private val removeCover: RemoveDocCover,
    private val dispatcher: Dispatcher<Payload>,
    private val getCoverGradientCollection: GetCoverGradientCollection,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager
) : SelectCoverViewModel(
    setCoverColor = setCoverColor,
    setCoverImage = setCoverImage,
    setCoverGradient = setCoverGradient,
    removeCover = removeCover,
    dispatcher = dispatcher,
    getCoverGradientCollection = getCoverGradientCollection,
    analytics = analytics,
    spaceManager = spaceManager
) {

    class Factory(
        private val setCoverImage: SetDocCoverImage,
        private val setCoverColor: SetDocCoverColor,
        private val setCoverGradient: SetDocCoverGradient,
        private val removeCover: RemoveDocCover,
        private val dispatcher: Dispatcher<Payload>,
        private val getCoverGradientCollection: GetCoverGradientCollection,
        private val analytics: Analytics,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectCoverObjectViewModel(
                setCoverColor = setCoverColor,
                setCoverImage = setCoverImage,
                setCoverGradient = setCoverGradient,
                removeCover = removeCover,
                dispatcher = dispatcher,
                getCoverGradientCollection = getCoverGradientCollection,
                analytics = analytics,
                spaceManager = spaceManager
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
    private val getCoverGradientCollection: GetCoverGradientCollection,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager
) : SelectCoverViewModel(
    setCoverColor = setCoverColor,
    setCoverImage = setCoverImage,
    setCoverGradient = setCoverGradient,
    removeCover = removeCover,
    dispatcher = dispatcher,
    getCoverGradientCollection = getCoverGradientCollection,
    analytics = analytics,
    spaceManager = spaceManager
) {

    class Factory(
        private val setCoverImage: SetDocCoverImage,
        private val setCoverColor: SetDocCoverColor,
        private val setCoverGradient: SetDocCoverGradient,
        private val removeCover: RemoveDocCover,
        private val dispatcher: Dispatcher<Payload>,
        private val getCoverGradientCollection: GetCoverGradientCollection,
        private val analytics: Analytics,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectCoverObjectSetViewModel(
                setCoverColor = setCoverColor,
                setCoverImage = setCoverImage,
                setCoverGradient = setCoverGradient,
                removeCover = removeCover,
                dispatcher = dispatcher,
                getCoverGradientCollection = getCoverGradientCollection,
                analytics = analytics,
                spaceManager = spaceManager
            ) as T
        }
    }
}