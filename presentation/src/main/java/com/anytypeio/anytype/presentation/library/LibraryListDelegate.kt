package com.anytypeio.anytype.presentation.library

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.navigation.LibraryView
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

interface LibraryListDelegate {

    val queryFlow: MutableStateFlow<String>
    val itemsFlow: Flow<LibraryScreenState.Tabs.TabData>

    fun itemsFlow(): Flow<List<ObjectWrapper.Basic>>

    @FlowPreview
    fun queryFlow() = queryFlow
        .debounce(DEBOUNCE_TIMEOUT)
        .distinctUntilChanged()

}

fun List<LibraryView>.filterByQuery(query: String): List<LibraryView> {
    return filter { it.name.contains(query.trim(), true) }
}

private const val DEBOUNCE_TIMEOUT = 100L