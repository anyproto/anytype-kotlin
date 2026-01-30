package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectRelationFeature
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectRelationUnfeature
import com.anytypeio.anytype.analytics.base.EventsDictionary.relationsScreenShow
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromObject
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.extension.getStruct
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsShowObjectTypeScreen
import com.anytypeio.anytype.presentation.objects.LockedStateProvider
import com.anytypeio.anytype.presentation.objects.getTypeForObjectAndTargetTypeForTemplate
import com.anytypeio.anytype.presentation.objects.isTemplateObject
import com.anytypeio.anytype.presentation.relations.model.RelationOperationError
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationListProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationListViewModel(
    private val vmParams: VmParams,
    private val objectRelationListProvider: ObjectRelationListProvider,
    private val lockedStateProvider: LockedStateProvider,
    private val urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDetail: UpdateDetail,
    private val addToFeaturedRelations: AddToFeaturedRelations,
    private val removeFromFeaturedRelations: RemoveFromFeaturedRelations,
    private val deleteRelationFromObject: DeleteRelationFromObject,
    private val analytics: Analytics,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val addRelationToObject: AddRelationToObject,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val userPermissionProvider: UserPermissionProvider,
    private val setObjectTypeRecommendedFields: SetObjectTypeRecommendedFields
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val isEditMode = MutableStateFlow(false)

    private val jobs = mutableListOf<Job>()
    private var _currentObjectTypeId: Id? = null

    val commands = MutableSharedFlow<Command>(replay = 0)
    val views = MutableStateFlow<List<Model>>(emptyList())
    val showLocalInfo = MutableStateFlow(false)
    val uiSettingsIconState = MutableStateFlow<UiPropertiesSettingsIconState>(UiPropertiesSettingsIconState.Hidden)

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    init {
        Timber.i("RelationListViewModel, init")
    }

    fun onStartListMode(ctx: Id) {
        Timber.d("onStartListMode, ctx: $ctx")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = relationsScreenShow
        )
        jobs += viewModelScope.launch {
            combine(
                storeOfObjectTypes.trackChanges(),
                storeOfRelations.trackChanges(),
                objectRelationListProvider.details
            ) { _, _, details ->
                parseToViews(ctx, details)
            }.collect { views.value = it }
        }
    }

    private suspend fun parseToViews(
        ctx: Id,
        details: ObjectViewDetails
    ): List<Model> {

        val currentObj = details.getObject(ctx)
        if (currentObj == null) {
            Timber.e("Object with id $ctx not found.")
            return emptyList()
        }
        val objType = currentObj.getTypeForObjectAndTargetTypeForTemplate(storeOfObjectTypes)

        _currentObjectTypeId = objType?.id

        uiSettingsIconState.value =
            if (objType == null || objType.isDeleted == true || currentObj.isTemplateObject(storeOfObjectTypes)) {
                UiPropertiesSettingsIconState.Hidden
            } else {
                UiPropertiesSettingsIconState.Shown
            }

        val parsedFields = fieldParser.getObjectParsedProperties(
            objectType = objType,
            storeOfRelations = storeOfRelations,
            objPropertiesKeys = details.getObject(ctx)?.map?.keys?.toList().orEmpty()
        )

        val headerFields = parsedFields.header.mapNotNull {
            if (it.key == Relations.DESCRIPTION) return@mapNotNull null
            it.view(
                details = details,
                values = details.getObject(ctx)?.map.orEmpty(),
                urlBuilder = urlBuilder,
                fieldParser = fieldParser,
                isFeatured = true,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }.map {
            Model.Item(it, isLocal = false)
        }

        val hiddenFields = parsedFields.hidden.mapNotNull {
            if (it.key == Relations.DESCRIPTION) return@mapNotNull null
            it.view(
                details = details,
                values = details.getObject(ctx)?.map.orEmpty(),
                urlBuilder = urlBuilder,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }.map {
            Model.Item(it, isLocal = false)
        }

        val sidebarFields = parsedFields.sidebar.mapNotNull {
            if (it.key == Relations.DESCRIPTION) return@mapNotNull null
            it.view(
                details = details,
                values = details.getObject(ctx)?.map.orEmpty(),
                urlBuilder = urlBuilder,
                fieldParser = fieldParser,
                isFeatured = false,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }.map {
            Model.Item(it, isLocal = false)
        }

        val filesFields = parsedFields.file.mapNotNull {
            if (it.key == Relations.DESCRIPTION) return@mapNotNull null
            it.view(
                details = details,
                values = details.getObject(ctx)?.map.orEmpty(),
                urlBuilder = urlBuilder,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }.map {
            Model.Item(it, isLocal = false)
        }

        val localFields = parsedFields.local.mapNotNull {
            if (it.key == Relations.DESCRIPTION) return@mapNotNull null
            it.view(
                details = details,
                values = details.getObject(ctx)?.map.orEmpty(),
                urlBuilder = urlBuilder,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }.map {
            Model.Item(it, isLocal = true)
        }

        return buildList {
            if (headerFields.isNotEmpty()) {
                addAll(headerFields)
            }

            //todo file fields are off for now
            if (false) {
                if (sidebarFields.isNotEmpty() || filesFields.isNotEmpty()) {
                    addAll(sidebarFields + filesFields)
                }
            } else {
                if (sidebarFields.isNotEmpty()) {
                    addAll(sidebarFields)
                }
            }

            if (hiddenFields.isNotEmpty()) {
                val currentHiddenState = views.value.firstOrNull { it is Model.Section.Hidden }
                if (currentHiddenState is Model.Section.Hidden.Shown) {
                    add(
                        Model.Section.Hidden.Shown(items = hiddenFields)
                    )
                    addAll(hiddenFields)
                } else {
                    if (headerFields.isEmpty() && sidebarFields.isEmpty() && localFields.isEmpty()) {
                        add(Model.Section.Empty)
                    }
                    add(
                        Model.Section.Hidden.Unshown(items = hiddenFields)
                    )
                }
            } else {
                if (headerFields.isEmpty() && sidebarFields.isEmpty() && localFields.isEmpty()) {
                    add(Model.Section.Empty)
                }
            }

            if (localFields.isNotEmpty()) {
                val currentLocalState = views.value.firstOrNull { it is Model.Section.Local }
                if (currentLocalState is Model.Section.Local.Shown) {
                    add(
                        Model.Section.Local.Shown(items = localFields)
                    )
                    addAll(localFields)
                } else {
                    add(
                        Model.Section.Local.Unshown(items = localFields)
                    )
                }
            }
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    fun onDismissLocalInfo() {
        showLocalInfo.value = false
    }

    fun onShowLocalInfo() {
        showLocalInfo.value = true
    }

    fun onTypeIconClicked() {
        val objTypeId = _currentObjectTypeId ?: return
        viewModelScope.launch {
            commands.emit(Command.NavigateToObjectType(objTypeId))
        }
        viewModelScope.launch {
            sendAnalyticsShowObjectTypeScreen(
                route = EventsDictionary.Routes.objectRoute,
                analytics = analytics,
                spaceParams = provideParams(vmParams.spaceId.id),
            )
        }
    }

    fun onAddToTypeClicked(item: Model.Item) {
        val currentObjTypeId = _currentObjectTypeId ?: return
        viewModelScope.launch {
            val objType = storeOfObjectTypes.get(currentObjTypeId)
            if (objType != null) {
                val params = SetObjectTypeRecommendedFields.Params(
                    objectTypeId = objType.id,
                    fields = objType.recommendedRelations + listOf(item.view.id)
                )
                setObjectTypeRecommendedFields.async(params).fold(
                    onFailure = { Timber.e(it, "Error while setting recommended fields") },
                    onSuccess = {
                        Timber.d("Successfully set recommended fields")
                    }
                )
            }
        }
    }

    fun onHiddenToggle(item: Model.Section.Hidden) {
        val currentList = views.value

        val index = currentList.indexOfFirst {
            it is Model.Section.Hidden
        }
        if (index == -1) return

        val newList = currentList.toMutableList()

        when (item) {
            is Model.Section.Hidden.Shown -> {
                newList[index] = Model.Section.Hidden.Unshown(item.items)
                repeat(item.items.size) {
                    if (newList.size > index + 1) {
                        newList.removeAt(index + 1)
                    }
                }
            }
            is Model.Section.Hidden.Unshown -> {
                newList[index] = Model.Section.Hidden.Shown(item.items)
                newList.addAll(index + 1, item.items)
            }
        }
        views.value = newList
    }

    fun onLocalToggle(item: Model.Section.Local) {
        val currentList = views.value

        val index = currentList.indexOfFirst {
            it is Model.Section.Local
        }
        if (index == -1) return

        val newList = currentList.toMutableList()

        when (item) {
            is Model.Section.Local.Shown -> {
                newList[index] = Model.Section.Local.Unshown(item.items)
                repeat(item.items.size) {
                    if (newList.size > index + 1) {
                        newList.removeAt(index + 1)
                    }
                }
            }
            is Model.Section.Local.Unshown -> {
                newList[index] = Model.Section.Local.Shown(item.items)
                newList.addAll(index + 1, item.items)
            }
        }
        views.value = newList
    }

    fun onRemoveFromObjectClicked(item: Model.Item) {
        onDeleteClicked(
            ctx = vmParams.objectId,
            view = item.view
        )
    }

    fun onRelationClicked(ctx: Id, target: Id?, view: ObjectRelationView) {
        Timber.d("onRelationClicked, ctx: $ctx, target: $target, view: $view")
        viewModelScope.launch {
            if (checkRelationIsInObject(view)) {
                onRelationClickedListMode(ctx, view)
            } else {
                proceedWithAddingRelationToObject(ctx, view) {
                    onRelationClickedListMode(ctx, view)
                }
            }
        }
    }

    fun onCheckboxClicked(ctx: Id, view: ObjectRelationView) {
        Timber.d("onCheckboxClicked, ctx: $ctx, view: $view")
        val isLocked = resolveIsLockedStateOrDetailsRestriction(ctx)
        if (isLocked) {
            sendToast(RelationOperationError.LOCKED_OBJECT_MODIFICATION_ERROR)
            return
        }
        viewModelScope.launch {
            if (checkRelationIsInObject(view)) {
                proceedWithUpdatingFeaturedRelations(view, ctx)
            } else {
                proceedWithAddingRelationToObject(ctx, view) {
                    proceedWithUpdatingFeaturedRelations(view, ctx)
                }
            }
        }
    }

    private fun checkRelationIsInObject(view: ObjectRelationView): Boolean {
        val objectRelations =
            objectRelationListProvider.getDetails().getStruct(vmParams.objectId)?.keys
        return objectRelations?.any { it == view.key } == true
    }

    private suspend fun proceedWithAddingRelationToObject(
        ctx: Id,
        view: ObjectRelationView,
        success: () -> Unit
    ) {
        val params = AddRelationToObject.Params(
            ctx = ctx,
            relationKey = view.key
        )
        addRelationToObject.async(params).fold(
            onFailure = { Timber.e(it, "Error while adding relation to object") },
            onSuccess = { payload ->
                if (payload != null) dispatcher.send(payload)
                analytics.sendAnalyticsRelationEvent(
                    eventName = EventsDictionary.relationAdd,
                    storeOfRelations = storeOfRelations,
                    relationKey = view.key,
                    spaceParams = provideParams(vmParams.spaceId.id)
                )
                success.invoke()
            }
        )
    }

    private fun proceedWithUpdatingFeaturedRelations(
        view: ObjectRelationView,
        ctx: Id
    ) {
        val relationKey = view.key
        viewModelScope.launch {
            if (view.featured) {
                viewModelScope.launch {
                    removeFromFeaturedRelations.async(
                        RemoveFromFeaturedRelations.Params(
                            ctx = ctx,
                            relations = listOf(relationKey)
                        )
                    ).fold(
                        onFailure = { Timber.e(it, "Error while removing from featured relations") },
                        onSuccess = {
                            dispatcher.send(it)
                            analytics.sendAnalyticsRelationEvent(
                                eventName = objectRelationUnfeature,
                                storeOfRelations = storeOfRelations,
                                relationKey = relationKey,
                                spaceParams = provideParams(vmParams.spaceId.id)
                            )
                        }
                    )
                }
            } else {
                viewModelScope.launch {
                    addToFeaturedRelations.async(
                        AddToFeaturedRelations.Params(
                            ctx = ctx,
                            relations = listOf(relationKey)
                        )
                    ).fold(
                        onFailure = { Timber.e(it, "Error while adding to featured relations") },
                        onSuccess = {
                            dispatcher.send(it)
                            analytics.sendAnalyticsRelationEvent(
                                eventName = objectRelationFeature,
                                storeOfRelations = storeOfRelations,
                                relationKey = relationKey,
                                spaceParams = provideParams(vmParams.spaceId.id)
                            )
                        }
                    )
                }
            }
        }
    }

    fun onDeleteClicked(ctx: Id, view: ObjectRelationView) {
        viewModelScope.launch {
            val params = DeleteRelationFromObject.Params(
                ctx = ctx,
                relations = listOf(view.key)
            )
            deleteRelationFromObject.async(params).fold(
                onFailure = { Timber.e(it, "Error while deleting relation") },
                onSuccess = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = EventsDictionary.relationDelete,
                        storeOfRelations = storeOfRelations,
                        relationKey = view.key,
                        spaceParams = provideParams(vmParams.spaceId.id)
                    )
                }
            )
        }
    }

    fun onEditOrDoneClicked(isLocked: Boolean) {
        //todo legacy, remove
//        if (isLocked) {
//            sendToast(RelationOperationError.LOCKED_OBJECT_MODIFICATION_ERROR)
//        } else {
//            isEditMode.value = !isEditMode.value
//            views.value = views.value.map { view ->
//                if (view is Model.Item && !view.isRecommended) {
//                    view.copy(isRemovable = isPossibleToRemoveRelation(view.view))
//                } else {
//                    view
//                }
//            }
//        }
    }

    private fun isPossibleToRemoveRelation(view: ObjectRelationView): Boolean {
        return isEditMode.value && !view.system
    }

    private suspend fun onRelationClickedAddMode(
        target: Id?,
        view: ObjectRelationView
    ) {
        checkNotNull(target)
        commands.emit(
            Command.SetRelationKey(
                blockId = target,
                key = view.key
            )
        )
    }

    private fun onRelationClickedListMode(ctx: Id, view: ObjectRelationView) {
        viewModelScope.launch {
            val relation = storeOfRelations.getById(view.id)
            if (relation == null) {
                if (BuildConfig.DEBUG) {
                    _toasts.emit("$NOT_FOUND_IN_RELATION_STORE[${view.id}]")
                }
                Timber.w("Couldn't find relation in store by id:${view.id}")
                return@launch
            }
            val isLocked = resolveIsLockedStateOrDetailsRestriction(ctx)
            when (relation.format) {
                RelationFormat.SHORT_TEXT,
                RelationFormat.LONG_TEXT,
                RelationFormat.NUMBER,
                RelationFormat.URL,
                RelationFormat.EMAIL,
                RelationFormat.PHONE -> {
                    commands.emit(
                        Command.EditTextRelationValue(
                            ctx = ctx,
                            relationId = relation.id,
                            relationKey = relation.key,
                            target = ctx,
                            isLocked = isLocked
                        )
                    )
                }

                RelationFormat.CHECKBOX -> {
                    if (isLocked || relation.isReadonlyValue) {
                        _toasts.emit(NOT_ALLOWED_FOR_RELATION)
                        Timber.d("No interaction allowed with this relation")
                        return@launch
                    }
                    proceedWithTogglingRelationCheckboxValue(view, ctx)
                }

                RelationFormat.DATE -> {
                    if (view.readOnly || isLocked) {
                        handleReadOnlyValue(view, relation, ctx, isLocked)
                    } else {
                        openRelationDateScreen(relation, ctx, isLocked)
                    }
                }

                RelationFormat.TAG, RelationFormat.STATUS -> {
                    commands.emit(
                        Command.EditTagOrStatusRelationValue(
                            ctx = ctx,
                            relationId = relation.id,
                            relationKey = relation.key,
                            target = ctx,
                            isLocked = isLocked
                        )
                    )
                }

                RelationFormat.FILE,
                RelationFormat.OBJECT -> {
                    commands.emit(
                        Command.EditFileObjectRelationValue(
                            ctx = ctx,
                            relationId = relation.id,
                            relationKey = relation.key,
                            target = ctx,
                            targetObjectTypes = relation.relationFormatObjectTypes,
                            isLocked = isLocked
                        )
                    )
                }

                RelationFormat.EMOJI,
                RelationFormat.RELATIONS,
                RelationFormat.UNDEFINED -> {
                    _toasts.emit(NOT_SUPPORTED_UPDATE_VALUE)
                    Timber.d("Update value of relation with format:[${relation.format}] is not supported")
                }

                else -> {}
            }
        }
    }

    private suspend fun handleReadOnlyValue(
        view: ObjectRelationView,
        relation: ObjectWrapper.Relation,
        ctx: Id,
        isLocked: Boolean
    ) {
        val timeInMillis =
            (view as? ObjectRelationView.Date)?.relativeDate?.initialTimeInMillis
        if ((timeInMillis != null)) {
            fieldParser.getDateObjectByTimeInSeconds(
                timeInSeconds = timeInMillis / 1000,
                spaceId = vmParams.spaceId,
                actionSuccess = { obj ->
                    commands.emit(Command.NavigateToDateObject(obj.id))
                },
                actionFailure = {
                    Timber.e(it, "Failed to get date object by timestamp")
                    openRelationDateScreen(relation, ctx, isLocked)
                }
            )
        } else {
            openRelationDateScreen(relation, ctx, isLocked)
        }
    }

    private suspend fun openRelationDateScreen(
        relation: ObjectWrapper.Relation,
        ctx: Id,
        isLocked: Boolean
    ) {
        commands.emit(
            Command.EditDateRelationValue(
                ctx = ctx,
                relationId = relation.id,
                relationKey = relation.key,
                target = ctx,
                isLocked = isLocked
            )
        )
    }

    private fun resolveIsLockedStateOrDetailsRestriction(ctx: Id): Boolean =
        permission.value?.isOwnerOrEditor() != true ||
                lockedStateProvider.isLocked(ctx) ||
                lockedStateProvider.isContainsDetailsRestriction()

    private fun proceedWithTogglingRelationCheckboxValue(view: ObjectRelationView, ctx: Id) {
        viewModelScope.launch {
            check(view is ObjectRelationView.Checkbox)
            updateDetail(
                UpdateDetail.Params(
                    target = ctx,
                    key = view.key,
                    value = !view.isChecked
                )
            ).process(
                success = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = EventsDictionary.relationChangeValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = view.key,
                        spaceParams = provideParams(vmParams.spaceId.id)
                    )
                },
                failure = { Timber.e(it, "Error while updating checkbox relation") }
            )
        }
    }

    fun onRelationTextValueChanged(
        ctx: Id,
        value: Any?,
        relationKey: Key,
        isValueEmpty: Boolean
    ) {
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    target = ctx,
                    key = relationKey,
                    value = value
                )
            ).process(
                success = { payload ->
                    if (payload.events.isNotEmpty()) dispatcher.send(payload)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = if (isValueEmpty) EventsDictionary.relationDeleteValue
                        else EventsDictionary.relationChangeValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = relationKey,
                        spaceParams = provideParams(vmParams.spaceId.id)
                    )
                },
                failure = { Timber.e(it, "Error while updating relation values") }
            )
        }
    }

    fun onOpenDateObjectByTimeInMillis(timeInMillis: TimeInMillis) {
        Timber.d("onOpenDateObjectByTimeInMillis, timeInMillis: $timeInMillis")
        viewModelScope.launch {
            fieldParser.getDateObjectByTimeInSeconds(
                timeInSeconds = timeInMillis / 1000,
                spaceId = vmParams.spaceId,
                actionSuccess = { obj ->
                    commands.emit(Command.NavigateToDateObject(obj.id))
                },
                actionFailure = {
                    Timber.e(it, "Failed to get date object by timestamp")
                    _toasts.emit("Failed to get date object by timestamp")
                }
            )
        }
    }

    sealed class Model : DefaultObjectDiffIdentifier {
        sealed class Section : Model() {
            data object Header : Section() {
                override val identifier: String get() = "Section_Header"
            }

            data object SideBar : Section() {
                override val identifier: String get() = "Section_SideBar"
            }

            sealed class Local : Section() {
                data class Shown(
                    val items: List<Model.Item>
                ) : Local() {
                    override val identifier: String get() = "Section_Local_Shown"
                }

                data class Unshown(
                    val items: List<Model.Item>
                ) : Local() {
                    override val identifier: String get() = "Section_Local_Unshown"
                }
            }

            sealed class Hidden : Section() {
                data class Shown(
                    val items: List<Model.Item>
                ) : Hidden() {
                    override val identifier: String get() = "Section_Hidden_Shown"
                }

                data class Unshown(
                    val items: List<Model.Item>
                ) : Hidden() {
                    override val identifier: String get() = "Section_Hidden_Unshown"
                }
            }

            data object Empty : Section() {
                override val identifier: String get() = "Section_Empty"
            }
        }

        data class Item(
            val view: ObjectRelationView,
            val isLocal: Boolean
        ) : Model() {
            override val identifier: String get() = view.identifier
        }
    }

    sealed class Command {
        data class EditTextRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id,
            val isLocked: Boolean = false
        ) : Command()

        data class EditDateRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id,
            val isLocked: Boolean = false
        ) : Command()

        data class EditFileObjectRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id,
            val targetObjectTypes: List<Id>,
            val isLocked: Boolean = false
        ) : Command()

        data class EditTagOrStatusRelationValue(
            val ctx: Id,
            val relationId: Id,
            val relationKey: Key,
            val target: Id,
            val isLocked: Boolean = false
        ) : Command()

        data class SetRelationKey(
            val blockId: Id,
            val key: Id
        ) : Command()

        data class NavigateToDateObject(
            val objectId: Id
        ) : Command()

        data class NavigateToObjectType(
            val objectTypeId: Id
        ) : Command()
    }

    data class VmParams(
        val objectId: Id,
        val spaceId: SpaceId
    )

    companion object {
        const val NOT_ALLOWED_FOR_RELATION = "Not allowed for this relation"
        const val NOT_FOUND_IN_RELATION_STORE = "Couldn't find in relation store by id:"
        const val NOT_SUPPORTED_UPDATE_VALUE = "Update value of this relation isn't supported"
    }
}

sealed class UiPropertiesSettingsIconState {
    data object Hidden : UiPropertiesSettingsIconState()
    data object Shown : UiPropertiesSettingsIconState()
}