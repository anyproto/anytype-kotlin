package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectTypeView
import com.anytypeio.anytype.presentation.relations.model.SelectLimitObjectTypeView
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LimitObjectTypeViewModel(
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val state: StateHolder<CreateFromScratchState>
) : BaseViewModel() {

    val views = MutableStateFlow<List<SelectLimitObjectTypeView>>(emptyList())

    val count = MutableStateFlow(0)

    val isDismissed = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            searchObjects(
                SearchObjects.Params(
                    filters = listOf(
                        DVFilter(
                            relationKey = Relations.TYPE,
                            condition = DVFilterCondition.EQUAL,
                            value = ObjectTypeIds.OBJECT_TYPE
                        ),
                        DVFilter(
                            relationKey = Relations.IS_ARCHIVED,
                            condition = DVFilterCondition.EQUAL,
                            value = false
                        ),
                        DVFilter(
                            relationKey = Relations.IS_HIDDEN,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        )
                    ),
                    keys = listOf(
                        Relations.ID,
                        Relations.NAME,
                        Relations.SNIPPET,
                        Relations.DESCRIPTION,
                        Relations.ICON_EMOJI,
                        Relations.ICON_IMAGE,
                        Relations.LAYOUT
                    )
                )
            ).process(
                success = { types ->
                    val limitObjectTypes = state.state.value.limitObjectTypes
                    views.value = types.map { t ->
                        SelectLimitObjectTypeView(
                            item = DefaultObjectTypeView(
                                id = t.id,
                                title = t.name.orEmpty(),
                                subtitle = t.description,
                                icon = ObjectIcon.from(
                                    obj = t,
                                    layout = t.layout,
                                    builder = urlBuilder
                                )
                            ),
                            isSelected = limitObjectTypes.map { it.id }.contains(t.id)
                        )
                    }.also {
                        count.value = it.count { view -> view.isSelected }
                    }
                },
                failure = {}
            )
        }
    }

    fun onObjectTypeClicked(type: SelectLimitObjectTypeView) {
        views.value = views.value.map { view ->
            if (view.item.id == type.item.id)
                view.copy(
                    isSelected = !view.isSelected
                )
            else
                view
        }.also {
            count.value = it.count { view -> view.isSelected }
        }
    }

    fun onAddClicked() {
        state.state.value = state.state.value.copy(
            limitObjectTypes = views.value.filter { it.isSelected }.map { it.item }
        )
        isDismissed.value = true
    }

    class Factory(
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder,
        private val state: StateHolder<CreateFromScratchState>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LimitObjectTypeViewModel(
                searchObjects = searchObjects,
                urlBuilder = urlBuilder,
                state = state
            ) as T
        }
    }
}