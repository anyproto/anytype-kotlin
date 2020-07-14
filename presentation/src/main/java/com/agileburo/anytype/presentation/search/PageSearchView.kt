package com.agileburo.anytype.presentation.search

import com.agileburo.anytype.core_ui.features.navigation.PageLinkView

sealed class PageSearchView {
    object Init : PageSearchView()
    object Loading : PageSearchView()
    object EmptyPages : PageSearchView()
    data class Success(val pages: List<PageLinkView>) : PageSearchView()
    data class NoResults(val searchText: String) : PageSearchView()
    data class Error(val error: String) : PageSearchView()
}