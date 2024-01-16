package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.EMPTY_QUERY
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.spaces.AddObjectToSpace
import com.anytypeio.anytype.domain.types.GetPinnedObjectTypes
import com.anytypeio.anytype.domain.types.SetPinnedObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectObjectTypeViewModel(
    private val params: Params,
    private val getObjectTypes: GetObjectTypes,
    private val spaceManager: SpaceManager,
    private val addObjectToSpace: AddObjectToSpace,
    private val setPinnedObjectTypes: SetPinnedObjectTypes,
    private val getPinnedObjectTypes: GetPinnedObjectTypes
) : BaseViewModel() {

    val viewState = MutableStateFlow<SelectTypeViewState>(SelectTypeViewState.Loading)
    val commands = MutableSharedFlow<Command>()
    private val _objectTypes = mutableListOf<ObjectWrapper.Type>()

    private val query = MutableSharedFlow<String>()

    lateinit var space: Id

    init {
        Timber.d("Params: ${params}")
        viewModelScope.launch {
            space = spaceManager.get()
            query.onStart { emit(EMPTY_QUERY) }.flatMapLatest { query ->
                val types = getObjectTypes.stream(
                    GetObjectTypes.Params(
                        sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                        filters = ObjectSearchConstants.filterTypes(
                            spaces = buildList {
                                add(space)
                                if (query.isNotEmpty()) {
                                    add(Marketplace.MARKETPLACE_SPACE_ID)
                                }
                            },
                            recommendedLayouts = SupportedLayouts.createObjectLayouts,
                            excludedTypeKeys = params.excludedTypeKeys
                        ),
                        keys = ObjectSearchConstants.defaultKeysObjectType,
                        query = query
                    )
                ).filterIsInstance<Resultat.Success<List<ObjectWrapper.Type>>>()

                combine(
                    types,
                    getPinnedObjectTypes.flow(GetPinnedObjectTypes.Params(SpaceId(space)))
                ) { result, pinned ->
                    _objectTypes.clear()
                    _objectTypes.addAll(result.getOrNull() ?: emptyList())

                    val pinnedObjectTypesIds = pinned.map { it.id }

                    val allTypes = (result.getOrNull() ?: emptyList())

                    val pinnedTypes = allTypes
                        .filter { pinnedObjectTypesIds.contains(it.id) }
                        .sortedBy { obj -> pinnedObjectTypesIds.indexOf(obj.id) }

                    val (allUserTypes, allLibraryTypes) = allTypes.partition { type ->
                        type.getValue<Id>(Relations.SPACE_ID) == space
                    }
                    val filteredLibraryTypes = allLibraryTypes.filter { type ->
                        allUserTypes.none { it.uniqueKey == type.uniqueKey }
                    }
                    val (groups, objects) = allUserTypes.partition { type ->
                        type.uniqueKey == ObjectTypeUniqueKeys.SET || type.uniqueKey == ObjectTypeUniqueKeys.COLLECTION
                    }
                    val notPinnedObjects = objects.filter { !pinnedObjectTypesIds.contains(it.id) }
                    buildList {
                        if (pinnedTypes.isNotEmpty()) {
                            add(
                                SelectTypeView.Section.Pinned
                            )
                            addAll(
                                pinnedTypes.mapIndexed { index, type ->
                                    SelectTypeView.Type(
                                        id = type.id,
                                        typeKey = type.uniqueKey,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty(),
                                        isPinned = true,
                                        isFirstInSection = index == 0
                                    )
                                }
                            )
                        }
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
                        if (notPinnedObjects.isNotEmpty()) {
                            add(
                                SelectTypeView.Section.Objects
                            )
                            addAll(
                                notPinnedObjects.map { type ->
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
                val state = if (it.isEmpty()) {
                    SelectTypeViewState.Empty
                } else {
                    SelectTypeViewState.Content(it)
                }
                viewState.value = state
            }
        }
    }

    fun onQueryChanged(input: String) {
        viewModelScope.launch {
            query.emit(input)
        }
    }

    fun onPinTypeClicked(typeView: SelectTypeView.Type) {
        val state = viewState.value
        if (state is SelectTypeViewState.Content) {
            val pinned = buildSet {
                add(TypeId(typeView.id))
                state.views.forEach { view ->
                    if (view is SelectTypeView.Type && view.isPinned)
                        add(TypeId(view.id))
                }
            }
            viewModelScope.launch {
                setPinnedObjectTypes.async(
                    SetPinnedObjectTypes.Params(
                        space = SpaceId(id = space),
                        types = pinned.toList()
                    )
                )
            }
        }
    }

    fun onUnpinTypeClicked(typeView: SelectTypeView.Type) {
        val state = viewState.value
        if (state is SelectTypeViewState.Content) {
            val pinned = buildSet {
                state.views.forEach { view ->
                    if (view is SelectTypeView.Type && view.isPinned && view.id != typeView.id)
                        add(TypeId(view.id))
                }
            }
            viewModelScope.launch {
                setPinnedObjectTypes.async(
                    SetPinnedObjectTypes.Params(
                        space = SpaceId(id = space),
                        types = pinned.toList()
                    )
                )
            }
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
        private val params: Params,
        private val getObjectTypes: GetObjectTypes,
        private val spaceManager: SpaceManager,
        private val addObjectToSpace: AddObjectToSpace,
        private val setPinnedObjectTypes: SetPinnedObjectTypes,
        private val getPinnedObjectTypes: GetPinnedObjectTypes
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SelectObjectTypeViewModel(
            params = params,
            getObjectTypes = getObjectTypes,
            spaceManager = spaceManager,
            addObjectToSpace = addObjectToSpace,
            setPinnedObjectTypes = setPinnedObjectTypes,
            getPinnedObjectTypes = getPinnedObjectTypes
        ) as T
    }

    data class Params(
        val excludedTypeKeys: List<TypeKey>
    )
}

sealed class SelectTypeViewState{
    object Loading : SelectTypeViewState()
    object Empty : SelectTypeViewState()
    data class Content(val views: List<SelectTypeView>) : SelectTypeViewState()
}

sealed class SelectTypeView {
    sealed class Section : SelectTypeView() {
        object Pinned : Section()
        object Objects : Section()
        object Groups : Section()
        object Library : Section()
    }

    data class Type(
        val id: Id,
        val typeKey: Key,
        val name: String,
        val icon: String,
        val isFromLibrary: Boolean = false,
        val isPinned: Boolean = false,
        val isFirstInSection: Boolean = false
    ) : SelectTypeView()
}

sealed class Command {
    data class ShowTypeInstalledToast(val typeName: String) : Command()
    data class DispatchObjectType(val type: ObjectWrapper.Type) : Command()
}