package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.publishing.GetWebPublishingList
import com.anytypeio.anytype.domain.publishing.GetPublishingDomain
import com.anytypeio.anytype.domain.publishing.RemovePublishing
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class MySitesViewModel(
    private val vmParams: VmParams,
    private val getWebPublishingList: GetWebPublishingList,
    private val getPublishingDomain: GetPublishingDomain,
    private val removePublishing: RemovePublishing,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder
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
        getWebPublishingList.async(
            params = GetWebPublishingList.Params(space = null)
        ).onFailure {

        }.onSuccess { result ->
            _viewState.value = MySitesViewState.Content(
                result.map { data ->
                    val wrapper = ObjectWrapper.Basic(data.details)
                    MySitesViewState.Item(
                        obj = data.obj,
                        space = data.space,
                        name = wrapper.name.orEmpty(),
                        size = data.size.toString(),
                        icon = wrapper.objectIcon(
                            builder = urlBuilder,
                            objType = null
                        ),
                        timestamp = data.timestamp.toString(),
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
                Timber.e(error, "Failed to get publishing domain for: ${item.name}")
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
        Timber.d("MySites onUnpublishClicked: ${item.name}")
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
        Timber.d("MySites onOpenObject: ${item.name}")
        viewModelScope.launch {
            commands.emit(Command.OpenObject(objectId = item.obj, spaceId = item.space))
        }
    }

    fun onOpenInBrowser(item: MySitesViewState.Item) {
        Timber.d("MySites onOpenInBrowser: ${item.name}")
        viewModelScope.launch {
            getPublishingDomain.async(
                params = GetPublishingDomain.Params(space = item.space)
            ).onFailure { error ->
                Timber.e(error, "Failed to get publishing domain for: ${item.name}")
                commands.emit(Command.ShowToast("Failed to open in browser: ${error.message}"))
            }.onSuccess { domain ->
                if (domain != null) {
                    val fullUrl = buildUrl(domain = domain, uri = item.uri)
                    commands.emit(Command.Browse(fullUrl))
                }
            }
        }
    }

    class Factory @Inject constructor(
        private val params: VmParams,
        private val getWebPublishingList: GetWebPublishingList,
        private val getPublishingDomain: GetPublishingDomain,
        private val removePublishing: RemovePublishing,
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MySitesViewModel(
                vmParams = params,
                getWebPublishingList = getWebPublishingList,
                getPublishingDomain = getPublishingDomain,
                removePublishing = removePublishing,
                searchObjects = searchObjects,
                urlBuilder = urlBuilder
            ) as T
        }
    }

    data object VmParams

    sealed class Command {
        data class Browse(val url: String) : Command()
        data class OpenObject(val objectId: Id, val spaceId: SpaceId) : Command()
        data class ShowToast(val message: String) : Command()
        data class CopyToClipboard(val text: String) : Command()
    }
}

sealed class MySitesViewState {
    data object Init : MySitesViewState()

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
