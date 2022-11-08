package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.toObjectTypeView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectTypeChangeViewModel(
    private val getCompatibleObjectTypes: GetCompatibleObjectTypes
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val views = MutableStateFlow<List<ObjectTypeView.Item>>(emptyList())

    val results = combine(searchQuery, views) { query, views ->
        if (query.isEmpty())
            views
        else
            views.filter { view -> view.name.contains(query, true) }
    }

    fun onStart(
        smartBlockType: SmartBlockType,
        excludedTypes: List<Id> = emptyList(),
        isDraft: Boolean,
        selectedSources: List<Id>,
        isSetSource: Boolean
    ) {
        viewModelScope.launch {
            getCompatibleObjectTypes.invoke(
                GetCompatibleObjectTypes.Params(
                    smartBlockType = smartBlockType,
                    isSetIncluded = if (isSetSource) true else isDraft
                )
            ).proceed(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { types ->
                    if (excludedTypes.isEmpty()) {
                        setViews(
                            objectTypes = types,
                            selectedSources = selectedSources
                        )
                    } else {
                        setViews(
                            objectTypes = types.filter { !excludedTypes.contains(it.url) },
                            selectedSources = selectedSources
                        )
                    }
                }
            )
        }
    }

    private fun setViews(objectTypes: List<ObjectType>, selectedSources: List<Id>) {
        views.value = objectTypes
            .toObjectTypeView(selectedSources = selectedSources)
            .sortedBy { !selectedSources.contains(it.id) }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }
}