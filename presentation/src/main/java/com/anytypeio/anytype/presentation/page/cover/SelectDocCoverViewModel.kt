package com.anytypeio.anytype.presentation.page.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.cover.*
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.util.Bridge
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectDocCoverViewModel(
    private val setDocCoverColor: SetDocCoverColor,
    private val setDocCoverGradient: SetDocCoverGradient,
    private val payloadDispatcher: Bridge<Payload>,
    private val getCoverImageCollection: GetCoverImageCollection,
    private val getCoverGradientCollection: GetCoverGradientCollection,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    val views = MutableStateFlow<List<DocCoverGalleryView>>(emptyList())
    val isDismissed = MutableSharedFlow<Boolean>()

    init {
        viewModelScope.launch {
            getCoverImageCollection(Unit).proceed(
                failure = { Timber.e(it, "Error while getting cover collection") },
                success = { images -> render(images) }
            )
        }
    }

    private fun render(images: List<CoverImage>) {
        val result = mutableListOf<DocCoverGalleryView>()
        result.add(DocCoverGalleryView.Section.Color)
        result.addAll(
            CoverColor.values().map {
                DocCoverGalleryView.Color(it)
            }
        )
        val grouped = images.groupBy { it.group }
        grouped.forEach { (group, img) ->
            result.add(DocCoverGalleryView.Section.Collection(group))
            result.addAll(
                img.map {
                    DocCoverGalleryView.Image(url = urlBuilder.thumbnail(it.hash), hash = it.hash)
                }
            )
        }
        result.add(DocCoverGalleryView.Section.Gradient)
        result.addAll(
            getCoverGradientCollection.provide().map {
                DocCoverGalleryView.Gradient(it)
            }
        )
        views.value = result.toList()
    }

    fun onSolidColorSelected(
        ctx: Id,
        color: CoverColor
    ) {
        viewModelScope.launch {
            setDocCoverColor(
                SetDocCoverColor.Params(
                    ctx = ctx,
                    color = color.code
                )
            ).proceed(
                failure = { Timber.e(it, "Error while updating document's cover color") },
                success = { payloadDispatcher.send(it).also { isDismissed.emit(true) } }
            )
        }
    }

    fun onGradientColorSelected(ctx: Id, gradient: String) {
        viewModelScope.launch {
            setDocCoverGradient(
                SetDocCoverGradient.Params(
                    ctx = ctx,
                    gradient = gradient
                )
            ).proceed(
                failure = { Timber.e(it, "Error while updating document's cover gradient") },
                success = { payloadDispatcher.send(it).also { isDismissed.emit(true) } }
            )
        }
    }

    class Factory(
        private val setDocCoverColor: SetDocCoverColor,
        private val setDocCoverGradient: SetDocCoverGradient,
        private val payloadDispatcher: Bridge<Payload>,
        private val getCoverCollection: GetCoverImageCollection,
        private val getCoverGradientCollection: GetCoverGradientCollection,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectDocCoverViewModel(
                setDocCoverColor = setDocCoverColor,
                setDocCoverGradient = setDocCoverGradient,
                payloadDispatcher = payloadDispatcher,
                getCoverImageCollection = getCoverCollection,
                getCoverGradientCollection = getCoverGradientCollection,
                urlBuilder = urlBuilder
            ) as T
        }
    }
}