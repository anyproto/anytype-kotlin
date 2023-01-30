package com.anytypeio.anytype.presentation.library

import com.anytypeio.anytype.core_models.ObjectWrapper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

interface LibraryListDelegate {

    val queryFlow: MutableStateFlow<String>
    val itemsFlow: Flow<LibraryScreenState.Tabs.TabData>

    fun itemsFlow(): Flow<List<ObjectWrapper>>

    @FlowPreview
    fun queryFlow() = queryFlow
        .debounce(DEBOUNCE_TIMEOUT)
        .distinctUntilChanged()

}

fun List<ObjectWrapper>.filterByQuery(query: String): List<ObjectWrapper> {
    return filter {
        when (it) {
            is ObjectWrapper.Basic -> {
                it.name?.contains(query.trim(), ignoreCase = true) == true
            }
            is ObjectWrapper.Type -> {
                it.name?.contains(query.trim(), ignoreCase = true) == true
            }
            is ObjectWrapper.Relation -> {
                it.name?.contains(query.trim(), ignoreCase = true) == true
            }
            is ObjectWrapper.Bookmark -> {
                it.name?.contains(query.trim(), ignoreCase = true) == true
            }
        }
    }
}

private const val DEBOUNCE_TIMEOUT = 100L