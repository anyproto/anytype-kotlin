package com.anytypeio.anytype.presentation.`object`

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectTypeChangeViewModel(
    private val getCompatibleObjectTypes: GetCompatibleObjectTypes
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val views = MutableStateFlow<List<ObjectTypeView.Item>>(emptyList())

    val results = combine(searchQuery, views) { query, views ->
        if (query.isEmpty())
            views
        else
            views.filter { view -> view.name.contains(query, true) }
    }

    init {
        viewModelScope.launch {
            getCompatibleObjectTypes.invoke(
                GetCompatibleObjectTypes.Params(
                    smartBlockType = SmartBlockType.PAGE
                )
            ).proceed(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { objectTypes ->
                    views.emit(toObjectTypeViews(objectTypes = objectTypes))
                }
            )
        }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    private fun toObjectTypeViews(objectTypes: List<ObjectType>): List<ObjectTypeView.Item> {
        return objectTypes.map { objectType ->
            ObjectTypeView.Item(
                id = objectType.url,
                name = objectType.name,
                emoji = objectType.emoji,
                description = objectType.description
            )
        }
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }
}