package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.scan
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

    val commands = MutableSharedFlow<Command>()

    init {
        proceedWithResolvingInitialState()
    }

    private fun proceedWithResolvingInitialState() {
        viewModelScope.launch {
            getPublishingState.async(
                params = GetPublishingState.Params(
                    space = vmParams.space,
                    objectId = vmParams.ctx
                )
            ).onFailure {
                Timber.e(it, "DROID-3786 Failed to get publishing status")
            }.onSuccess { state ->

                val domain = getPublishingDomain.async(
                    params = GetPublishingDomain.Params(space = vmParams.space)
                ).getOrNull()

                if (state == null) {
                    _viewState.value = PublishToWebViewState.NotPublished(
                        domain = domain.orEmpty(),
                        uri = resolveSuggestedUri().orEmpty()
                    )
                } else {

                    val uri = resolveSuggestedUri()

                    Timber.d("DROID-3786 Resolved uri: $uri")

                    if (domain != null) {
                        _viewState.value = PublishToWebViewState.Published(
                            domain = domain,
                            uri = state.uri
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
        _viewState.value = PublishToWebViewState.Publishing(
            domain = viewState.value.domain,
            uri = viewState.value.uri
        )
        proceedWithPublishing(uri = uri)
    }

    fun onUpdateClicked(uri: String) {
        Timber.d("DROID-3786 onUpdateClicked")
        _viewState.value = PublishToWebViewState.Publishing(
            domain = viewState.value.domain,
            uri = viewState.value.uri
        )
        proceedWithUpdating(uri = uri)
    }

    fun onUnpublishClicked(uri: String) {
        Timber.d("DROID-3786 onUnpublishClicked")
        proceedWithUnpublishing()
    }

    fun onPreviewClicked() {
        Timber.d("DROID-3786 onPreviewClicked")
        viewModelScope.launch {
            when(val state = viewState.value) {
                is PublishToWebViewState.FailedToPublish -> TODO()
                is PublishToWebViewState.FailedToUpdate -> TODO()
                PublishToWebViewState.Init -> TODO()
                is PublishToWebViewState.NotPublished -> TODO()
                is PublishToWebViewState.Published -> {
                    commands.emit(
                        Command.Browse(
                            "https://" + state.domain + "/" + state.uri
                        )
                    )
                }
                is PublishToWebViewState.Publishing -> TODO()
                else -> {
                    Timber.w("Unexpected state")
                }
            }
        }
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
                _viewState.value = PublishToWebViewState.FailedToPublish(
                    domain = viewState.value.domain,
                    uri = viewState.value.uri,
                    err = it.message.orEmpty()
                )
            }.onSuccess { uri ->
                proceedWithResolvingInitialState()
            }
        }
    }

    private fun proceedWithUpdating(uri: String) {
        viewModelScope.launch {
            publish.async(
                params = CreatePublishing.Params(
                    space = vmParams.space,
                    objectId = vmParams.ctx,
                    uri = uri
                )
            ).onFailure {
                Timber.e(it, "DROID-3786 Failed to publish!")
                _viewState.value = PublishToWebViewState.FailedToUpdate(
                    domain = viewState.value.domain,
                    uri = viewState.value.uri,
                    err = it.message.orEmpty()
                )
            }.onSuccess {
                proceedWithResolvingInitialState()
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
                proceedWithResolvingInitialState()
            }
        }
    }

    private suspend fun resolveSuggestedUri() : String? {
        return searchObjects(
            SearchObjects.Params(
                space = vmParams.space,
                limit = 1,
                filters = listOf(
                    DVFilter(
                        value = vmParams.ctx,
                        condition = DVFilterCondition.EQUAL,
                        relation = Relations.ID
                    )
                )
            )
        ).let { either ->
            either.getOrNull().orEmpty().firstOrNull()?.name?.toWebSlug()
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

    sealed class Command {
        data class Browse(val url: String): Command()
    }
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
        override val uri: String
    ) : PublishToWebViewState()

    data class FailedToPublish(
        override val domain: String,
        override val uri: String,
        val err: String
    ) : PublishToWebViewState()

    data class FailedToUpdate(
        override val domain: String,
        override val uri: String,
        val err: String
    ) : PublishToWebViewState()
}

fun String.toWebSlug(): String =
    trim()
        .lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("\\s+"), "-")
        .replace(Regex("-+"), "-")
        .trim('-')

private fun normalizeUri(uri: String): String =
    uri.trim().removePrefix("/")

private fun buildUrl(domain: String, uri: String): String =
    "https://$domain/${normalizeUri(uri)}"