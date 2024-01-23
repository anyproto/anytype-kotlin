package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.EMPTY_QUERY
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_models.ObjectOrigin
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.core_utils.ext.swap
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.launch.SetDefaultObjectType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.spaces.AddObjectToSpace
import com.anytypeio.anytype.domain.types.GetPinnedObjectTypes
import com.anytypeio.anytype.domain.types.SetPinnedObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectObjectTypeViewModel(
    private val params: Params,
    private val getObjectTypes: GetObjectTypes,
    private val spaceManager: SpaceManager,
    private val addObjectToSpace: AddObjectToSpace,
    private val setPinnedObjectTypes: SetPinnedObjectTypes,
    private val getPinnedObjectTypes: GetPinnedObjectTypes,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val setDefaultObjectType: SetDefaultObjectType,
    private val createBookmarkObject: CreateBookmarkObject,
    private val createPrefilledNote: CreatePrefilledNote,
    private val appActionManager: AppActionManager,
    private val analytics: Analytics,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : BaseViewModel() {

    val viewState = MutableStateFlow<SelectTypeViewState>(SelectTypeViewState.Loading)
    val clipboardToolbarViewState = MutableStateFlow<ClipboardToolbarViewState>(ClipboardToolbarViewState.Hidden)
    val commands = MutableSharedFlow<Command>()
    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    private val _objectTypes = mutableListOf<ObjectWrapper.Type>()

    private val query = MutableSharedFlow<String>()

    lateinit var space: Id

    private val defaultObjectTypePipeline = MutableSharedFlow<TypeKey>(1)

    private val pinned = MutableStateFlow<List<TypeId>>(emptyList())

    init {
        viewModelScope.launch {
            space = spaceManager.get()
            getPinnedObjectTypes.flow(
                GetPinnedObjectTypes.Params(SpaceId(space))
            ).collect { pinned.value = it }
        }
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
                    pinned,
                    defaultObjectTypePipeline
                ) { result, pinned, default ->
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
                    val notPinnedGroups = groups.filter { !pinnedObjectTypesIds.contains(it.id) }
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
                                        isFirstInSection = index == 0,
                                        isLastInSection = index == pinnedTypes.lastIndex,
                                        isDefault = type.uniqueKey == default.key
                                    )
                                }
                            )
                        }
                        if (notPinnedGroups.isNotEmpty()) {
                            add(
                                SelectTypeView.Section.Groups
                            )
                            addAll(
                                notPinnedGroups.mapIndexed { index, type ->
                                    SelectTypeView.Type(
                                        id = type.id,
                                        typeKey = type.uniqueKey,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty(),
                                        isFirstInSection = index == 0,
                                        isLastInSection = index == pinnedTypes.lastIndex,
                                        isPinnable = true,
                                        isDefault = false,
                                        canBeDefault = false
                                    )
                                }
                            )
                        }
                        if (notPinnedObjects.isNotEmpty()) {
                            add(
                                SelectTypeView.Section.Objects
                            )
                            addAll(
                                notPinnedObjects.mapIndexed { index, type ->
                                    SelectTypeView.Type(
                                        id = type.id,
                                        typeKey = type.uniqueKey,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty(),
                                        isPinnable = true,
                                        isFirstInSection = index == 0,
                                        isLastInSection = index == pinnedTypes.lastIndex,
                                        isPinned = false,
                                        isDefault = type.uniqueKey == default.key
                                    )
                                }
                            )
                        }
                        if (filteredLibraryTypes.isNotEmpty()) {
                            add(SelectTypeView.Section.Library)
                            addAll(
                                filteredLibraryTypes.mapIndexed { index, type ->
                                    SelectTypeView.Type(
                                        id = type.id,
                                        typeKey = type.uniqueKey,
                                        name = type.name.orEmpty(),
                                        icon = type.iconEmoji.orEmpty(),
                                        isFromLibrary = true,
                                        isPinned = false,
                                        isPinnable = false,
                                        isFirstInSection = index == 0,
                                        isLastInSection = index == pinnedTypes.lastIndex,
                                        isDefault = type.uniqueKey == default.key
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

        viewModelScope.launch {
            getDefaultObjectType.async(Unit).fold(
                onSuccess = { response ->
                    defaultObjectTypePipeline.emit(response.type)
                },
                onFailure = {
                    Timber.e(it, "Error while getting default object type for init")
                }
            )
        }
    }

    fun onQueryChanged(input: String) {
        viewModelScope.launch {
            query.emit(input)
        }
    }

    fun onPinTypeClicked(typeView: SelectTypeView.Type) {
        Timber.d("onPinTypeClicked: ${typeView.id}")
        val state = viewState.value
        if (state is SelectTypeViewState.Content) {
            val updatedPinnedTypes = buildSet {
                add(TypeId(typeView.id))
                addAll(pinned.value)
            }
            proceedWithSettingPinnedTypes(
                pinned = updatedPinnedTypes.map { type -> TypeId(type.id) }
            )
            viewModelScope.launch {
                proceedWithUpdatingAppActions(
                    pinned = updatedPinnedTypes.mapNotNull { type ->
                        val obj = storeOfObjectTypes.get(type.id)
                        val key = obj?.uniqueKey
                        if (obj != null && key != null) {
                            key to obj.name.orEmpty()
                        } else {
                            null
                        }
                    }
                )
            }
        }
    }

    fun onUnpinTypeClicked(typeView: SelectTypeView.Type) {
        Timber.d("onUnpinTypeClicked: ${typeView.id}")
        val state = viewState.value
        if (state is SelectTypeViewState.Content) {
            val updatedPinnedTypes = pinned.value.filter { type -> type.id != typeView.id }
            proceedWithSettingPinnedTypes(pinned = updatedPinnedTypes)
            viewModelScope.launch {
                proceedWithUpdatingAppActions(
                    pinned = updatedPinnedTypes.mapNotNull { type ->
                        val obj = storeOfObjectTypes.get(type.id)
                        val key = obj?.uniqueKey
                        if (obj != null && key != null) {
                            key to obj.name.orEmpty()
                        } else {
                            null
                        }
                    }
                )
            }
        }
    }

    fun onMoveLeftClicked(typeView: SelectTypeView.Type) {
        Timber.d("onMoveLeftClicked: ${typeView.id}")
        val pinnedTypes = pinned.value
        val currentPosition = pinnedTypes.indexOf(TypeId(typeView.id))
        if (currentPosition != -1 && currentPosition != 0 && pinnedTypes.size > 1) {
            val updatedPinnedTypes = pinnedTypes.toMutableList().apply {
                swap(index1 = currentPosition, index2 = currentPosition.dec())
            }
            proceedWithSettingPinnedTypes(pinned = updatedPinnedTypes)
            viewModelScope.launch {
                proceedWithUpdatingAppActions(
                    pinned = updatedPinnedTypes.mapNotNull { type ->
                        val obj = storeOfObjectTypes.get(type.id)
                        val key = obj?.uniqueKey
                        if (obj != null && key != null) {
                            key to obj.name.orEmpty()
                        } else {
                            null
                        }
                    }
                )
            }
        }
    }

    fun onMoveRightClicked(typeView: SelectTypeView.Type) {
        Timber.d("onMoveRightClicked: ${typeView.id}")
        val pinnedTypes = pinned.value
        val currentPosition = pinnedTypes.indexOf(TypeId(typeView.id))
        if (currentPosition != -1 && currentPosition != pinnedTypes.lastIndex && pinnedTypes.size > 1) {
            val updatedPinnedTypes = pinnedTypes.toMutableList().apply {
                swap(index1 = currentPosition, index2 = currentPosition.inc())
            }
            proceedWithSettingPinnedTypes(pinned = updatedPinnedTypes)
            viewModelScope.launch {
                proceedWithUpdatingAppActions(
                    pinned = updatedPinnedTypes.mapNotNull { type ->
                        val obj = storeOfObjectTypes.get(type.id)
                        val key = obj?.uniqueKey
                        if (obj != null && key != null) {
                            key to obj.name.orEmpty()
                        } else {
                            null
                        }
                    }
                )
            }
        }
    }

    private fun proceedWithSettingPinnedTypes(pinned: List<TypeId>) {
        viewModelScope.launch {
            setPinnedObjectTypes.async(
                SetPinnedObjectTypes.Params(
                    space = SpaceId(id = space),
                    types = pinned
                )
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while setting pinned types")
                },
                onSuccess = {
                    Timber.d("Set pinned types successfully")
                }
            )
        }
    }

    private fun proceedWithUpdatingAppActions(pinned: List<Pair<Key, Name>>) {
        if (pinned.isNotEmpty()) {
            appActionManager.setup(
                pinned.take(MAX_TYPE_COUNT_FOR_APP_ACTIONS).map { (key, name) ->
                    AppActionManager.Action.CreateNew(
                        type = TypeKey(key),
                        name = name
                    )
                }
            )
        }
    }

    fun onSetDefaultObjectTypeClicked(typeView: SelectTypeView.Type) {
        viewModelScope.launch {
            setDefaultObjectType.async(
                SetDefaultObjectType.Params(
                    space = SpaceId(space),
                    type = TypeId(typeView.id)
                )
            ).fold(
                onSuccess = {
                   defaultObjectTypePipeline.emit(TypeKey(typeView.typeKey))
                },
                onFailure = {
                    Timber.d(it, "Error while setting default object type")
                }
            )
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

    fun onClipboardUrlTypeDetected(url: Url) {
        clipboardToolbarViewState.value = ClipboardToolbarViewState.CreateBookmark(url)
    }

    fun onClipboardTextTypeDetected(text: String) {
        clipboardToolbarViewState.value = ClipboardToolbarViewState.CreateObject(text)
    }

    fun onClipboardToolbarClicked() {
        when(val state = clipboardToolbarViewState.value) {
            is ClipboardToolbarViewState.CreateBookmark -> {
                proceedWithCreatingBookmark(state.url)
            }
            is ClipboardToolbarViewState.CreateObject -> {
                proceedWithCreatingNote(state.text)
            }
            is ClipboardToolbarViewState.Hidden -> {
                // Do nothing.
            }
        }
    }

    private fun proceedWithCreatingBookmark(url: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            createBookmarkObject(
                CreateBookmarkObject.Params(
                    space = space,
                    url = url,
                    details = mapOf(
                        Relations.ORIGIN to ObjectOrigin.CLIPBOARD.code.toDouble()
                    )
                )
            ).process(
                success = { obj ->
                    // TODO select correct route
//                    sendAnalyticsObjectCreateEvent(
//                        analytics = analytics,
//                        objType = MarketplaceObjectTypeIds.BOOKMARK,
//                        route = EventsDictionary.Routes.sharingExtension,
//                        startTime = startTime
//                    )
                    navigation.emit(OpenObjectNavigation.OpenEditor(obj))
                },
                failure = {
                    Timber.d(it, "Error while creating bookmark")
                    sendToast("Error while creating bookmark: ${it.msg()}")
                }
            )
        }
    }

    private fun proceedWithCreatingNote(text: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val defaultObjectType = getDefaultObjectType.async(Unit).getOrNull()?.type?.let {
                if (it.key != ObjectTypeUniqueKeys.COLLECTION && it.key != ObjectTypeUniqueKeys.SET)
                    it
                else
                    null
            }
            createPrefilledNote.async(
                CreatePrefilledNote.Params(
                    text = text,
                    space = space,
                    details = mapOf(
                        Relations.ORIGIN to ObjectOrigin.CLIPBOARD.code.toDouble()
                    )
                )
            ).fold(
                onSuccess = { result ->
                    // TODO select correct route
//                    sendAnalyticsObjectCreateEvent(
//                        analytics = analytics,
//                        objType = MarketplaceObjectTypeIds.NOTE,
//                        route = EventsDictionary.Routes.sharingExtension,
//                        startTime = startTime
//                    )
                    navigation.emit(OpenObjectNavigation.OpenEditor(result))
                },
                onFailure = {
                    Timber.d(it, "Error while creating note")
                    sendToast("Error while creating note: ${it.msg()}")
                }
            )
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val getObjectTypes: GetObjectTypes,
        private val spaceManager: SpaceManager,
        private val addObjectToSpace: AddObjectToSpace,
        private val setPinnedObjectTypes: SetPinnedObjectTypes,
        private val getPinnedObjectTypes: GetPinnedObjectTypes,
        private val getDefaultObjectType: GetDefaultObjectType,
        private val setDefaultObjectType: SetDefaultObjectType,
        private val createBookmarkObject: CreateBookmarkObject,
        private val createPrefilledNote: CreatePrefilledNote,
        private val appActionManager: AppActionManager,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val analytics: Analytics
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
            getPinnedObjectTypes = getPinnedObjectTypes,
            getDefaultObjectType = getDefaultObjectType,
            setDefaultObjectType = setDefaultObjectType,
            createBookmarkObject = createBookmarkObject,
            createPrefilledNote = createPrefilledNote,
            appActionManager = appActionManager,
            storeOfObjectTypes = storeOfObjectTypes,
            analytics = analytics
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

sealed class ClipboardToolbarViewState {
    object Hidden : ClipboardToolbarViewState()
    data class CreateBookmark(val url: Url) : ClipboardToolbarViewState()
    data class CreateObject(val text: String) : ClipboardToolbarViewState()

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
        val isFirstInSection: Boolean = false,
        val isLastInSection: Boolean = false,
        val isPinnable: Boolean = true,
        val isDefault: Boolean = false,
        val canBeDefault: Boolean = true
    ) : SelectTypeView()
}

sealed class Command {
    data class ShowTypeInstalledToast(val typeName: String) : Command()
    data class DispatchObjectType(val type: ObjectWrapper.Type) : Command()
}

const val MAX_TYPE_COUNT_FOR_APP_ACTIONS = 3