package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.publishing.CreatePublishing
import com.anytypeio.anytype.domain.publishing.GetPublishingDomain
import com.anytypeio.anytype.domain.publishing.GetPublishingState
import com.anytypeio.anytype.domain.publishing.RemovePublishing
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class PublishToWebViewModel(
    private val vmParams: Params,
    private val getPublishingDomain: GetPublishingDomain,
    private val publish: CreatePublishing,
    private val getPublishingState: GetPublishingState,
    private val removePublishing: RemovePublishing,
    private val searchObjects: SearchObjects
) : BaseViewModel() {

    private val _viewState = MutableStateFlow<PublishToWebViewState>(PublishToWebViewState.Init)
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            getPublishingState.async(
                params = GetPublishingState.Params(
                    space = vmParams.space,
                    objectId = vmParams.ctx
                )
            ).onFailure {
                Timber.e(it, "DROID-3786 Failed to get publishing status")
            }.onSuccess { state ->
                if (state != null) {
                    // TODO
                } else {
                    val domain = getPublishingDomain.async(
                        params = GetPublishingDomain.Params(space = vmParams.space)
                    ).getOrNull()
                    if (domain != null) {
                        _viewState.value = PublishToWebViewState.NotPublished(
                            domain = domain,
                            uri = "test"
                        )
                    } else {
                        // TODO
                    }
                }
            }
        }
    }



    fun onPublishClicked(uri: String) {
        Timber.d("DROID-3786 onPublishClicked")
        proceedWithPublishing(uri = uri)
    }

    fun onUpdateClicked(uri: String) {
        Timber.d("DROID-3786 onUpdateClicked")
        proceedWithPublishing(uri = uri)
    }

    fun onUnpublishClicked(uri: String) {
        Timber.d("DROID-3786 onUnpublishClicked")
        proceedWithUnpublishing()
    }

    private fun proceedWithPublishing(uri: String) {
        viewModelScope.launch {
            publish.async(
                params = CreatePublishing.Params(
                    space = vmParams.space,
                    objectId = vmParams.ctx,
                    uri = uri
                )
            ).onFailure {
                Timber.e(it, "DROID-3786 Failed to publish!")
            }.onSuccess {
                Timber.d("DROID-3786 Published: $it")
            }
        }
    }

    private fun proceedWithUnpublishing() {
        viewModelScope.launch {
            removePublishing.async(
                params = RemovePublishing.Params(
                    space = vmParams.space,
                    objectId = vmParams.ctx
                )
            ).onFailure {
                Timber.e(it, "DROID-3786 Failed to unpublish!")
            }.onSuccess {
                Timber.d("DROID-3786 Unpublished object: $vmParams")
            }
        }
    }

    class Factory @Inject constructor(
        private val publish: CreatePublishing,
        private val getStatus: GetPublishingState,
        private val getPublishingDomain: GetPublishingDomain,
        private val removePublishing: RemovePublishing,
        private val searchObjects: SearchObjects,
        private val params: Params,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PublishToWebViewModel(
                vmParams = params,
                getPublishingDomain = getPublishingDomain,
                publish = publish,
                getPublishingState = getStatus,
                removePublishing = removePublishing,
                searchObjects = searchObjects
            ) as T
        }
    }

    data class Params(
        val ctx: Id,
        val space: SpaceId
    )
}

sealed class PublishToWebViewState {

    abstract val uri: String
    abstract val domain: String

    data  object Init : PublishToWebViewState() {
        override val uri: String
            get() = ""
        override val domain: String
            get() = ""
    }

    data class NotPublished(
        override val domain: String,
        override val uri: String,
    ) : PublishToWebViewState()

    data class Published(
        override val domain: String,
        override val uri: String,
    ) : PublishToWebViewState()

    data class Publishing(
        override val domain: String,
        override val uri: String,
    ) : PublishToWebViewState()
}
