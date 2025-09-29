package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.date.DateFormatter
import com.anytypeio.anytype.core_utils.ext.readableFileSize
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.publishing.GetWebPublishingList
import com.anytypeio.anytype.domain.publishing.GetPublishingDomain
import com.anytypeio.anytype.domain.publishing.RemovePublishing
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.splash.SplashViewModel.Companion.SPACE_LOADING_TIMEOUT
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

class MySitesViewModel(
    private val vmParams: VmParams,
    private val getWebPublishingList: GetWebPublishingList,
    private val getPublishingDomain: GetPublishingDomain,
    private val removePublishing: RemovePublishing,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val dateFormatter: DateFormatter,
    private val spaceManager: SpaceManager
) : BaseViewModel() {
    private val _viewState = MutableStateFlow<MySitesViewState>(MySitesViewState.Init)
    val viewState = _viewState.asStateFlow()

    val commands = MutableSharedFlow<Command>()

    init {
        viewModelScope.launch {
            proceedWithLoadingPages()
        }
    }

    private suspend fun proceedWithLoadingPages() {
        _viewState.value = MySitesViewState.Loading
        getWebPublishingList.async(
            params = GetWebPublishingList.Params(space = null)
        ).onFailure {
            Timber.e(it, "Failed to load web publishing list")
        }.onSuccess { result ->
            _viewState.value = MySitesViewState.Content(
                result.map { data ->
                    val wrapper = ObjectWrapper.Basic(data.details)
                    MySitesViewState.Item(
                        obj = data.obj,
                        space = data.space,
                        name = wrapper.name.orEmpty(),
                        size = data.size.readableFileSize(),
                        icon = wrapper.objectIcon(
                            builder = urlBuilder,
                            objType = null
                        ),
                        timestamp = dateFormatter.format(
                            millis = data.timestamp * 1000L
                        ),
                        uri = data.uri
                    )
                }
            )
        }
    }

    fun onCopyWebLink(item: MySitesViewState.Item) {
        viewModelScope.launch {
            getPublishingDomain.async(
                params = GetPublishingDomain.Params(space = item.space)
            ).onFailure { error ->
                Timber.e(error, "Failed to get publishing domain for: ${item.obj}")
                commands.emit(Command.ShowToast("Failed to copy link: ${error.message}"))
            }.onSuccess { domain ->
                if (domain != null) {
                    val fullUrl = buildUrl(domain = domain, uri = item.uri)
                    commands.emit(Command.CopyToClipboard(fullUrl))
                }
            }
        }
    }

    fun onUnpublishClicked(item: MySitesViewState.Item) {
        Timber.d("MySites onUnpublishClicked: ${item.obj}")
        viewModelScope.launch {
            removePublishing.async(
                params = RemovePublishing.Params(
                    space = item.space,
                    objectId = item.obj
                )
            ).onFailure { error ->
                Timber.e(error, "Failed to unpublish site: ${item.name}")
                commands.emit(Command.ShowToast("Failed to unpublish: ${error.message}"))
            }.onSuccess {
                Timber.d("Successfully unpublished: ${item.name}")
                commands.emit(Command.ShowToast("${item.name} unpublished successfully"))
                // Refresh the list to remove the unpublished item
                proceedWithLoadingPages()
            }
        }
    }

    fun onOpenObject(item: MySitesViewState.Item) {
        Timber.d("MySites onOpenObject: ${item.obj}")
        viewModelScope.launch {
            val view = awaitActiveSpaceView(item.space)
            if (view != null) {
                val chatId = if (view.spaceUxType == SpaceUxType.CHAT) view.chatId else null
                spaceManager.set(
                    item.space.id
                ).onSuccess {
                    commands.emit(
                        Command.OpenObject(
                            objectId = item.obj,
                            spaceId = item.space,
                            chatId = chatId
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Failed to open space before navigating to an object from my-sites screen")
                }
            } else {
                commands.emit(Command.ShowToast("Failed to find space for this object"))
            }
        }
    }

    fun onOpenInBrowser(item: MySitesViewState.Item) {
        Timber.d("MySites onOpenInBrowser: ${item.obj}")
        viewModelScope.launch {
            getPublishingDomain.async(
                params = GetPublishingDomain.Params(space = item.space)
            ).onFailure { error ->
                Timber.e(error, "Failed to get publishing domain for: ${item.obj}")
                commands.emit(Command.ShowToast("Failed to open in browser: ${error.message}"))
            }.onSuccess { domain ->
                if (domain != null) {
                    val fullUrl = buildUrl(domain = domain, uri = item.uri)
                    commands.emit(Command.Browse(fullUrl))
                }
            }
        }
    }

    private suspend fun awaitActiveSpaceView(space: SpaceId) = withTimeoutOrNull(
        SPACE_LOADING_TIMEOUT
    ) {
        spaceViews
            .observe(space)
            .onEach { view ->
                Timber.i(
                    "Observing space view for ${space.id}, isActive: ${view.isActive}, spaceUxType: ${view.spaceUxType}"
                )
            }
            .filter { view -> view.isActive }
            .take(1)
            .catch {
                Timber.w(it, "Error while observing space view for ${space.id}")
            }
            .firstOrNull()
    }

    class Factory @Inject constructor(
        private val params: VmParams,
        private val getWebPublishingList: GetWebPublishingList,
        private val getPublishingDomain: GetPublishingDomain,
        private val removePublishing: RemovePublishing,
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val dateFormatter: DateFormatter,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MySitesViewModel(
                vmParams = params,
                getWebPublishingList = getWebPublishingList,
                getPublishingDomain = getPublishingDomain,
                removePublishing = removePublishing,
                spaceViews = spaceViews,
                urlBuilder = urlBuilder,
                spaceManager = spaceManager,
                dateFormatter = dateFormatter
            ) as T
        }
    }

    data object VmParams

    sealed class Command {
        data class Browse(val url: String) : Command()
        data class OpenObject(val objectId: Id, val spaceId: SpaceId, val chatId: Id?) : Command()
        data class ShowToast(val message: String) : Command()
        data class CopyToClipboard(val text: String) : Command()
    }
}

sealed class MySitesViewState {
    data object Init : MySitesViewState()
    data object Loading : MySitesViewState()
    data class Content(
        val items: List<Item>
    ) : MySitesViewState()

    data class Item(
        val obj: Id,
        val space: SpaceId,
        val name: String,
        val size: String,
        val icon: ObjectIcon,
        val timestamp: String,
        val uri: String
    )
}

private fun buildUrl(domain: String, uri: String): String =
    "https://$domain/${uri.removePrefix("/")}"
