package com.anytypeio.anytype.presentation.objects.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts.fileLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.systemLayouts
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dashboard.interactor.SetObjectListIsFavorite
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromObject
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.domain.templates.CreateTemplateFromObject
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.common.PayloadDelegator
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.extension.getTypeObject
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCreateTemplateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDefaultTemplateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsResolveObjectConflict
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.getProperType
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuViewModelBase.Command.ShareDeeplinkToObject
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugGoroutinesShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.DebugTreeShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.MiddlewareShareDownloader
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectMenuViewModel(
    addBackLinkToObject: AddBackLinkToObject,
    delegator: Delegator<Action>,
    urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val menuOptionsProvider: ObjectMenuOptionsProvider,
    duplicateObject: DuplicateObject,
    createWidget: CreateWidget,
    payloadDelegator: PayloadDelegator,
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
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val setObjectListIsFavorite: SetObjectListIsFavorite,
    private val setObjectIsArchived: SetObjectListIsArchived,
    private val fieldParser: FieldParser,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val getSpaceInviteLink: GetSpaceInviteLink,
    private val addToFeaturedRelations: AddToFeaturedRelations,
    private val removeFromFeaturedRelations: RemoveFromFeaturedRelations,
    private val userPermissionProvider: UserPermissionProvider,
    private val deleteRelationFromObject: DeleteRelationFromObject,
    private val showObject: GetObject,
    private val deleteWidget: DeleteWidget
) : ObjectMenuViewModelBase(
    setObjectIsArchived = setObjectIsArchived,
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
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
    payloadDelegator = payloadDelegator,
    setObjectListIsFavorite = setObjectListIsFavorite,
    fieldParser = fieldParser,
    spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
    deepLinkResolver = deepLinkResolver,
    getSpaceInviteLink = getSpaceInviteLink,
    showObject = showObject,
    deleteWidget = deleteWidget
) {

    init {
        Timber.i("ObjectMenuViewModel, init")
    }

    private val objectRestrictions = storage.objectRestrictions.current()

    val canBePublished = MutableStateFlow(false)

    fun onResolveWebPublishPermission(space: SpaceId) {
        viewModelScope.launch {
            val permission = userPermissionProvider.get(space = space)
            if (permission?.isOwnerOrEditor() == true) {
                canBePublished.value = true
            }
        }
    }

    override fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isTemplate: Boolean,
        isLocked: Boolean,
        isReadOnly: Boolean,
        isCurrentObjectPinned: Boolean
    ): List<ObjectAction> = buildList {

        val wrapper = storage.details.current().getObject(ctx)
        val layout = wrapper?.layout

        if (isReadOnly) {
            add(ObjectAction.COPY_LINK)
            add(ObjectAction.SEARCH_ON_PAGE)
            if (layout in fileLayouts) {
                add(ObjectAction.DOWNLOAD_FILE)
            }
        } else {

            if (isArchived) {
                add(ObjectAction.RESTORE)
            } else {
                if (objectRestrictions.none { it == ObjectRestriction.DELETE }) {
                    add(ObjectAction.MOVE_TO_BIN)
                }
            }

            if (!isTemplate && !systemLayouts.contains(layout) && !fileLayouts.contains(layout)) {
                if (isCurrentObjectPinned) {
                    add(ObjectAction.UNPIN)
                } else {
                    add(ObjectAction.PIN)
                }
            }

            if (!objectRestrictions.contains(ObjectRestriction.DUPLICATE)) {
                add(ObjectAction.DUPLICATE)
            }

            add(ObjectAction.UNDO_REDO)

            if (isTemplate) {
                add(ObjectAction.SET_AS_DEFAULT)
            }

            val objTypeId = wrapper?.getProperType()
            if (objTypeId != null) {
            val objType = storage.details.current().getTypeObject(objTypeId)
            if (objType != null) {
                val isTemplateAllowed = objType.isTemplatesAllowed()
                if (isTemplateAllowed && !isTemplate) {
                    add(ObjectAction.USE_AS_TEMPLATE)
                }
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

            if (layout in fileLayouts) {
                clear()
                add(ObjectAction.MOVE_TO_BIN)
                add(ObjectAction.DOWNLOAD_FILE)
                if (isCurrentObjectPinned) {
                    add(ObjectAction.UNPIN)
                } else {
                    add(ObjectAction.PIN)
                }
                add(ObjectAction.LINK_TO)
                add(ObjectAction.COPY_LINK)
            }
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

    override fun onDescriptionClicked(ctx: Id, space: Id) {
        viewModelScope.launch {
            if (userPermissionProvider.get(space = SpaceId(space))?.isOwnerOrEditor() != true) {
                _toasts.emit(NOT_ALLOWED)
                return@launch
            }
            val isDescriptionAlreadyInFeatured =
                storage.details.current().getObject(ctx)?.featuredRelations?.contains(
                    Relations.DESCRIPTION
                ) == true
            if (isDescriptionAlreadyInFeatured) {
                removeFromFeaturedRelations.async(
                    params = RemoveFromFeaturedRelations.Params(
                        ctx = ctx,
                        relations = listOf(Relations.DESCRIPTION)
                    )
                ).fold(
                    onSuccess = { payload ->
                        dispatcher.send(payload)
                        Timber.d("Description was removed from featured relations")
                    },
                    onFailure = {
                        Timber.e(it, "Error while removing description from featured relations")
                        _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                    }
                )
            } else {
                addToFeaturedRelations.async(
                    params = AddToFeaturedRelations.Params(
                        ctx = ctx,
                        relations = listOf(Relations.DESCRIPTION)
                    )
                ).fold(
                    onSuccess = { payload ->
                        dispatcher.send(payload)
                        Timber.d("Description was added to featured relations")
                    },
                    onFailure = {
                        Timber.e(it, "Error while adding description to featured relations")
                        _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                    }
                )
            }
        }
    }

    override fun onRelationsClicked() {
        viewModelScope.launch {
            commands.emit(Command.OpenObjectRelations)
        }
    }

    override fun onTemplateNamePrefillToggleClicked(ctx: Id, space: Id) {
        viewModelScope.launch {
            val currentValue =
                storage.details.current().getObject(ctx)?.templateNamePrefillType ?: 0
            val newValue =
                if (currentValue == 1) NAME_PREFILL_DISABLED else NAME_PREFILL_FROM_TEMPLATE

            setObjectDetails.async(
                SetObjectDetails.Params(
                    ctx = ctx,
                    details = mapOf(Relations.TEMPLATE_NAME_PREFILL_TYPE to newValue.toDouble())
                )
            ).fold(
                onSuccess = { payload ->
                    dispatcher.send(payload)
                },
                onFailure = { error ->
                    Timber.e(error, "Error updating template name prefill")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    override fun onActionClicked(ctx: Id, space: Id, action: ObjectAction) {
        when (action) {
            ObjectAction.MOVE_TO_BIN -> {
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
                viewModelScope.launch {
                    val link = proceedWithGeneratingObjectLink(
                        space = SpaceId(space),
                        ctx = ctx
                    )
                    commands.emit(ShareDeeplinkToObject(link))
                }
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
            ObjectAction.PIN -> {
                val wrapper = storage.details.current().getObject(ctx)
                if (wrapper != null) proceedWithCreatingWidget(obj = wrapper)
            }
            ObjectAction.UNPIN -> {
                proceedWithRemovingWidget()
            }
            ObjectAction.MOVE_TO,
            ObjectAction.DELETE,
            ObjectAction.DELETE_FILES -> {
                throw IllegalStateException("$action is unsupported")
            }

            ObjectAction.DOWNLOAD_FILE -> {
                viewModelScope.launch {
                    delegator.delegate(action = Action.DownloadCurrentObjectAsFile)
                }
                isDismissed.value = true
            }
        }
    }

    private fun proceedWithSettingAsDefaultTemplate(ctx: Id) {
        val startTime = System.currentTimeMillis()
        val objTemplate = storage.details.current().getObject(ctx)
        val targetObjectTypeId = objTemplate?.targetObjectType ?: return
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = targetObjectTypeId,
                details = mapOf(Relations.DEFAULT_TEMPLATE_ID to ctx)
            )
            setObjectDetails.async(params).fold(
                onSuccess = {
                    val objType = storage.details.current().getTypeObject(targetObjectTypeId)
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
                        spaceParams = provideParams(space)
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
        val type = storage.details.current().getObject(ctx)?.getProperType()
        if (type != null) {
            val objType = storage.details.current().getTypeObject(type)
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
        } else {
            Timber.e("Error while opening template from object, object hasn't type")
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

    override fun onResetToDefaultLayout(
        ctx: Id,
        space: Id
    ) {

        showLayoutConflictScreen.value = false

        val currentObject = storage.details.current().getObject(ctx)
        val featuredRelations = currentObject?.featuredRelations ?: emptyList()

        viewModelScope.launch {
            val params = DeleteRelationFromObject.Params(
                ctx = ctx,
                relations = listOf(
                    Relations.LEGACY_LAYOUT,
                    Relations.LAYOUT_ALIGN
                )
            )
            deleteRelationFromObject.async(
                params = params
            ).fold(
                onSuccess = { payload ->
                    dispatcher.send(payload)
                },
                onFailure = {
                    Timber.e(it, "Error while resetting layout to default")
                }
            )
        }

        viewModelScope.launch{

            val rootBlockFields = storage.document.get().find { it.id == ctx }?.fields

            val params = UpdateFields.Params(
                context = ctx,
                fields = listOf(Pair(ctx, rootBlockFields?.copy(
                    map = rootBlockFields.map.toMutableMap().apply {
                        put("width", null)
                    }
                ) ?: Block.Fields.empty()))
            )
            updateFields(params = params
            ).process(
                success = { payload ->
                    dispatcher.send(payload)
                },
                failure = {
                    Timber.e(it, "Error while resetting layout to default")
                }
            )
        }

        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = ctx,
                details = mapOf(
                    Relations.FEATURED_RELATIONS to featuredRelations.filter {
                        it == Relations.DESCRIPTION
                    }.map { it }
                )
            )
            setObjectDetails.async(params).fold(
                onSuccess = {
                    dispatcher.send(it)
                },
                onFailure = {
                    Timber.e(it, "Error while resetting layout to default")
                }
            )
        }

        viewModelScope.launch {
            sendAnalyticsResolveObjectConflict(
                analytics = analytics,
                spaceParams = provideParams(space)
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val duplicateObject: DuplicateObject,
        private val debugTreeShareDownloader: DebugTreeShareDownloader,
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
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val payloadDelegator: PayloadDelegator,
        private val setObjectListIsFavorite: SetObjectListIsFavorite,
        private val setObjectIsArchived: SetObjectListIsArchived,
        private val fieldParser: FieldParser,
        private val getSpaceInviteLink: GetSpaceInviteLink,
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val addToFeaturedRelations: AddToFeaturedRelations,
        private val removeFromFeaturedRelations: RemoveFromFeaturedRelations,
        private val userPermissionProvider: UserPermissionProvider,
        private val deleteRelationFromObject: DeleteRelationFromObject,
        private val showObject: GetObject,
        private val deleteWidget: DeleteWidget
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectMenuViewModel(
                setObjectIsArchived = setObjectIsArchived,
                duplicateObject = duplicateObject,
                debugTreeShareDownloader = debugTreeShareDownloader,
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
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                payloadDelegator = payloadDelegator,
                setObjectListIsFavorite = setObjectListIsFavorite,
                fieldParser = fieldParser,
                getSpaceInviteLink = getSpaceInviteLink,
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                addToFeaturedRelations = addToFeaturedRelations,
                removeFromFeaturedRelations = removeFromFeaturedRelations,
                userPermissionProvider = userPermissionProvider,
                deleteRelationFromObject = deleteRelationFromObject,
                showObject = showObject,
                deleteWidget = deleteWidget
            ) as T
        }
    }
}

private const val NAME_PREFILL_DISABLED = 0
private const val NAME_PREFILL_FROM_TEMPLATE = 1