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
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetObjectInfoWithLinks
import com.anytypeio.anytype.presentation.mapper.getEmojiPath
import com.anytypeio.anytype.presentation.mapper.getImagePath
import com.anytypeio.anytype.presentation.mapper.toView
import kotlinx.coroutines.launch
import timber.log.Timber

class PageNavigationViewModel(
    private val urlBuilder: UrlBuilder,
    private val getObjectInfoWithLinks: GetObjectInfoWithLinks,
    private val getConfig: GetConfig,
    private val analytics: Analytics,
    private val objectTypesProvider: ObjectTypesProvider
) : ViewStateViewModel<ViewState<PageNavigationView>>(),
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
            getObjectInfoWithLinks.invoke(GetObjectInfoWithLinks.Params(pageId = target)).proceed(
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
                                    title = documentInfo.obj.name.orEmpty(),
                                    subtitle = documentInfo.snippet.orEmpty(),
                                    image = documentInfo.obj.getImagePath(urlBuilder),
                                    emoji = documentInfo.obj.getEmojiPath(),
                                    inbound = links.inbound.map { it.toView(urlBuilder, objectTypesProvider.get()) },
                                    outbound = links.outbound.map { it.toView(urlBuilder, objectTypesProvider.get()) }
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
            navigate(EventWrapper(AppNavigation.Command.ExitToDesktop))
        } else {
            navigate(EventWrapper(AppNavigation.Command.LaunchDocument(pageId)))
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
