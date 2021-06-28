package com.anytypeio.anytype.presentation.page.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.cover.*
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectDocCoverViewModel(
    private val setDocCoverColor: SetDocCoverColor,
    private val setDocCoverGradient: SetDocCoverGradient,
    private val dispatcher: Dispatcher<Payload>,
    private val getCoverImageCollection: GetCoverImageCollection,
    private val getCoverGradientCollection: GetCoverGradientCollection,
    private val urlBuilder: UrlBuilder,
    private val details: DetailModificationManager
) : ViewModel() {

    val views = MutableStateFlow<List<DocCoverGalleryView>>(emptyList())
    val isDismissed = MutableSharedFlow<Boolean>()

    init {
//        viewModelScope.launch {
//            getCoverImageCollection(Unit).proceed(
//                failure = { Timber.e(it, "Error while getting cover collection") },
//                success = { images -> render(images) }
//            )
//        }
        render(images = listOf())
    }

    private fun render(images: List<CoverImage>) {
        val result = mutableListOf<DocCoverGalleryView>()
        result.add(DocCoverGalleryView.Section.Color)
        result.addAll(
            CoverColor.values().map {
                DocCoverGalleryView.Color(it)
            }
        )
//        val grouped = images.groupBy { it.group }
//        grouped.forEach { (group, img) ->
//            result.add(DocCoverGalleryView.Section.Collection(group))
//            result.addAll(
//                img.map {
//                    DocCoverGalleryView.Image(url = urlBuilder.thumbnail(it.hash), hash = it.hash)
//                }
//            )
//        }
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
                success = {
                    dispatcher.send(it)
                    details.setDocCoverColor(
                        target = ctx,
                        color = color.code
                    )
                    isDismissed.emit(true)
                }
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
                success = {
                    dispatcher.send(it)
                    details.setDocCoverGradient(
                        target = ctx,
                        gradient = gradient
                    )
                    isDismissed.emit(true)
                }
            )
        }
    }

    class Factory(
        private val setDocCoverColor: SetDocCoverColor,
        private val setDocCoverGradient: SetDocCoverGradient,
        private val dispatcher: Dispatcher<Payload>,
        private val getCoverCollection: GetCoverImageCollection,
        private val getCoverGradientCollection: GetCoverGradientCollection,
        private val urlBuilder: UrlBuilder,
        private val details: DetailModificationManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectDocCoverViewModel(
                setDocCoverColor = setDocCoverColor,
                setDocCoverGradient = setDocCoverGradient,
                dispatcher = dispatcher,
                getCoverImageCollection = getCoverCollection,
                getCoverGradientCollection = getCoverGradientCollection,
                urlBuilder = urlBuilder,
                details = details
            ) as T
        }
    }
}