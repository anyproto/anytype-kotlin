package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CreateObjectOfTypeViewModel(
    private val getObjectTypes: GetObjectTypes,
    private val spaceManager: SpaceManager
) : BaseViewModel() {

    val views = MutableStateFlow<List<SelectTypeView>>(emptyList())

    private val query = MutableSharedFlow<String>()

    init {
        viewModelScope.launch {
            query.onStart { emit("") }.flatMapLatest { query ->
                getObjectTypes.stream(
                    GetObjectTypes.Params(
                        sorts = emptyList(),
                        filters = buildList {
                            addAll(
                                ObjectSearchConstants.filterObjectTypeLibrary(
                                    space = spaceManager.get()
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.RECOMMENDED_LAYOUT,
                                    condition = DVFilterCondition.IN,
                                    value = SupportedLayouts.createObjectLayouts.map {
                                        it.code.toDouble()
                                    }
                                )
                            )
                        },
                        keys = ObjectSearchConstants.defaultKeysObjectType,
                        query = query
                    )
                ).map { result ->
                    val types = result.getOrNull() ?: emptyList()
                    val (groups, objects) = types.partition { type ->
                        type.uniqueKey == ObjectTypeUniqueKeys.SET || type.uniqueKey == ObjectTypeUniqueKeys.COLLECTION
                    }
                    buildList {
                        if (groups.isNotEmpty()) {
                            add(
                                SelectTypeView.Section.Groups
                            )
                            addAll(
                                groups.map { type ->
                                    SelectTypeView.Type(
                                        typeKey = type.uniqueKey!!,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty()
                                    )
                                }
                            )
                        }
                        if (objects.isNotEmpty()) {
                            add(
                                SelectTypeView.Section.Objects
                            )
                            addAll(
                                objects.map { type ->
                                    SelectTypeView.Type(
                                        typeKey = type.uniqueKey!!,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty()
                                    )
                                }
                            )
                        }
                    }
                }
            }.collect {
                views.value = it
            }
        }
    }

    fun onQueryChanged(input: String) {
        viewModelScope.launch {
            query.emit(input)
        }
    }

    class Factory @Inject constructor(
        private val getObjectTypes: GetObjectTypes,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateObjectOfTypeViewModel(
            getObjectTypes = getObjectTypes,
            spaceManager = spaceManager
        ) as T
    }
}

sealed class SelectTypeView {
    sealed class Section : SelectTypeView() {
        object Objects: Section()
        object Groups: Section()
    }
    data class Type(
//        val typeId: String,
        val typeKey: String,
        val name: String,
        val icon: String,
    ): SelectTypeView()
}