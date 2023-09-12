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
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.UpdateFields
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.dashboard.interactor.AddToFavorite
import com.anytypeio.anytype.domain.dashboard.interactor.RemoveFromFavorite
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.templates.CreateTemplateFromObject
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCreateTemplateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDefaultTemplateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCreateTemplateEvent
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.DebugTreeShareDownloader
import com.anytypeio.anytype.presentation.util.downloader.MiddlewareShareDownloader
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
    private val debugTreeShareDownloader: DebugTreeShareDownloader,
    private val storage: Editor.Storage,
    private val analytics: Analytics,
    private val updateFields: UpdateFields,
    private val addObjectToCollection: AddObjectToCollection,
    private val createTemplateFromObject: CreateTemplateFromObject,
    private val setObjectDetails: SetObjectDetails
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
    addObjectToCollection = addObjectToCollection
) {

    private val objectRestrictions = storage.objectRestrictions.current()

    override fun buildActions(
        ctx: Id,
        isArchived: Boolean,
        isFavorite: Boolean,
        isProfile: Boolean,
        isTemplate: Boolean
    ): List<ObjectAction> = buildList {

        if (!isTemplate) {
            if (isFavorite) {
                add(ObjectAction.REMOVE_FROM_FAVOURITE)
            } else {
                add(ObjectAction.ADD_TO_FAVOURITE)
            }
        }
        if (!isProfile) {
            if (isArchived) {
                add(ObjectAction.RESTORE)
            } else {
                add(ObjectAction.DELETE)
            }
        }

        if (isTemplate) {
            add(ObjectAction.SET_AS_DEFAULT)
        }

        if (!isProfile && !objectRestrictions.contains(ObjectRestriction.DUPLICATE)) {
            add(ObjectAction.DUPLICATE)
        }

        add(ObjectAction.UNDO_REDO)

        val objTypeId = storage.details.current().details[ctx]?.type?.firstOrNull()
        storage.details.current().details[objTypeId]?.let { objType ->
            val objTypeWrapper = ObjectWrapper.Type(objType.map)
            val isTemplateAllowed = objTypeWrapper.isTemplatesAllowed()
            if (isTemplateAllowed && !isTemplate && !isProfile) {
                add(ObjectAction.USE_AS_TEMPLATE)
            }
        }
        if (!isTemplate) {
            add(ObjectAction.LINK_TO)
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
                MiddlewareShareDownloader.Params(hash = ctx, name = "$ctx.zip")
            ).collect { result ->
                result.fold(
                    onSuccess = { uri ->
                        commands.emit(Command.ShareDebugTree(uri))
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

    override fun onIconClicked(ctx: Id) {
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

    override fun onCoverClicked(ctx: Id) {
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

    override fun onLayoutClicked(ctx: Id) {
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

    override fun onActionClicked(ctx: Id, action: ObjectAction) {
        when (action) {
            ObjectAction.DELETE -> {
                proceedWithUpdatingArchivedStatus(ctx = ctx, isArchived = true)
            }
            ObjectAction.DUPLICATE -> {
                proceedWithDuplication(ctx = ctx)
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
                proceedWithCreatingTemplateFromObject(ctx)
            }
            ObjectAction.SET_AS_DEFAULT -> {
                proceedWithSettingAsDefaultTemplate(ctx = ctx)
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
        val objTemplate = ObjectWrapper.Basic(
            storage.details.current().details[ctx]?.map ?: emptyMap()
        )
        val targetObjectTypeId = objTemplate.targetObjectType ?: return
        val objType = ObjectWrapper.Type(
            storage.details.current().details[targetObjectTypeId]?.map ?: emptyMap()
        )
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = targetObjectTypeId,
                details = mapOf(Relations.DEFAULT_TEMPLATE_ID to ctx)
            )
            setObjectDetails.async(params).fold(
                onSuccess = {
                    sendAnalyticsDefaultTemplateEvent(analytics, objType, startTime)
                    _toasts.emit("Template is set as default")
                    isDismissed.value = true
                },
                onFailure = {
                    Timber.e(it, "Error while setting template as default")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    private fun proceedWithCreatingTemplateFromObject(ctx: Id) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = CreateTemplateFromObject.Params(obj = ctx)
            createTemplateFromObject.async(params).fold(
                onSuccess = { template ->
                    val objTypeId = storage.details.current().details[ctx]?.type?.firstOrNull()
                    sendAnalyticsCreateTemplateEvent(analytics, objTypeId, startTime)
                    commands.emit(buildOpenTemplateCommand(ctx, template))
                    isDismissed.value = true
                },
                onFailure = {
                    Timber.e(it, "Error while creating template from object")
                    _toasts.emit(SOMETHING_WENT_WRONG_MSG)
                }
            )
        }
    }

    private fun buildOpenTemplateCommand(ctx: Id, template: Id): Command.OpenTemplate {
        val type = storage.details.current().details[ctx]?.type?.firstOrNull()
        val objType =
            ObjectWrapper.Basic(storage.details.current().details[type]?.map ?: emptyMap())
        return Command.OpenTemplate(
            template = template,
            icon = ObjectIcon.from(objType, objType.layout, urlBuilder),
            typeName = objType.getProperName()
        )
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
    class Factory(
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
        private val setObjectDetails: SetObjectDetails
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
                setObjectDetails = setObjectDetails
            ) as T
        }
    }
}