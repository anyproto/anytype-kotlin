package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.EMPTY_QUERY
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.spaces.AddObjectToSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateObjectOfTypeViewModel(
    private val getObjectTypes: GetObjectTypes,
    private val spaceManager: SpaceManager,
    private val addObjectToSpace: AddObjectToSpace,
) : BaseViewModel() {

    val views = MutableStateFlow<List<SelectTypeView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    private val _objectTypes = mutableListOf<ObjectWrapper.Type>()

    private val query = MutableSharedFlow<String>()

    lateinit var space: Id

    init {
        viewModelScope.launch {
            space = spaceManager.get()
            query.onStart { emit(EMPTY_QUERY) }.flatMapLatest { query ->
                getObjectTypes.stream(
                    GetObjectTypes.Params(
                        sorts = buildList {
                            add(
                                DVSort(
                                    relationKey = Relations.LAST_USED,
                                    type = DVSortType.DESC,
                                    includeTime = true
                                )
                            )
                            add(
                                DVSort(
                                    relationKey = Relations.NAME,
                                    type = DVSortType.ASC
                                )
                            )
                        },
                        filters = ObjectSearchConstants.filterTypes(
                            spaces = buildList {
                                add(space)
                                if (query.isNotEmpty()) {
                                    add(Marketplace.MARKETPLACE_SPACE_ID)
                                }
                            },
                            recommendedLayouts = SupportedLayouts.createObjectLayouts
                        ),
                        keys = ObjectSearchConstants.defaultKeysObjectType,
                        query = query
                    )
                ).filterIsInstance<Resultat.Success<List<ObjectWrapper.Type>>>().map { result ->
                    _objectTypes.clear()
                    _objectTypes.addAll(result.getOrNull() ?: emptyList())
                    val allTypes = (result.getOrNull() ?: emptyList())
                    val (allUserTypes, allLibraryTypes) = allTypes.partition { type ->
                        type.getValue<Id>(Relations.SPACE_ID) == space
                    }
                    val filteredLibraryTypes = allLibraryTypes.filter { type ->
                        allUserTypes.none { it.uniqueKey == type.uniqueKey }
                    }
                    val (groups, objects) = allUserTypes.partition { type ->
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
                                        id = type.id,
                                        typeKey = type.uniqueKey,
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
                                        id = type.id,
                                        typeKey = type.uniqueKey,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty()
                                    )
                                }
                            )
                        }
                        if (filteredLibraryTypes.isNotEmpty()) {
                            add(SelectTypeView.Section.Library)
                            addAll(
                                filteredLibraryTypes.map { type ->
                                    SelectTypeView.Type(
                                        id = type.id,
                                        typeKey = type.uniqueKey,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty(),
                                        isFromLibrary = true
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

    fun onTypeClicked(typeView: SelectTypeView.Type) {
        viewModelScope.launch {
            if (typeView.isFromLibrary) {
                val params = AddObjectToSpace.Params(
                    obj = typeView.id,
                    space = space
                )
                addObjectToSpace.async(params = params).fold(
                    onSuccess = { result ->
                        val struct = result.type
                        val type = struct?.mapToObjectWrapperType()
                        if (type != null) {
                            commands.emit(Command.ShowTypeInstalledToast(type.name.orEmpty()))
                            commands.emit(Command.DispatchObjectType(type))
                        } else {
                            Timber.e("Type is not valid")
                            sendToast("Error while installing type")
                        }

                    },
                    onFailure = {
                        Timber.e(it, "Error while installing type")
                        sendToast("Error while installing type")
                    }
                )
            } else {
                _objectTypes.find { it.id == typeView.id }?.let { type ->
                    commands.emit(Command.DispatchObjectType(type))
                } ?: Timber.e("CreateObjectOfTypeViewModel, Type not found by id")
            }
        }
    }

    class Factory @Inject constructor(
        private val getObjectTypes: GetObjectTypes,
        private val spaceManager: SpaceManager,
        private val addObjectToSpace: AddObjectToSpace
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateObjectOfTypeViewModel(
            getObjectTypes = getObjectTypes,
            spaceManager = spaceManager,
            addObjectToSpace = addObjectToSpace
        ) as T
    }
}

sealed class SelectTypeView {
    sealed class Section : SelectTypeView() {
        object Objects : Section()
        object Groups : Section()
        object Library : Section()
    }

    data class Type(
        val id: Id,
        val typeKey: Key,
        val name: String,
        val icon: String,
        val isFromLibrary: Boolean = false
    ) : SelectTypeView()
}

sealed class Command {
    data class ShowTypeInstalledToast(val typeName: String) : Command()
    data class DispatchObjectType(val type: ObjectWrapper.Type) : Command()
}