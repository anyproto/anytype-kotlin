package com.anytypeio.anytype.presentation.navigation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.anytypeio.anytype.presentation.mapper.toEmojiView
import com.anytypeio.anytype.presentation.mapper.toImageView
import com.anytypeio.anytype.presentation.mapper.toView
import kotlinx.coroutines.launch
import timber.log.Timber

class PageNavigationViewModel(
    private val urlBuilder: UrlBuilder,
    private val getPageInfoWithLinks: GetPageInfoWithLinks,
    private val getConfig: GetConfig,
    private val analytics: Analytics
) :
    ViewStateViewModel<ViewState<PageNavigationView>>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private var pageId: String = ""
    private var homeId = ""

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        proceedWithGettingConfig()
        stateData.postValue(ViewState.Init)
    }

    fun onGetPageLinks(target: String) {
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

    fun onPageLinkClick(target: String) {
        onGetPageLinks(target)
    }

    fun onOpenPageClicked() {
        if (pageId == homeId) {
            viewModelScope.sendEvent(
                analytics = analytics,
                eventName = EventsDictionary.SCREEN_DASHBOARD
            )
            navigate(EventWrapper(AppNavigation.Command.ExitToDesktop))
        } else {
            viewModelScope.sendEvent(
                analytics = analytics,
                eventName = EventsDictionary.SCREEN_DOCUMENT
            )
            navigate(EventWrapper(AppNavigation.Command.ExitToDesktopAndOpenPage(pageId = pageId)))
        }
    }

    private fun proceedWithGettingConfig() {
        getConfig(viewModelScope, Unit) { result ->
            result.either(
                fnR = { config -> setHomeId(id = config.home) },
                fnL = { Timber.e(it, "Error while getting config") }
            )
        }
    }

    private fun setHomeId(id: String) {
        homeId = id
    }
}
