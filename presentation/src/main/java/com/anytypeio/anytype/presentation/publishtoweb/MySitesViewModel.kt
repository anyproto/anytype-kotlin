package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.publishing.GetWebPublishingList
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch

class MySitesViewModel(
    private val vmParams: VmParams,
    private val getWebPublishingList: GetWebPublishingList,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    private val _viewState = MutableStateFlow<MySitesViewState>(MySitesViewState.Init)
    val viewState = _viewState.asStateFlow()

    val commands = MutableSharedFlow<Command>()

    init {
        viewModelScope.launch {
            getWebPublishingList.async(
                params = GetWebPublishingList.Params(space = null)
            ).onFailure {

            }.onSuccess { result ->
                _viewState.value = MySitesViewState.Content(
                    result.map { data ->
                        val wrapper = ObjectWrapper.Basic(data.details)
                        MySitesViewState.Item(
                            name = wrapper.name.orEmpty(),
                            size = data.size.toString(),
                            icon = wrapper.objectIcon(
                                builder = urlBuilder,
                                objType = null
                            ),
                            timestamp = data.timestamp.toString()
                        )
                    }
                )
            }
        }
    }

    class Factory @Inject constructor(
        private val params: VmParams,
        private val getWebPublishingList: GetWebPublishingList,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MySitesViewModel(
                vmParams = params,
                getWebPublishingList = getWebPublishingList,
                urlBuilder = urlBuilder
            ) as T
        }
    }

    data object VmParams

    sealed class Command {
        // TODO: Add commands when needed
    }
}

sealed class MySitesViewState {
    data object Init : MySitesViewState()

    data class Content(
        val items: List<Item>
    ) : MySitesViewState()

    data class Item(
        val name: String,
        val size: String,
        val icon: ObjectIcon,
        val timestamp: String
    )
}