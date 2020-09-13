package com.agileburo.anytype.presentation.linking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.timber
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.interactor.CreateLinkToObject
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.agileburo.anytype.presentation.mapper.toEmojiView
import com.agileburo.anytype.presentation.mapper.toImageView
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.PageNavigationView
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LinkToObjectViewModel(
    private val urlBuilder: UrlBuilder,
    private val getPageInfoWithLinks: GetPageInfoWithLinks,
    private val createLinkToObject: CreateLinkToObject,
    private val getConfig: GetConfig
) : ViewStateViewModel<ViewState<PageNavigationView>>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private var pageId: String = ""
    private var home: Id = ""

    val isLinkingDisabled: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun onStart(initialTarget: Id) {
        viewModelScope.launch {
            getConfig(Unit).proceed(
                failure = { Timber.e(it, "Error while getting config") },
                success = { config ->
                    home = config.home
                    proceedWithGettingDocumentLinks(initialTarget)
                }
            )
        }
    }

    private fun proceedWithGettingDocumentLinks(target: String) {
        stateData.postValue(ViewState.Loading)
        viewModelScope.launch {
            getPageInfoWithLinks.invoke(GetPageInfoWithLinks.Params(pageId = target)).proceed(
                failure = { error ->
                    error.timber()
                    stateData.postValue(ViewState.Error(error.message ?: "Unknown error"))
                },
                success = { response ->
                    with(response.pageInfoWithLinks) {
                        pageId = this.id
                        stateData.postValue(
                            ViewState.Success(
                                PageNavigationView(
                                    title = pageInfo.fields.name.orEmpty(),
                                    subtitle = pageInfo.snippet.orEmpty(),
                                    image = pageInfo.fields.toImageView(urlBuilder),
                                    emoji = pageInfo.fields.toEmojiView(),
                                    inbound = links.inbound.map { it.toView(urlBuilder) },
                                    outbound = links.outbound.map { it.toView(urlBuilder) }
                                )
                            )
                        )
                    }
                }
            )
        }
    }

    fun onLinkClicked(
        target: Id,
        context: Id
    ) {
        isLinkingDisabled.value = (target == context || target == home)
        proceedWithGettingDocumentLinks(target)
    }

    fun onLinkToObjectClicked(
        context: Id,
        target: Id,
        replace: Boolean,
        position: Position
    ) {
        viewModelScope.launch {
            createLinkToObject(
                CreateLinkToObject.Params(
                    context = context,
                    target = target,
                    block = pageId,
                    replace = replace,
                    position = position
                )
            ).proceed(
                failure = { Timber.e(it, "Error while creating link to object") },
                success = { navigate(EventWrapper(AppNavigation.Command.Exit)) }
            )
        }
    }
}