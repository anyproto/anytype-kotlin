package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.EMPTY_QUERY
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Marketplace
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
            val space = spaceManager.get()
            query.onStart { emit(EMPTY_QUERY) }.flatMapLatest { query ->
                getObjectTypes.stream(
                    GetObjectTypes.Params(
                        sorts = emptyList(),
                        filters = ObjectSearchConstants.filterTypes(
                            spaces = buildList {
                                add(space)
                                if (query.isNotEmpty()) add(Marketplace.MARKETPLACE_SPACE_ID)
                            },
                            recommendedLayouts = SupportedLayouts.createObjectLayouts
                        ),
                        keys = ObjectSearchConstants.defaultKeysObjectType,
                        query = query
                    )
                ).map { result ->

                    val allTypes = (result.getOrNull() ?: emptyList())
                    val (userTypes, libraryTypes) = allTypes.partition { type ->
                        type.getValue<Id>(Relations.SPACE_ID) == space
                    }
                    val (groups, objects) = userTypes.partition { type ->
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
                        if (libraryTypes.isNotEmpty()) {
                            add(
                                SelectTypeView.Section.Library
                            )
                            addAll(
                                libraryTypes.map { type ->
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
        object Library: Section()
    }
    data class Type(
        val typeKey: String,
        val name: String,
        val icon: String,
    ): SelectTypeView()
}