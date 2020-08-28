package com.agileburo.anytype.presentation.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.features.navigation.PageLinkView
import com.agileburo.anytype.core_ui.features.navigation.filterBy
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.timber
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetListPages
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.launch

class PageSearchViewModel(
    private val urlBuilder: UrlBuilder,
    private val getListPages: GetListPages
) : ViewStateViewModel<PageSearchView>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private var links: MutableList<PageLinkView> = mutableListOf()

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(PageSearchView.Init)
    }

    fun onGetPageList(searchText: String) {
        stateData.postValue(PageSearchView.Loading)
        viewModelScope.launch {
            getListPages(Unit).proceed(
                failure = {
                    it.timber()
                    stateData.postValue(PageSearchView.Error(it.message ?: "Unknown error"))
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

    private fun proceedWithResults(original: List<PageLinkView>, filter: String) {
        val query = filter.trim()
        val filtered = original.filterBy(query)
        if (filtered.isNotEmpty()) {
            stateData.postValue(PageSearchView.Success(pages = filtered))
        } else {
            if (query.isEmpty())
                stateData.postValue(PageSearchView.EmptyPages)
            else
                stateData.postValue(PageSearchView.NoResults(query))
        }
    }

    fun onOpenPageClicked(pageId: String) {
        navigate(EventWrapper(AppNavigation.Command.ExitToDesktopAndOpenPage(pageId = pageId)))
    }
}