package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.block.interactor.CreateLinkToObject
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.anytypeio.anytype.presentation.mapper.toEmojiView
import com.anytypeio.anytype.presentation.mapper.toImageView
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.PageNavigationView
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
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
                                    title = documentInfo.fields.name.orEmpty(),
                                    subtitle = documentInfo.snippet.orEmpty(),
                                    image = documentInfo.fields.toImageView(urlBuilder),
                                    emoji = documentInfo.fields.toEmojiView(),
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