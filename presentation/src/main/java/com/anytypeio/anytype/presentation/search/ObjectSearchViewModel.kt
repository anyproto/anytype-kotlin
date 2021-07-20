package com.anytypeio.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.navigation.filterBy
import kotlinx.coroutines.launch

class ObjectSearchViewModel(
    private val urlBuilder: UrlBuilder,
    private val getListPages: GetListPages,
    private val analytics: Analytics
) : ViewStateViewModel<ObjectSearchView>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private var links: MutableList<ObjectView> = mutableListOf()

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        links.clear()
        stateData.postValue(ObjectSearchView.Init)
    }

    fun onGetPageList(searchText: String) {
        stateData.postValue(ObjectSearchView.Loading)
        viewModelScope.launch {
            getListPages(Unit).proceed(
                failure = {
                    it.timber()
                    stateData.postValue(ObjectSearchView.Error(it.message ?: "Unknown error"))
                },
                success = { response ->
                    links.addAll(response.listPages.map { it.toView(urlBuilder) })
                    proceedWithResults(
                        original = links,
                        filter = searchText
                    )
                }
            )
        }
    }

    fun onSearchTextChanged(searchText: String) {
        proceedWithResults(original = links, filter = searchText)
    }

    private fun proceedWithResults(original: List<ObjectView>, filter: String) {
        val query = filter.trim()
        val filtered = original.filterBy(query)
        if (filtered.isNotEmpty()) {
            stateData.postValue(ObjectSearchView.Success(pages = filtered))
        } else {
            if (query.isEmpty())
                stateData.postValue(ObjectSearchView.EmptyPages)
            else
                stateData.postValue(ObjectSearchView.NoResults(query))
        }
    }

    fun onOpenPageClicked(pageId: String) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DOCUMENT
        )
        navigate(EventWrapper(AppNavigation.Command.LaunchDocument(id = pageId)))
    }

    fun onBottomSheetHidden() {
        navigateToDesktop()
    }

    private fun navigateToDesktop() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DASHBOARD
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop))
    }
}