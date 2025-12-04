package com.anytypeio.anytype.presentation.objects.menu

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dashboard.interactor.SetObjectListIsFavorite
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddToCollectionEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddToFavoritesEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsBackLinkAddEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDuplicateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsMoveToBinEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveFromFavoritesEvent
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugGoroutinesShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.MiddlewareShareDownloader
import com.anytypeio.anytype.presentation.widgets.findWidgetBlockForObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ObjectMenuViewModelBase(
    private val addBackLinkToObject: AddBackLinkToObject,
    protected val delegator: Delegator<Action>,
    protected val urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val menuOptionsProvider: ObjectMenuOptionsProvider,
    private val duplicateObject: DuplicateObject,
    private val addObjectToCollection: AddObjectToCollection,
    private val debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader,
    private val createWidget: CreateWidget,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val payloadDelegator: PayloadDelegator,
    private val setObjectListIsFavorite: SetObjectListIsFavorite,
    private val setObjectIsArchived: SetObjectListIsArchived,
    private val fieldParser: FieldParser,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val getSpaceInviteLink: GetSpaceInviteLink,
    private val deepLinkResolver: DeepLinkResolver,
    private val showObject: GetObject,
    private val deleteWidget: DeleteWidget
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    protected val jobs = mutableListOf<Job>()
    val isDismissed = MutableStateFlow(false)
    val isObjectArchived = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val actions = MutableStateFlow(emptyList<ObjectAction>())

    private val pinnedWidgetBlockId = MutableStateFlow<Id?>(null)

    private val _options = MutableStateFlow(
        ObjectMenuOptionsProvider.Options(
            hasIcon = false,
            hasCover = false,
            hasRelations = false,
            hasDiagnosticsVisibility = false,
            hasHistory = false,
            hasDescriptionShow = false,
            hasObjectLayoutConflict = false
        )
    )
    val options: Flow<ObjectMenuOptionsProvider.Options> = _options

    abstract fun onIconClicked(ctx: Id, space: Id)
    abstract fun onCoverClicked(ctx: Id, space: Id)
    abstract fun onDescriptionClicked(ctx: Id, space: Id)
    abstract fun onRelationsClicked()
    abstract fun onResetToDefaultLayout(ctx: Id, space: Id)

    val showLayoutConflictScreen = MutableStateFlow(false)

    fun onHistoryClicked(ctx: Id, space: Id) {
        viewModelScope.launch {
            commands.emit(Command.OpenHistoryScreen(ctx, space))
        }
    }

    fun onStop() {
        jobs.forEach(Job::cancel)
        jobs.clear()
    }

    fun onStart(
        ctx: Id,
        space: SpaceId,
        isFavorite: Boolean,
        isArchived: Boolean,
        isLocked: Boolean,
        isTemplate: Boolean,
        isReadOnly: Boolean
    ) {
        Timber.d("ObjectMenuViewModelBase, onStart, ctx:[$ctx], isFavorite:[$isFavorite], isArchived:[$isArchived], isLocked:[$isLocked], isReadOnly: [$isReadOnly]")
        viewModelScope.launch {
            pinnedWidgetBlockId.collect { widgetId ->
                actions.value = buildActions(
                    ctx = ctx,
                    isArchived = isArchived,
                    isFavorite = isFavorite,
                    isTemplate = isTemplate,
                    isLocked = isLocked,
                    isReadOnly = isReadOnly,
                    isCurrentObjectPinned = widgetId != null
                )
            }
        }
        jobs += viewModelScope.launch {
            menuOptionsProvider
                .provide(ctx = ctx, isLocked = isLocked, isReadOnly = isReadOnly)
                .distinctUntilChanged()
                .collect(_options)
        }
        jobs += viewModelScope.launch {
            checkIfObjectIsPinned(ctx, space)
        }
    }

    abstract fun onActionClicked(ctx: Id, space: Id, action: ObjectAction)

    abstract fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isTemplate: Boolean = false,
        isLocked: Boolean,
        isReadOnly: Boolean,
        isCurrentObjectPinned: Boolean
    ): List<ObjectAction>

    protected fun proceedWithRemovingFromFavorites(ctx: Id) {
        Timber.d("proceedWithRemovingFromFavorites, cts:[$ctx]")
        viewModelScope.launch {
            val params = SetObjectListIsFavorite.Params(
                objectIds = listOf(ctx),
                isFavorite = false
            )
            setObjectListIsFavorite.async(params).fold(
                onSuccess = {
                    sendAnalyticsRemoveFromFavoritesEvent(analytics)
                    _toasts.emit(REMOVE_FROM_FAVORITE_SUCCESS_MSG).also {
                        isDismissed.value = true
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while removing from favorite.")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    protected fun proceedWithAddingToFavorites(ctx: Id) {
        Timber.d("proceedWithAddingToFavorites, ctx:[$ctx]")
        viewModelScope.launch {
            val params = SetObjectListIsFavorite.Params(
                objectIds = listOf(ctx),
                isFavorite = true
            )
            setObjectListIsFavorite.async(params).fold(
                onSuccess = {
                    sendAnalyticsAddToFavoritesEvent(analytics)
                    _toasts.emit(ADD_TO_FAVORITE_SUCCESS_MSG).also {
                        isDismissed.value = true
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while removing from favorite.")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    fun proceedWithUpdatingArchivedStatus(ctx: Id, isArchived: Boolean) {
        Timber.d("proceedWithUpdatingArchivedStatus, cts:[$ctx], isArchived:[$isArchived]")
        viewModelScope.launch {
            val params = SetObjectListIsArchived.Params(
                targets = listOf(ctx),
                isArchived = isArchived
            )
            setObjectIsArchived.async(params).fold(
                onSuccess = {
                    if (isArchived) {
                        sendAnalyticsMoveToBinEvent(analytics)
                        _toasts.emit(MOVE_OBJECT_TO_BIN_SUCCESS_MSG)
                    } else {
                        _toasts.emit(RESTORE_OBJECT_SUCCESS_MSG)
                    }
                    isObjectArchived.value = true
                },
                onFailure = {
                    Timber.e(it, MOVE_OBJECT_TO_BIN_ERR_MSG)
                    _toasts.emit(MOVE_OBJECT_TO_BIN_ERR_MSG)
                }
            )
        }
    }

    fun onBackLinkOrAddToObjectAction(
        ctx: Id,
        space: Id,
        backLinkId: Id,
        backLinkName: String,
        backLinkLayout: ObjectType.Layout?,
        backLinkIcon: ObjectIcon,
        fromName: String
    ) {
        Timber.d("onBackLinkOrAddToObjectAction, ctx:[$ctx], backLinkId:[$backLinkId], backLinkLayout:[$backLinkLayout]")
        if (backLinkLayout == null) {
            Timber.e("onBackLinkOrAddToObjectAction, layout is null")
            viewModelScope.launch { _toasts.emit(BACK_LINK_WRONG_LAYOUT) }
            return
        }
        when (backLinkLayout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE -> {
                onLinkedMyselfTo(
                    myself = ctx,
                    addTo = backLinkId,
                    fromName = fromName,
                    space = space
                )
            }
            ObjectType.Layout.COLLECTION -> {
                proceedWithAddObjectToCollection(
                    ctx = ctx,
                    space = space,
                    collection = backLinkId,
                    collectionName = backLinkName,
                    collectionIcon = backLinkIcon,
                    fromName = fromName
                )
            }
            else -> { Timber.e("onBackLinkOrAddToObjectAction, layout:$backLinkLayout is not supported") }
        }
    }

    private fun proceedWithAddObjectToCollection(
        ctx: Id,
        space: Id,
        collection: Id,
        collectionName: String,
        collectionIcon: ObjectIcon,
        fromName: String
    ) {
        val params = AddObjectToCollection.Params(
            ctx = collection,
            after = "",
            targets = listOf(ctx)
        )
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            addObjectToCollection.execute(params).fold(
                onSuccess = { payload ->
                    dispatcher.send(payload)
                    sendAnalyticsAddToCollectionEvent(
                        analytics = analytics,
                        startTime = startTime
                    )
                    commands.emit(
                        Command.OpenSnackbar(
                            id = collection,
                            currentObjectName = fromName,
                            targetObjectName = collectionName,
                            icon = collectionIcon,
                            isCollection = true,
                            space = space
                        )
                    )
                },
                onFailure = { Timber.e(it, "Error while adding object to collection") }
            )
        }
    }

    fun onLinkedMyselfTo(
        myself: Id,
        addTo: Id,
        fromName: String?,
        space: Id
    ) {
        Timber.d("onLinkedMyselfTo, myself:[$myself], addTo:[$addTo], fromName:[$fromName]")
        jobs += viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            addBackLinkToObject.execute(
                AddBackLinkToObject.Params(
                    objectToLink = myself,
                    objectToPlaceLink = addTo,
                    saveAsLastOpened = true,
                    spaceId = SpaceId(space)
                )
            ).fold(
                onSuccess = { obj ->
                    sendAnalyticsBackLinkAddEvent(
                        analytics = analytics,
                        startTime = startTime,
                        spaceParams = provideParams(space)
                    )
                    commands.emit(
                        Command.OpenSnackbar(
                            id = addTo,
                            currentObjectName = fromName,
                            targetObjectName = fieldParser.getObjectName(obj),
                            icon = obj.objectIcon(urlBuilder),
                            space = space
                        )
                    )
                },
                onFailure = {
                    Timber.e(it, "Error while adding link from object to object")
                }
            )
        }
    }

    protected fun proceedWithLinkTo() {
        jobs += viewModelScope.launch { commands.emit(Command.OpenLinkToChooser) }
    }

    fun proceedWithOpeningPage(target: Id, space: Id) {
        Timber.d("proceedWithOpeningPage, id:[$target]")
        viewModelScope.launch {
            delegator.delegate(
                Action.OpenObject(target = target, space = space)
            )
        }
    }

    fun proceedWithOpeningCollection(target: Id, space: Id) {
        Timber.d("proceedWithOpeningCollection, id:[$target]")
        viewModelScope.launch {
            delegator.delegate(
                Action.OpenCollection(
                    target = target,
                    space = space
                )
            )
        }
    }

    fun proceedWithDuplication(ctx: Id, space: Id, details: Map<Id, Struct>) {
        Timber.d("proceedWithDuplication, ctx:[$ctx]")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            duplicateObject(ctx).process(
                failure = {
                    Timber.e(it, "Duplication error")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                },
                success = { id ->
                    _toasts.emit("Your object is duplicated")
                    delegator.delegate(
                        Action.Duplicate(
                            target = id,
                            space = space
                        )
                    )
                    sendAnalyticsDuplicateEvent(
                        analytics = analytics,
                        startTime = startTime,
                        details = details,
                        ctx = ctx,
                        spaceParams = provideParams(space)
                    )
                }
            )
        }
    }

    open fun onDiagnosticsClicked(ctx: Id) {
        viewModelScope.launch {
            _toasts.emit("You should not call diagnostics for sets")
        }
    }

    fun onDiagnosticsGoroutinesClicked(ctx: Id) {
        jobs += viewModelScope.launch {
            debugGoroutinesShareDownloader.stream(
                MiddlewareShareDownloader.Params(objectId = ctx, name = "goroutines")
            ).collect { result ->
                result.fold(
                    onSuccess = { success -> commands.emit(Command.ShareDebugGoroutines(success.path)) },
                    onFailure = {
                        sendToast("Error while collecting goroutines diagnostics :${it.message}")
                        Timber.e(it, "Error while collecting goroutines diagnostics")
                    }
                )
            }
        }
    }

    fun proceedWithCreatingWidget(obj: ObjectWrapper.Basic) {
        viewModelScope.launch {
            val config = spaceManager.getConfig()
            if (config != null && obj.isValid) {

                var target: Id?  = null

                showObject.async(
                    GetObject.Params(
                        target = config.widgets,
                        space = SpaceId(config.space),
                        saveAsLastOpened = false
                    )
                ).onSuccess { objectView ->
                    objectView.blocks.find { it.content is Block.Content.Widget }?.let {
                        target = it.id
                    }
                }

                val params = CreateWidget.Params(
                    ctx = config.widgets,
                    source = obj.id,
                    type = when {
                        obj.layout.isDataView() -> {
                            WidgetLayout.VIEW
                        }
                        obj.layout == ObjectType.Layout.PARTICIPANT -> {
                            WidgetLayout.LINK
                        }
                        else -> {
                            WidgetLayout.TREE
                        }
                    },
                    position = Position.TOP,
                    target = target
                )
                createWidget.async(params).fold(
                    onSuccess = { payload ->
                        payloadDelegator.dispatch(payload)
                        sendToast("Widget created")
                        isDismissed.value = true
                    },
                    onFailure = {
                        Timber.e(it, "Error while creating widget")
                        sendToast(SOMETHING_WENT_WRONG_MSG)
                    }
                )
            } else {
                if (!obj.isValid) {
                    sendToast("Could not create widget: object is not valid.")
                }
                if (config == null) {
                    sendToast("Could not create widget: config is missing.")
                }
            }
        }
    }

    fun proceedWithRemovingWidget() {
        val widgetId = pinnedWidgetBlockId.value ?: return
        viewModelScope.launch {
            val config = spaceManager.getConfig()
            deleteWidget.async(
                params = DeleteWidget.Params(
                    ctx = config?.widgets ?: return@launch,
                    targets = listOf(widgetId)
                )
            ).fold(
                onSuccess = { payload ->
                    payloadDelegator.dispatch(payload)
                    pinnedWidgetBlockId.value = null
                    sendToast("Widget removed")
                    isDismissed.value = true
                },
                onFailure = {
                    Timber.e(it, "Error while deleting widget")
                    sendToast(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    suspend fun proceedWithGeneratingObjectLink(
        space: SpaceId,
        ctx: String
    ): String {
        val spaceView = spaceViewSubscriptionContainer.get(space)
        return if (spaceView?.spaceAccessType == SpaceAccessType.SHARED) {
            val link = getSpaceInviteLink.async(space).getOrNull()
            if (link != null) {
                deepLinkResolver.createObjectDeepLinkWithInvite(
                    obj = ctx,
                    space = space,
                    encryptionKey = link.fileKey,
                    invite = link.contentId
                )
            } else {
                deepLinkResolver.createObjectDeepLink(
                    obj = ctx,
                    space = space
                )
            }
        } else {
            deepLinkResolver.createObjectDeepLink(
                obj = ctx,
                space = space
            )
        }
    }

    fun onShowConflictScreen(objectId: Id, space: Id) {
        viewModelScope.launch {
            showLayoutConflictScreen.value = true
        }
    }

    fun onHideConflictScreen() {
        viewModelScope.launch {
            showLayoutConflictScreen.value = false
        }
    }

    fun onPublishToWebClicked(ctx: Id, space: Id) {
        viewModelScope.launch {
            commands.emit(
                Command.PublishToWeb(
                    ctx = ctx,
                    space = space
                )
            )
        }
    }

    /**
     * Checks if the given object is pinned as a widget in the space's home screen.
     * Updates [pinnedWidgetBlockId] with the widget block ID if found, or null otherwise.
     */
    private suspend fun checkIfObjectIsPinned(ctx: Id, space: SpaceId) {
        spaceManager.getConfig(space)?.let { config ->
            fetchWidgetsAndUpdatePinnedState(
                ctx = ctx,
                widgetsObjectId = config.widgets,
                space = space
            )
        }
    }

    /**
     * Fetches the widgets object and updates the pinned state for the given context.
     */
    private suspend fun fetchWidgetsAndUpdatePinnedState(
        ctx: Id,
        widgetsObjectId: Id,
        space: SpaceId
    ) {
        val params = GetObject.Params(
            target = widgetsObjectId,
            space = space,
            saveAsLastOpened = false
        )
        showObject.async(params).fold(
            onFailure = { error ->
                Timber.e(error, "Error fetching widgets object")
                pinnedWidgetBlockId.value = null
            },
            onSuccess = { obj ->
                pinnedWidgetBlockId.value = findWidgetBlockForObject(ctx, obj.blocks)
            }
        )
    }

    sealed class Command {
        data object OpenObjectIcons : Command()
        data object OpenSetIcons : Command()
        data object OpenObjectCover : Command()
        data object OpenSetCover : Command()
        data object OpenSetLayout : Command()
        data object OpenObjectRelations : Command()
        data object OpenSetRelations : Command()
        data object OpenLinkToChooser : Command()
        data class ShareDeeplinkToObject(val link: String): Command()
        data class ShareDebugTree(val uri: Uri) : Command()
        data class ShareDebugGoroutines(val path: String) : Command()
        data class OpenHistoryScreen(val objectId: Id, val spaceId: Id) : Command()
        data class PublishToWeb(val ctx: Id, val space: Id) : Command()
        data class OpenSnackbar(
            val id: Id,
            val space: Id,
            val currentObjectName: String?,
            val targetObjectName: String?,
            val icon: ObjectIcon,
            val isCollection: Boolean = false
        ) : Command()
        data class OpenTemplate(
            val templateId: Id,
            val space: Id,
            val typeId: Id,
            val typeKey: Key,
            val typeName: String
        ) : Command()
    }

    companion object {
        const val MOVE_OBJECT_TO_BIN_SUCCESS_MSG = "Moved to bin!"
        const val RESTORE_OBJECT_SUCCESS_MSG = "Object restored!"
        const val MOVE_OBJECT_TO_BIN_ERR_MSG =
            "Error while moving object to bin. Please, try again later."
        const val ADD_TO_FAVORITE_SUCCESS_MSG = "Object added to favorites."
        const val REMOVE_FROM_FAVORITE_SUCCESS_MSG = "Object removed from favorites."
        const val NOT_ALLOWED = "Not allowed for this object"
        const val OBJECT_IS_LOCKED_MSG = "Your object is locked"
        const val OBJECT_IS_UNLOCKED_MSG = "Your object is locked"
        const val SOMETHING_WENT_WRONG_MSG = "Something went wrong. Please, try again later."
        const val BACK_LINK_WRONG_LAYOUT = "Wrong object layout, try again"
    }
}