package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.toObjectTypeView
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

    fun onStart(smartBlockType: SmartBlockType) {
        viewModelScope.launch {
            getCompatibleObjectTypes.invoke(
                GetCompatibleObjectTypes.Params(
                    smartBlockType = smartBlockType
                )
            ).proceed(
                failure = { Timber.e(it, "Error while getting object types") },
                success = { setViews(it) }
            )
        }
    }

    private fun setViews(objectTypes: List<ObjectType>) {
        views.value = objectTypes.toObjectTypeView()
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }
}