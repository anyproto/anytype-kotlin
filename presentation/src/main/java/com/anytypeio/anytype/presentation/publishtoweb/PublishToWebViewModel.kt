package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.publishing.Publishing
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.publishing.CreatePublishing
import com.anytypeio.anytype.domain.publishing.GetPublishingDomain
import com.anytypeio.anytype.domain.publishing.GetPublishingStatus
import com.anytypeio.anytype.domain.publishing.RemovePublishing
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class PublishToWebViewModel(
    private val vmParams: Params,
    private val getPublishingDomain: GetPublishingDomain,
    private val publish: CreatePublishing,
    private val getStatus: GetPublishingStatus,
    private val removePublishing: RemovePublishing,
    private val searchObjects: SearchObjects
) : BaseViewModel() {

    init {

        viewModelScope.launch {

            val domain = getPublishingDomain.async(
                params = GetPublishingDomain.Params(
                    space = vmParams.space
                )
            ).onFailure {
                Timber.e(it, "DROID-3786 Failed to get publishing domain")
            }.getOrNull()

            getStatus.async(
                params = GetPublishingStatus.Params(
                    space = vmParams.space,
                    objectId = vmParams.ctx
                )
            ).onFailure {
                Timber.e(it, "DROID-3786 Failed to get publishing status")
            }.onSuccess {
                Timber.d("DROID-3786 Publishing status: $it")
            }
        }
    }



    fun onPublishClicked(uri: String) {
        Timber.d("DROID-3786 onPublishClicked: $uri")
        proceedWithPublishing(uri = uri)
    }

    fun onUpdateClicked(uri: String) {
        Timber.d("DROID-3786 onUpdateClicked: $uri")
        proceedWithPublishing(uri = uri)
    }

    fun onUnpublishClicked() {
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
        private val getStatus: GetPublishingStatus,
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
                getStatus = getStatus,
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
    data  object Idle : PublishToWebViewState()

    data class Published(
        val domain: String,
        val uri: String
    ) : PublishToWebViewState()

    data class Publishing(
        val domain: String,
        val uri: String
    ) : PublishToWebViewState()

    data object Error : PublishToWebViewState()
}
