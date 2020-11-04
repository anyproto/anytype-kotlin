package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.presentation.navigation.PageLinkView


sealed class PageSearchView {
    object Init : PageSearchView()
    object Loading : PageSearchView()
    object EmptyPages : PageSearchView()
    data class Success(val pages: List<PageLinkView>) : PageSearchView()
    data class NoResults(val searchText: String) : PageSearchView()
    data class Error(val error: String) : PageSearchView()
}