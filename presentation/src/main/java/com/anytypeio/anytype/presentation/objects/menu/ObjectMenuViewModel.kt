package com.anytypeio.anytype.presentation.objects.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.templates.CreateTemplateFromObject
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCreateTemplateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDefaultTemplateEvent
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.SupportedLayouts.fileLayouts
import com.anytypeio.anytype.presentation.objects.SupportedLayouts.systemLayouts
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugGoroutinesShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.DebugTreeShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.MiddlewareShareDownloader
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectMenuViewModel(
    setObjectIsArchived: SetObjectIsArchived,
    addToFavorite: AddToFavorite,
    removeFromFavorite: RemoveFromFavorite,
    addBackLinkToObject: AddBackLinkToObject,
    delegator: Delegator<Action>,
    urlBuilder: UrlBuilder,
    dispatcher: Dispatcher<Payload>,
    menuOptionsProvider: ObjectMenuOptionsProvider,
    duplicateObject: DuplicateObject,
    createWidget: CreateWidget,
    private val debugTreeShareDownloader: DebugTreeShareDownloader,
    private val storage: Editor.Storage,
    private val analytics: Analytics,
    private val updateFields: UpdateFields,
    private val addObjectToCollection: AddObjectToCollection,
    private val createTemplateFromObject: CreateTemplateFromObject,
    private val setObjectDetails: SetObjectDetails,
    private val debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader,
    private val spaceManager: SpaceManager,
    private val deepLinkResolver: DeepLinkResolver,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ObjectMenuViewModelBase(
    setObjectIsArchived = setObjectIsArchived,
    addToFavorite = addToFavorite,
    removeFromFavorite = removeFromFavorite,
    addBackLinkToObject = addBackLinkToObject,
    duplicateObject = duplicateObject,
    delegator = delegator,
    urlBuilder = urlBuilder,
    dispatcher = dispatcher,
    analytics = analytics,
    menuOptionsProvider = menuOptionsProvider,
    addObjectToCollection = addObjectToCollection,
    debugGoroutinesShareDownloader = debugGoroutinesShareDownloader,
    createWidget = createWidget,
    spaceManager = spaceManager,
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
) {

    private val objectRestrictions = storage.objectRestrictions.current()

    override fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isTemplate: Boolean
    ): List<ObjectAction> = buildList {

        val wrapper = ObjectWrapper.Basic(storage.details.current().details[ctx]?.map.orEmpty())
        val layout = wrapper.layout

        if (!isTemplate) {
            if (isFavorite) {
                add(ObjectAction.REMOVE_FROM_FAVOURITE)
            } else {
                add(ObjectAction.ADD_TO_FAVOURITE)
            }
        }

        if (isArchived) {
            add(ObjectAction.RESTORE)
        } else {
            if (objectRestrictions.none { it == ObjectRestriction.DELETE }) {
                add(ObjectAction.DELETE)
            }
        }

        if (!isTemplate && !systemLayouts.contains(layout) && !fileLayouts.contains(layout)) {
            add(ObjectAction.CREATE_WIDGET)
        }

        if (isTemplate) {
            add(ObjectAction.SET_AS_DEFAULT)
        }

        if (!objectRestrictions.contains(ObjectRestriction.DUPLICATE) && !isTemplate) {
            add(ObjectAction.DUPLICATE)
        }

        add(ObjectAction.UNDO_REDO)

        val details = storage.details.current().details
        val objTypeId = details[ctx]?.type?.firstOrNull()
        val typeStruct = details[objTypeId]?.map
        val objType = typeStruct?.mapToObjectWrapperType()
        if (objType != null) {
            val isTemplateAllowed = objType.isTemplatesAllowed()
            if (isTemplateAllowed && !isTemplate) {
                add(ObjectAction.USE_AS_TEMPLATE)
            }
        }
        if (!isTemplate) {
            add(ObjectAction.LINK_TO)
            add(ObjectAction.COPY_LINK)
        }

        if (!isTemplate) {
            val root = storage.document.get().find { it.id == ctx }
            if (root != null) {
                if (root.fields.isLocked == true) {
                    add(ObjectAction.UNLOCK)
                } else {
                    add(ObjectAction.LOCK)
                }
            }
            add(ObjectAction.SEARCH_ON_PAGE)
        }
    }

    override fun onDiagnosticsClicked(ctx: Id) {
        jobs += viewModelScope.launch {
            debugTreeShareDownloader.stream(
                MiddlewareShareDownloader.Params(objectId = ctx, name = "$ctx.zip")
            ).collect { result ->
                result.fold(
                    onSuccess = { success ->
                        commands.emit(Command.ShareDebugTree(success.uri))
                    },
                    onLoading = {
                        sendToast(
                            "Do not go away from this menu and don't turn the screen off. " +
                                    "Tree diagnostic is started to collect."
                        )
                    },
                    onFailure = {
                        sendToast("Error while collecting tree diagnostics")
                        Timber.e(it, "Error while adding link from object to object")
                    }
                )
            }
        }
    }

    override fun onIconClicked(ctx: Id, space: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                try {
                    if (!isThisObjectLocked(ctx)) {
                        commands.emit(Command.OpenObjectIcons)
                    } else {
                        _toasts.emit("Your object is locked.")
                    }
                } catch (e: Exception) {
                    _toasts.emit("Something went wrong. Please, try again later.")
                }
            }
        }
    }

    override fun onCoverClicked(ctx: Id, space: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.DETAILS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                try {
                    if (!isThisObjectLocked(ctx)) {
                        commands.emit(Command.OpenObjectCover)
                    } else {
                        _toasts.emit("Your object is locked.")
                    }
                } catch (e: Exception) {
                    _toasts.emit("Something went wrong. Please, try again later.")
                }
            }
        }
    }

    override fun onLayoutClicked(ctx: Id, space: Id) {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.LAYOUT_CHANGE)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                try {
                    if (!isThisObjectLocked(ctx)) {
                        commands.emit(Command.OpenObjectLayout)
                    } else {
                        _toasts.emit("Your object is locked.")
                    }
                } catch (e: Exception) {
                    _toasts.emit("Something went wrong. Please, try again later.")
                }
            }
        }
    }

    override fun onRelationsClicked() {
        viewModelScope.launch {
            if (objectRestrictions.contains(ObjectRestriction.RELATIONS)) {
                _toasts.emit(NOT_ALLOWED)
            } else {
                commands.emit(Command.OpenObjectRelations)
            }
        }
    }

    override fun onActionClicked(ctx: Id, space: Id, action: ObjectAction) {
        when (action) {
            ObjectAction.DELETE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = true)
            }
            ObjectAction.DUPLICATE -> {
                proceedWithDuplication(
                    ctx = ctx,
                    space = space,
                    details = storage.details.current().details
                )
            }
            ObjectAction.RESTORE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = false)
            }
            ObjectAction.ADD_TO_FAVOURITE -> {
                proceedWithAddingToFavorites(ctx)
            }
            ObjectAction.REMOVE_FROM_FAVOURITE -> {
                proceedWithRemovingFromFavorites(ctx)
            }
            ObjectAction.LINK_TO -> {
                proceedWithLinkTo()
            }
            ObjectAction.COPY_LINK -> {
                val deeplink = deepLinkResolver.createObjectDeepLink(
                    obj = ctx,
                    space = SpaceId(space)
                )
                viewModelScope.launch { commands.emit(Command.ShareDeeplinkToObject(deeplink)) }
            }
            ObjectAction.UNLOCK -> {
                proceedWithUpdatingLockStatus(ctx, false)
            }
            ObjectAction.LOCK -> {
                proceedWithUpdatingLockStatus(ctx, true)
            }
            ObjectAction.SEARCH_ON_PAGE -> {
                viewModelScope.launch {
                    delegator.delegate(Action.SearchOnPage)
                }
                isDismissed.value = true
            }
            ObjectAction.UNDO_REDO -> {
                viewModelScope.launch {
                    delegator.delegate(Action.UndoRedo)
                }
                isDismissed.value = true
            }
            ObjectAction.USE_AS_TEMPLATE -> {
                proceedWithCreatingTemplateFromObject(
                    ctx = ctx,
                    space = space
                )
            }
            ObjectAction.SET_AS_DEFAULT -> {
                proceedWithSettingAsDefaultTemplate(ctx = ctx)
            }
            ObjectAction.CREATE_WIDGET -> {
                val details = storage.details.current().details[ctx]
                val wrapper = ObjectWrapper.Basic(details?.map ?: emptyMap())
                proceedWithCreatingWidget(obj = wrapper)
            }
            ObjectAction.MOVE_TO,
            ObjectAction.MOVE_TO_BIN,
            ObjectAction.DELETE_FILES -> {
                throw IllegalStateException("$action is unsupported")
            }
        }
    }

    private fun proceedWithSettingAsDefaultTemplate(ctx: Id) {
        val startTime = System.currentTimeMillis()
        val details = storage.details.current().details
        val objTemplate = ObjectWrapper.Basic(details[ctx]?.map ?: emptyMap())
        val targetObjectTypeId = objTemplate.targetObjectType ?: return
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = targetObjectTypeId,
                details = mapOf(Relations.DEFAULT_TEMPLATE_ID to ctx)
            )
            setObjectDetails.async(params).fold(
                onSuccess = {
                    val objType = details[targetObjectTypeId]?.map?.mapToObjectWrapperType()
                    sendAnalyticsDefaultTemplateEvent(analytics, objType, startTime)
                    _toasts.emit("The template was set as default")
                    isDismissed.value = true
                },
                onFailure = {
                    Timber.e(it, "Error while setting template as default")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    private fun proceedWithCreatingTemplateFromObject(ctx: Id, space: Id) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = CreateTemplateFromObject.Params(obj = ctx)
            createTemplateFromObject.async(params).fold(
                onSuccess = { template ->
                    sendAnalyticsCreateTemplateEvent(
                        analytics = analytics,
                        details = storage.details.current().details,
                        ctx = ctx,
                        startTime = startTime,
                        spaceParams = provideParams(SpaceId(space))
                    )
                    buildOpenTemplateCommand(
                        ctx = ctx,
                        space = space,
                        template = template
                    )
                    isDismissed.value = true
                },
                onFailure = {
                    Timber.e(it, "Error while creating template from object")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    private suspend fun buildOpenTemplateCommand(ctx: Id, space: Id, template: Id) {
        val details = storage.details.current().details
        val type = details[ctx]?.type?.firstOrNull()
        val typeStruct = details[type]?.map
        val objType = typeStruct?.mapToObjectWrapperType()
        if (objType != null) {
            val objTypeKey = objType.uniqueKey
            val command = Command.OpenTemplate(
                templateId = template,
                typeId = objType.id,
                typeKey = objTypeKey,
                typeName = objType.name.orEmpty(),
                space = space
            )
            commands.emit(command)
        } else {
            Timber.e("Error while opening template from object, type:$type hasn't key")
        }
    }

    private fun proceedWithUpdatingLockStatus(
        ctx: Id,
        isLocked: Boolean
    ) {
        val root = storage.document.get().find { it.id == ctx }
        if (root != null) {
            viewModelScope.launch {
                updateFields(
                    UpdateFields.Params(
                        context = ctx,
                        fields = listOf(
                            ctx to root.fields.copy(
                                map = root.fields.map.toMutableMap().apply {
                                    put(Block.Fields.IS_LOCKED_KEY, isLocked)
                                }
                            )
                        )
                    )
                ).proceed(
                    success = {
                        dispatcher.send(it)
                        if (isLocked) {
                            sendEvent(
                                analytics = analytics,
                                eventName = EventsDictionary.objectLock
                            )
                            _toasts.emit(OBJECT_IS_LOCKED_MSG).also {
                                isDismissed.value = true
                            }
                        } else {
                            sendEvent(
                                analytics = analytics,
                                eventName = EventsDictionary.objectUnlock
                            )
                            _toasts.emit(OBJECT_IS_UNLOCKED_MSG).also {
                                isDismissed.value = true
                            }
                        }
                    },
                    failure = {
                        Timber.e(it, "Error while updating lock-status for object")
                        _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                    }
                )
            }
        }
    }

    @Throws(Exception::class)
    fun isThisObjectLocked(ctx: Id): Boolean {
        val doc = storage.document.get().first { it.id == ctx }
        return doc.fields.isLocked ?: false
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val setObjectIsArchived: SetObjectIsArchived,
        private val duplicateObject: DuplicateObject,
        private val debugTreeShareDownloader: DebugTreeShareDownloader,
        private val addToFavorite: AddToFavorite,
        private val removeFromFavorite: RemoveFromFavorite,
        private val addBackLinkToObject: AddBackLinkToObject,
        private val urlBuilder: UrlBuilder,
        private val storage: Editor.Storage,
        private val analytics: Analytics,
        private val dispatcher: Dispatcher<Payload>,
        private val updateFields: UpdateFields,
        private val delegator: Delegator<Action>,
        private val menuOptionsProvider: ObjectMenuOptionsProvider,
        private val addObjectToCollection: AddObjectToCollection,
        private val createTemplateFromObject: CreateTemplateFromObject,
        private val setObjectDetails: SetObjectDetails,
        private val debugGoroutinesShareDownloader: DebugGoroutinesShareDownloader,
        private val createWidget: CreateWidget,
        private val spaceManager: SpaceManager,
        private val deepLinkResolver: DeepLinkResolver,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectMenuViewModel(
                setObjectIsArchived = setObjectIsArchived,
                duplicateObject = duplicateObject,
                debugTreeShareDownloader = debugTreeShareDownloader,
                addToFavorite = addToFavorite,
                removeFromFavorite = removeFromFavorite,
                addBackLinkToObject = addBackLinkToObject,
                urlBuilder = urlBuilder,
                storage = storage,
                analytics = analytics,
                dispatcher = dispatcher,
                updateFields = updateFields,
                delegator = delegator,
                menuOptionsProvider = menuOptionsProvider,
                addObjectToCollection = addObjectToCollection,
                createTemplateFromObject = createTemplateFromObject,
                setObjectDetails = setObjectDetails,
                debugGoroutinesShareDownloader = debugGoroutinesShareDownloader,
                createWidget = createWidget,
                spaceManager = spaceManager,
                deepLinkResolver = deepLinkResolver,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
            ) as T
        }
    }
}