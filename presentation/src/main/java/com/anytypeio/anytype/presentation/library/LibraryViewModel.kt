package com.anytypeio.anytype.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryScreenRelation
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryScreenType
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryView
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.allUniqueBy
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.RemoveObjectsFromWorkspace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Companion.HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.library.delegates.LibraryRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.LibraryTypesDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyTypesDelegate
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class LibraryViewModel(
    private val params: Params,
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
    private val spaceManager: SpaceManager,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : NavigationViewModel<LibraryViewModel.Navigation>(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val icon = MutableStateFlow<ProfileIconView>(ProfileIconView.Loading)

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
        Timber.i("LibraryViewModel, init")
        proceedWithObservingProfileIcon()
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

    private fun proceedWithObservingProfileIcon() {
        viewModelScope.launch {
            spaceManager
                .observe()
                .flatMapLatest { config ->
                    storelessSubscriptionContainer.subscribe(
                        StoreSearchByIdsParams(
                            subscription = HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                            targets = listOf(config.profile),
                            keys = listOf(
                                Relations.ID,
                                Relations.NAME,
                                Relations.ICON_EMOJI,
                                Relations.ICON_IMAGE,
                                Relations.ICON_OPTION
                            )
                        )
                    ).map { result ->
                        val obj = result.firstOrNull()
                        obj?.profileIcon(urlBuilder) ?: ProfileIconView.Placeholder(null)
                    }
                }
                .catch { Timber.e(it, "Error while observing space icon") }
                .flowOn(appCoroutineDispatchers.io)
                .collect { icon.value = it }
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
            is LibraryEvent.BottomMenu.OpenProfile -> navigate(Navigation.SelectSpace)
        }
    }

    fun onCreateObjectOfTypeClicked(objType: ObjectWrapper.Type) {
        proceedWithCreateDoc(objType)
    }

    private fun proceedWithCreateDoc(
        objType: ObjectWrapper.Type? = null
    ) {
        val startTime = System.currentTimeMillis()
        val params = objType?.uniqueKey.getCreateObjectParams(objType?.defaultTemplateId)
        viewModelScope.launch {
            createObject.async(params).fold(
                onSuccess = {
                    result -> proceedWithOpeningObject(result.obj)
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        route = EventsDictionary.Routes.objCreateLibrary,
                        startTime = startTime,
                        objType = objType ?: storeOfObjectTypes.getByKey(result.typeKey.key),
                        view = EventsDictionary.View.viewHome,
                        spaceParams = provideParams(spaceManager.get())
                    )
                            },
                onFailure = { e -> Timber.e(e, "Error while creating a new object") }
            )
        }
    }

    private fun proceedWithOpeningObject(obj: ObjectWrapper.Basic) {
        when (val navigation = obj.navigation()) {
            is OpenObjectNavigation.OpenDataView -> {
                navigate(Navigation.OpenSetOrCollection(navigation.target))
            }
            is OpenObjectNavigation.OpenEditor -> {
                navigate(Navigation.OpenEditor(navigation.target))
            }
            is OpenObjectNavigation.UnexpectedLayoutError -> {
                sendToast("Unexpected layout: ${navigation.layout}")
            }
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
        val myTypeViews = myTypes
            .items
            .filterIsInstance<LibraryView.MyTypeView>()

        return libTypes.copy(
            items = libTypes.items.map { libType ->
                if (libType is LibraryView.LibraryTypeView) {
                    with(
                        myTypeViews.find { it.uniqueKey == libType.uniqueKey }
                    ) {
                        libType.copy(
                            dependentData = if (this != null) {
                                DependentData.Model(item = this)
                            } else {
                                DependentData.None
                            }
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

        val updatedLibraryRelations = updateLibraryRelationItems(
            libraryItems = libRelations.items,
            myRelationItems = myRelations.items
        )
        return libRelations.copy(
            items = updatedLibraryRelations.distinctBy { view -> view.id }
        )
    }

    private fun updateLibraryRelationItems(
        libraryItems: List<LibraryView>,
        myRelationItems: List<LibraryView>
    ): List<LibraryView> {
        return libraryItems.map { libraryItem ->
            if (libraryItem !is LibraryView.LibraryRelationView) {
                return@map libraryItem
            }
            val relationInstalled = myRelationItems.firstOrNull { myRelationItem ->
                (myRelationItem as? LibraryView.MyRelationView)?.sourceObject == libraryItem.id
            }
            val dependedData = if (relationInstalled != null) {
                DependentData.Model(item = relationInstalled)
            } else {
                DependentData.None
            }
            libraryItem.copy(dependentData = dependedData)
        }
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
        private val params: Params,
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
        private val spaceManager: SpaceManager,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val urlBuilder: UrlBuilder,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(
                params = params,
                myTypesDelegate = myTypesDelegate,
                libraryTypesDelegate = libraryTypesDelegate,
                myRelationsDelegate = myRelationsDelegate,
                libraryRelationsDelegate = libraryRelationsDelegate,
                addObjectToWorkspace = addObjectToWorkspace,
                removeObjectsFromWorkspace = removeObjectsFromWorkspace,
                resourceManager = resourceManager,
                setObjectDetails = setObjectDetails,
                createObject = createObject,
                analytics = analytics,
                spaceManager = spaceManager,
                storelessSubscriptionContainer = storelessSubscriptionContainer,
                appCoroutineDispatchers = appCoroutineDispatchers,
                urlBuilder = urlBuilder,
                storeOfObjectTypes = storeOfObjectTypes,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
            ) as T
        }
    }

    class Params(val space: SpaceId)

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

        object SelectSpace: Navigation()

        class Back : Navigation()

        class Search : Navigation()

        class OpenEditor(val id: Id) : Navigation()

        class OpenSetOrCollection(val id: Id) : Navigation()
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