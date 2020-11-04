package com.anytypeio.anytype.presentation.page.editor.search

sealed class SearchInDocEvent {
    object Clear : SearchInDocEvent()
    object Cancel : SearchInDocEvent()
    object Next : SearchInDocEvent()
    object Search : SearchInDocEvent()
    object Previous : SearchInDocEvent()
    data class Query(val query: String) : SearchInDocEvent()
}