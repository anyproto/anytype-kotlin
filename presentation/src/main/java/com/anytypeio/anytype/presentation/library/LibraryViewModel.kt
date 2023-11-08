package com.anytypeio.anytype.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryScreenRelation
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryScreenType
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryView
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.allUniqueBy
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.RemoveObjectsFromWorkspace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.library.delegates.LibraryRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.LibraryTypesDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyTypesDelegate
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class LibraryViewModel(
    private val myTypesDelegate: MyTypesDelegate,
    private val libraryTypesDelegate: LibraryTypesDelegate,
    private val myRelationsDelegate: MyRelationsDelegate,
    private val libraryRelationsDelegate: LibraryRelationsDelegate,
    private val addObjectToWorkspace: AddObjectToWorkspace,
    private val removeObjectsFromWorkspace: RemoveObjectsFromWorkspace,
    private val resourceManager: LibraryResourceManager,
    private val setObjectDetails: SetObjectDetails,
    private val createObject: CreateObject,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager
) : NavigationViewModel<LibraryViewModel.Navigation>() {

    private val uiEvents = MutableStateFlow<LibraryEvent>(LibraryEvent.Query.MyTypes(""))
    private val analyticsEvents = MutableStateFlow<LibraryAnalyticsEvent.Ui>(
        LibraryAnalyticsEvent.Ui.Idle
    )

    val effects = MutableStateFlow<Effect>(Effect.Idle)

    val uiState: StateFlow<LibraryScreenState> = combine(
        myTypesDelegate.itemsFlow,
        libraryTypesDelegate.itemsFlow,
        myRelationsDelegate.itemsFlow,
        libraryRelationsDelegate.itemsFlow
    ) { myTypes, libTypes, myRel, libRel ->

        val libTypesItems = updateInstalledValueForTypes(
            libTypes,
            myTypes
        )
        val libRelItems = updateInstalledValueForRelations(
            libRel,
            myRel
        )

        LibraryScreenState(
            types = LibraryScreenState.Tabs.Types(myTypes, libTypesItems),
            relations = LibraryScreenState.Tabs.Relations(myRel, libRelItems)
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
        initialValue = LibraryScreenState(
            types = LibraryScreenState.Tabs.Types(
                myTypes = LibraryScreenState.Tabs.TabData(),
                libTypes = LibraryScreenState.Tabs.TabData()
            ),
            relations = LibraryScreenState.Tabs.Relations(
                myRelations = LibraryScreenState.Tabs.TabData(),
                libRelations = LibraryScreenState.Tabs.TabData()
            )
        )
    )

    init {
        viewModelScope.launch {
            uiEvents.collect {
                when (it) {
                    is LibraryEvent.Query -> proceedQueryEvent(it)
                    is LibraryEvent.ToggleInstall -> proceedWithToggleInstall(it.item)
                    is LibraryEvent.Type -> proceedWithTypeActions(it)
                    is LibraryEvent.Relation -> proceedWithRelationActions(it)
                    is LibraryEvent.BottomMenu -> proceedWithBottomMenuActions(it)
                }
            }
        }
        viewModelScope.launch {
            analyticsEvents.filterIsInstance<LibraryAnalyticsEvent.Ui.TabView>()
                .collectIndexed { index, it ->
                    val route = if (index == 0) ROUTE_OUTER else ROUTE_INNER
                    proceedWithViewAnalytics(it, route)
                }
        }
    }

    private fun proceedWithViewAnalytics(it: LibraryAnalyticsEvent.Ui.TabView, route: String) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = libraryView,
            props = Props(
                mapOf(
                    "type" to it.type,
                    "view" to it.view,
                    "route" to route
                )
            )
        )
    }

    private fun proceedWithBottomMenuActions(it: LibraryEvent.BottomMenu) {
        when (it) {
            is LibraryEvent.BottomMenu.Back -> navigate(Navigation.Back())
            is LibraryEvent.BottomMenu.Search -> navigate(Navigation.Search())
            is LibraryEvent.BottomMenu.CreateObject -> proceedWithCreateDoc()
        }
    }

    fun onCreateObjectOfTypeClicked(type: Key) {
        proceedWithCreateDoc(
            typeKey = TypeKey(type)
        )
    }

    private fun proceedWithCreateDoc(
        typeKey: TypeKey? = null
    ) {
        viewModelScope.launch {
            createObject.async(
                CreateObject.Param(
                    type = typeKey,
                    internalFlags = buildList {
                        add(InternalFlags.ShouldSelectTemplate)
                        add(InternalFlags.ShouldEmptyDelete)
                        if (typeKey == null) {
                            add(InternalFlags.ShouldSelectType)
                        }
                    }
                )
            ).fold(
                onSuccess = { result ->
                    navigate(Navigation.CreateDoc(result.objectId))
                },
                onFailure = { e -> Timber.e(e, "Error while creating a new page") }
            )
        }
    }

    private fun proceedQueryEvent(event: LibraryEvent.Query) {
        when (event) {
            is LibraryEvent.Query.MyTypes -> {
                myTypesDelegate.onQueryMyTypes(event.query)
            }
            is LibraryEvent.Query.LibraryTypes -> {
                libraryTypesDelegate.onQueryLibTypes(event.query)
            }
            is LibraryEvent.Query.MyRelations -> {
                myRelationsDelegate.onQueryMyRelations(event.query)
            }
            is LibraryEvent.Query.LibraryRelations -> {
                libraryRelationsDelegate.onQueryLibRelations(event.query)
            }
        }
    }

    private fun proceedWithToggleInstall(item: LibraryView) {
        when (val dependentData = item.dependentData) {
            is DependentData.Model -> uninstallObject(item, dependentData.item.id)
            is DependentData.None -> installObject(item)
        }
    }

    private fun proceedWithTypeActions(event: LibraryEvent.Type) {
        when (event) {
            is LibraryEvent.Type.Create -> {
                navigate(Navigation.OpenTypeCreation(event.name))
            }
            is LibraryEvent.Type.Edit -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = libraryScreenType,
                    props = Props(
                        map = mapOf("objectType" to event.item.id)
                    )
                )
                navigate(Navigation.OpenTypeEditing(event.item))
            }
        }
    }

    private fun proceedWithRelationActions(event: LibraryEvent.Relation) {
        when (event) {
            is LibraryEvent.Relation.Create -> navigate(Navigation.OpenRelationCreation(event.name))
            is LibraryEvent.Relation.Edit -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = libraryScreenRelation,
                    props = Props(
                        map = mapOf("relationKey" to event.item.id)
                    )
                )
                navigate(Navigation.OpenRelationEditing(event.item))
            }
        }
    }

    private fun installObject(item: LibraryView) {
        viewModelScope.launch {
            addObjectToWorkspace(
                AddObjectToWorkspace.Params(
                    space = spaceManager.get(),
                    objects = listOf (item.id)
                )
            ).proceed(
                success = {
                    when (item) {
                        is LibraryView.LibraryRelationView -> {
                            sendToast(resourceManager.messageRelationAdded(item.name))
                        }
                        is LibraryView.LibraryTypeView -> {
                            sendToast(resourceManager.messageTypeAdded(item.name))
                        }
                        else -> {
                            Timber.e("Unsupported item type: $item")
                        }
                    }
                },
                failure = {
                    Timber.e(it, "Error while adding relation to workspace.")
                    sendToast(resourceManager.errorMessage)
                }
            )
        }
    }

    private fun uninstallObject(item: LibraryView, id: Id) {
        viewModelScope.launch {
            removeObjectsFromWorkspace.execute(
                RemoveObjectsFromWorkspace.Params(listOf(id))
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while removing relation from workspace.")
                    sendToast(resourceManager.errorMessage)
                },
                onSuccess = {
                    when (item) {
                        is LibraryView.LibraryRelationView -> {
                            sendToast(resourceManager.messageRelationRemoved(item.name))
                        }
                        is LibraryView.LibraryTypeView -> {
                            sendToast(resourceManager.messageTypeRemoved(item.name))
                        }
                        else -> {
                            Timber.e("Unsupported item type: $item")
                        }
                    }
                }
            )
        }
    }

    fun uninstallObject(id: Id, type: LibraryItem, name: String) {
        viewModelScope.launch {
            removeObjectsFromWorkspace.execute(
                RemoveObjectsFromWorkspace.Params(listOf(id))
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while uninstalling object")
                    sendToast(resourceManager.errorMessage)
                },
                onSuccess = {
                    val message = when (type) {
                        LibraryItem.TYPE -> resourceManager.messageTypeRemoved(name)
                        LibraryItem.RELATION -> resourceManager.messageRelationRemoved(name)
                    }
                    sendToast(message)
                }
            )
        }
    }

    fun eventStream(event: LibraryEvent) {
        uiEvents.value = event
    }

    fun analyticsStream(event: LibraryAnalyticsEvent.Ui) {
        analyticsEvents.value = event
    }

    private fun updateInstalledValueForTypes(
        libTypes: LibraryScreenState.Tabs.TabData,
        myTypes: LibraryScreenState.Tabs.TabData
    ): LibraryScreenState.Tabs.TabData {
        if (BuildConfig.DEBUG) {
            assert(libTypes.items.allUniqueBy { it.id })
            assert(myTypes.items.allUniqueBy { it.id })
        }
        return libTypes.copy(
            items = libTypes.items.map { libType ->
                if (libType is LibraryView.LibraryTypeView) {
                    with(
                        myTypes.items.find {
                            (it as? LibraryView.MyTypeView)?.sourceObject == libType.id
                        }
                    ) {
                        libType.copy(
                            dependentData = if (this != null) {
                                DependentData.Model(item = this)
                            } else DependentData.None
                        )
                    }
                } else {
                    libType
                }
            }.distinctBy { view -> view.id }
        )
    }

    private fun updateInstalledValueForRelations(
        libRelations: LibraryScreenState.Tabs.TabData,
        myRelations: LibraryScreenState.Tabs.TabData
    ): LibraryScreenState.Tabs.TabData {
        if (BuildConfig.DEBUG) {
            assert(libRelations.items.allUniqueBy { it.id })
            assert(myRelations.items.allUniqueBy { it.id })
        }
        return libRelations.copy(
            items = libRelations.items.map { libRelation ->
                if (libRelation is LibraryView.LibraryRelationView) {
                    with(
                        myRelations.items.find {
                            (it as? LibraryView.MyRelationView)?.sourceObject == libRelation.id
                        }
                    ) {
                        libRelation.copy(
                            dependentData = if (this != null) {
                                DependentData.Model(item = this)
                            } else DependentData.None
                        )
                    }
                } else {
                    libRelation
                }
            }.distinctBy { view -> view.id }
        )
    }

    fun updateObject(id: String, name: String, icon: String?) {
        viewModelScope.launch {
            setObjectDetails.execute(
                SetObjectDetails.Params(
                    ctx = id,
                    details = mapOf(
                        Relations.NAME to name,
                        Relations.ICON_EMOJI to icon.orNull(),
                    )
                )
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while updating object details")
                    sendToast(resourceManager.errorMessage)
                },
                onSuccess = {
                    // do nothing
                }
            )
        }
    }

    fun onObjectCreated() {
        effects.value = Effect.ObjectCreated()
    }

    override fun onCleared() {
        GlobalScope.launch {
            myRelationsDelegate.unsubscribe()
            libraryRelationsDelegate.unsubscribe()
            myTypesDelegate.unsubscribe()
            libraryTypesDelegate.unsubscribe()
        }
        super.onCleared()
    }

    class Factory @Inject constructor(
        private val myTypesDelegate: MyTypesDelegate,
        private val libraryTypesDelegate: LibraryTypesDelegate,
        private val myRelationsDelegate: MyRelationsDelegate,
        private val libraryRelationsDelegate: LibraryRelationsDelegate,
        private val addObjectToWorkspace: AddObjectToWorkspace,
        private val removeObjectsFromWorkspace: RemoveObjectsFromWorkspace,
        private val resourceManager: LibraryResourceManager,
        private val setObjectDetails: SetObjectDetails,
        private val createObject: CreateObject,
        private val analytics: Analytics,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(
                myTypesDelegate,
                libraryTypesDelegate,
                myRelationsDelegate,
                libraryRelationsDelegate,
                addObjectToWorkspace,
                removeObjectsFromWorkspace,
                resourceManager,
                setObjectDetails,
                createObject,
                analytics,
                spaceManager
            ) as T
        }
    }

    sealed class Navigation {
        class OpenTypeCreation(
            val name: String = ""
        ) : Navigation()

        class OpenRelationCreation(
            val name: String = ""
        ) : Navigation()

        class OpenTypeEditing(
            val view: LibraryView.MyTypeView
        ) : Navigation()

        class OpenRelationEditing(
            val view: LibraryView.MyRelationView
        ) : Navigation()

        class Back : Navigation()

        class Search : Navigation()

        class CreateDoc(val id: Id) : Navigation()
    }

    sealed class Effect {
        class ObjectCreated : Effect()
        object Idle : Effect()
    }

    enum class LibraryItem {
        TYPE, RELATION
    }

}

private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L
private const val ROUTE_INNER = "inner"
private const val ROUTE_OUTER = "outer"