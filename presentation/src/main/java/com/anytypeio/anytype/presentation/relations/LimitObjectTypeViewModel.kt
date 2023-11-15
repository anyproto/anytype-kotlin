package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.EMPTY_QUERY
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectTypeView
import com.anytypeio.anytype.presentation.relations.model.SelectLimitObjectTypeView
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

class LimitObjectTypeViewModel(
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val state: StateHolder<CreateFromScratchState>,
    private val spaceManager: SpaceManager
) : BaseViewModel() {


    private val query = MutableStateFlow(EMPTY_QUERY)
    private val types = MutableStateFlow<List<SelectLimitObjectTypeView>>(emptyList())

    val views = combine(types, query) { t, q ->
        if (q.isEmpty())
            t
        else
            t.filter { type ->
                val title = type.item.title
                val subtitle = type.item.subtitle
                if (subtitle.isNullOrEmpty())
                    title.contains(q, true)
                else
                    title.contains(q, true) || subtitle.contains(q, true)
            }
    }

    val count = MutableStateFlow(0)

    val isDismissed = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            searchObjects(
                SearchObjects.Params(
                    filters = listOf(
                        DVFilter(
                            relation = Relations.SPACE_ID,
                            condition = DVFilterCondition.EQUAL,
                            value = spaceManager.get()
                        ),
                        DVFilter(
                            relation = Relations.LAYOUT,
                            condition = DVFilterCondition.EQUAL,
                            value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                        ),
                        DVFilter(
                            relation = Relations.IS_ARCHIVED,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        ),
                        DVFilter(
                            relation = Relations.IS_DELETED,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        ),
                        DVFilter(
                            relation = Relations.IS_HIDDEN,
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
                    this@LimitObjectTypeViewModel.types.value = types.map { t ->
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
                failure = {
                    Timber.e(it, "Error while searching for types")
                }
            )
        }
    }

    fun onSearchInputChanged(input: String) {
        query.value = input
    }

    fun onObjectTypeClicked(type: SelectLimitObjectTypeView) {
        types.value = types.value.map { view ->
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
            limitObjectTypes = types.value.filter { it.isSelected }.map { it.item }
        )
        isDismissed.value = true
    }

    class Factory(
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder,
        private val state: StateHolder<CreateFromScratchState>,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LimitObjectTypeViewModel(
                searchObjects = searchObjects,
                urlBuilder = urlBuilder,
                state = state,
                spaceManager = spaceManager
            ) as T
        }
    }
}