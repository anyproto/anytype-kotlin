package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class ObjectTypeChangeViewModel(
    private val storeOfObjectTypes: StoreOfObjectTypes
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val views = MutableStateFlow<List<ObjectTypeView>>(emptyList())

    val results = combine(searchQuery, views) { query, views ->
        if (query.isEmpty())
            views
        else
            views.filter { view -> view.name.contains(query, true) }
    }

    fun onStart(
        isWithSet: Boolean,
        isWithBookmark: Boolean,
        excludeTypes: List<Id>,
        selectedTypes: List<Id>,
        isSetSource: Boolean
    ) {
        viewModelScope.launch {
            val all = storeOfObjectTypes.getAll()
            val objectTypeViews = all.getObjectTypeViewsForSBPage(
                isWithSet = isWithSet,
                isWithBookmark = isWithBookmark,
                selectedTypes = selectedTypes,
                excludeTypes = excludeTypes
            )
            setViews(objectTypeViews)
        }
    }

    private fun setViews(objectTypeViews: List<ObjectTypeView>) {
        views.value = objectTypeViews
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }
}