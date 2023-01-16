package com.anytypeio.anytype.presentation.sets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.ext.addIds
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.model.RelationOperationError
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.CopyFileStatus
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.OnCopyFileToCacheAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class RelationValueBaseViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val details: ObjectDetailProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val copyFileToCache: CopyFileToCacheDirectory,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectDetails: UpdateDetail,
    private val analytics: Analytics,
    private val addFileToObject: AddFileToObject
) : BaseViewModel() {

    val navigation = MutableSharedFlow<AppNavigation.Command>()

    private val jobs = mutableListOf<Job>()
    private var relationFormat: Relation.Format? = null

    val isEditing = MutableStateFlow(false)
    val isDismissed = MutableStateFlow(false)
    val name = MutableStateFlow("")
    val views = MutableStateFlow(listOf<RelationValueView>())
    val commands = MutableSharedFlow<ObjectRelationValueCommand>(replay = 0)
    val isLoading = MutableStateFlow(false)

    fun onStart(objectId: Id, relationKey: Key) {
        Timber.d("onStart")
        jobs += viewModelScope.launch {
            combine(
                relations.observe(relationKey),
                values.subscribe(objectId)
            ) { relation, record ->
                initViewState(
                    relation = relation,
                    record = record,
                    relationKey = relationKey
                )
            }.collect()
        }
    }

    fun onStop() {
        Timber.d("onStop")
        jobs.cancel()
    }

    private suspend fun initViewState(
        relation: ObjectWrapper.Relation,
        record: Map<String, Any?>,
        relationKey: Key
    ) {
        val result = mutableListOf<RelationValueView>()
        val items = mutableListOf<RelationValueView>()

        when (relation.format) {
            Relation.Format.TAG -> {
                relationFormat = Relation.Format.TAG
                val isRemovable = isEditing.value
                val ids: List<Id> = when (val value = record[relationKey]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                items.addAll(
                    parseTagRelationValues(
                        ids = ids,
                        isRemovable = isRemovable,
                        relationKey = relationKey
                    )
                )
            }
            Relation.Format.STATUS -> {
                relationFormat = Relation.Format.STATUS
                val ids: List<Id> = when (val value = record[relationKey]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                items.addAll(
                    parseStatusRelationValues(
                        ids = ids,
                        relationKey = relationKey
                    )
                )
            }
            Relation.Format.OBJECT -> {
                val isRemovable = isEditing.value
                relationFormat = Relation.Format.OBJECT
                val value = record.getOrDefault(relationKey, null)
                if (value is List<*>) {
                    value.typeOf<Id>().forEach { id ->
                        val wrapper = resolveWrapperForObject(id)
                        val type = wrapper.type.firstOrNull()
                        val objectType = type?.let { storeOfObjectTypes.get(it) }
                        if (wrapper.isDeleted == true) {
                            items.add(
                                RelationValueView.Object.NonExistent(
                                    id = id,
                                    removable = isRemovable
                                )
                            )
                        } else {
                            items.add(
                                RelationValueView.Object.Default(
                                    id = id,
                                    name = wrapper.getProperName(),
                                    typeName = objectType?.name,
                                    type = type,
                                    icon = ObjectIcon.from(
                                        obj = wrapper,
                                        layout = wrapper.layout,
                                        builder = urlBuilder
                                    ),
                                    removable = isRemovable,
                                    layout = wrapper.layout
                                )
                            )
                        }
                    }
                } else if (value is Id) {
                    val wrapper = resolveWrapperForObject(value)
                    val type = wrapper.type.firstOrNull()
                    val objectType = type?.let { storeOfObjectTypes.get(it) }
                    if (wrapper.isDeleted == true) {
                        items.add(
                            RelationValueView.Object.NonExistent(
                                id = value,
                                removable = isRemovable
                            )
                        )
                    } else {
                        items.add(
                            RelationValueView.Object.Default(
                                id = value,
                                name = wrapper.getProperName(),
                                typeName = objectType?.name,
                                type = type,
                                icon = ObjectIcon.from(
                                    obj = wrapper,
                                    layout = wrapper.layout,
                                    builder = urlBuilder
                                ),
                                removable = isRemovable,
                                layout = wrapper.layout
                            )
                        )
                    }
                }
            }
            RelationFormat.FILE -> {
                relationFormat = RelationFormat.FILE
                val isRemovable = isEditing.value
                val value = record.getOrDefault(relation.key, null)
                if (value != null) {
                    val ids = buildList {
                        when (value) {
                            is List<*> -> addAll(value.typeOf())
                            is Id -> add(value)
                        }
                    }
                    ids.forEach { id ->
                        val detail = details.provide()[id]
                        items.add(
                            RelationValueView.File(
                                id = id,
                                name = detail?.name.orEmpty(),
                                mime = detail?.fileMimeType.orEmpty(),
                                ext = detail?.fileExt.orEmpty(),
                                image = detail?.iconImage,
                                removable = isRemovable
                            )
                        )
                    }
                }
            }
            else -> throw IllegalStateException("Unsupported format: ${relation.format}")
        }

        result.addAll(items)

        if (result.isEmpty()) {
            result.add(RelationValueView.Empty)
        }

        views.value = result
        name.value = relation.name.orEmpty()
    }

    abstract suspend fun parseStatusRelationValues(
        ids: List<Id>,
        relationKey: Key
    ): List<RelationValueView.Option.Status>

    abstract suspend fun parseTagRelationValues(
        ids: List<Id>,
        isRemovable: Boolean,
        relationKey: Key
    ): List<RelationValueView.Option.Tag>

    open suspend fun resolveWrapperForObject(id: Id): ObjectWrapper.Basic {
        val detail = details.provide()[id]
        if (detail == null || detail.map.isEmpty()) {
            Timber.w("Could not found data for object: $id")
        }
        return ObjectWrapper.Basic(detail?.map ?: emptyMap())
    }

    fun onEditOrDoneClicked(isLocked: Boolean) {
        if (isLocked) {
            sendToast(RelationOperationError.LOCKED_OBJECT_MODIFICATION_ERROR)
        } else {
            isEditing.value = !isEditing.value
            views.value = views.value.map { v ->
                when (v) {
                    is RelationValueView.Object.Default -> v.copy(removable = isEditing.value)
                    is RelationValueView.Object.NonExistent -> v.copy(removable = isEditing.value)
                    is RelationValueView.Option.Tag -> v.copy(removable = isEditing.value)
                    is RelationValueView.Option.Status -> v.copy(removable = isEditing.value)
                    is RelationValueView.File -> v.copy(removable = isEditing.value)
                    else -> v
                }
            }
        }
    }

    fun onAddValueClicked(isLocked: Boolean) {
        if (isLocked) {
            sendToast(RelationOperationError.LOCKED_OBJECT_MODIFICATION_ERROR)
        } else {
            when (relationFormat) {
                Relation.Format.STATUS,
                Relation.Format.TAG -> {
                    viewModelScope.launch {
                        commands.emit(ObjectRelationValueCommand.ShowAddStatusOrTagScreen)
                    }
                }
                Relation.Format.FILE -> {
                    viewModelScope.launch {
                        commands.emit(ObjectRelationValueCommand.ShowFileValueActionScreen)
                    }
                }
                Relation.Format.OBJECT -> {
                    viewModelScope.launch {
                        commands.emit(ObjectRelationValueCommand.ShowAddObjectScreen)
                    }
                }
                else -> {
                }
            }
        }
    }

    fun onRemoveTagFromObjectClicked(
        target: Id,
        relationKey: Key,
        tag: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relationKey].filterIdsById(tag)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relationKey,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing tag from object") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    fun onRemoveStatusFromObjectClicked(
        target: Id,
        relationKey: Key,
        status: Id? = null
    ) {
        viewModelScope.launch {
            val statusId = status ?: ((views.value.first {
                it is RelationValueView.Option.Status
            }) as? RelationValueView.Option.Status)?.id ?: return@launch
            val obj = values.get(target)
            val remaining = obj[relationKey].filterIdsById(statusId)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relationKey,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing status from object") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    fun onRemoveObjectFromObjectClicked(
        target: Id,
        relationKey: Key,
        objectId: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relationKey].filterIdsById(objectId)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relationKey,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing object from object") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    fun onRemoveFileFromObjectClicked(
        target: Id,
        relationKey: Key,
        fileId: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relationKey].filterIdsById(fileId)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relationKey,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing file from object") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    fun onObjectValueOrderChanged(
        target: Id,
        relationKey: Key,
        order: List<Id>
    ) {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relationKey,
                    value = order
                )
            ).process(
                failure = { Timber.e(it, "Error while updating object value order") },
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    fun onFileValueActionAddClicked() {
        viewModelScope.launch {
            commands.emit(ObjectRelationValueCommand.ShowAddFileScreen)
        }
    }

    fun onFileValueActionUploadFromGalleryClicked() {}

    fun onFileValueActionUploadFromStorageClicked() {}

    fun onObjectClicked(ctx: Id, id: Id, layout: ObjectType.Layout?) {
        if (id != ctx) {
            when (layout) {
                ObjectType.Layout.BASIC,
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.TODO,
                ObjectType.Layout.FILE,
                ObjectType.Layout.IMAGE,
                ObjectType.Layout.NOTE,
                ObjectType.Layout.BOOKMARK -> {
                    viewModelScope.launch {
                        navigation.emit(AppNavigation.Command.OpenObject(id))
                    }
                }
                ObjectType.Layout.SET -> {
                    viewModelScope.launch {
                        navigation.emit(AppNavigation.Command.OpenObjectSet(id))
                    }
                }
                else -> Timber.d("Unexpected layout: $layout").also {
                    sendToast(CANNOT_OPEN_OBJECT_WITH_THIS_LAYOUT)
                }
            }
        } else {
            sendToast(ALREADY_HERE_MSG)
        }
    }

    fun onNonExistentObjectClicked(ctx: Id, target: Id) {
        // TODO consider closing object before navigation
        viewModelScope.launch {
            navigation.emit(AppNavigation.Command.OpenObject(target))
        }
    }

    fun onFileClicked(id: Id) {
        viewModelScope.launch {
            navigation.emit(AppNavigation.Command.OpenObject(id))
        }
    }

    fun onAddFileToObject(
        ctx: Id,
        target: Id,
        relationKey: Key,
        filePath: String
    ) {
        viewModelScope.launch {
            isLoading.emit(true)
            val obj = values.get(target)
            addFileToObject(
                params = AddFileToObject.Params(
                    ctx = target,
                    relation = relationKey,
                    obj = obj,
                    path = filePath
                )
            ).process(
                failure = {
                    isLoading.emit(false)
                    Timber.e(it, "Error while adding new file to object")
                },
                success = {
                    isLoading.emit(false)
                    Timber.d("Successfully add new file to object")
                    dispatcher.send(it)
                }
            )
        }
    }

    //region COPY FILE TO CACHE
    val copyFileStatus = MutableSharedFlow<CopyFileStatus>(replay = 0)

    fun onStartCopyFileToCacheDir(uri: Uri) {
        copyFileToCache.execute(
            uri = uri,
            scope = viewModelScope,
            listener = copyFileListener
        )
    }

    fun onCancelCopyFileToCacheDir() {
        copyFileToCache.cancel()
    }

    private val copyFileListener = object : OnCopyFileToCacheAction {
        override fun onCopyFileStart() {
            viewModelScope.launch {
                copyFileStatus.emit(CopyFileStatus.Started)
            }
        }

        override fun onCopyFileResult(result: String?) {
            viewModelScope.launch {
                copyFileStatus.emit(CopyFileStatus.Completed(result))
            }
        }

        override fun onCopyFileError(msg: String) {
            viewModelScope.launch {
                copyFileStatus.emit(CopyFileStatus.Error(msg))
            }
        }
    }
    //endregion

    sealed class ObjectRelationValueCommand {
        object ShowAddStatusOrTagScreen : ObjectRelationValueCommand()
        object ShowAddObjectScreen : ObjectRelationValueCommand()
        object ShowFileValueActionScreen : ObjectRelationValueCommand()
        object ShowAddFileScreen : ObjectRelationValueCommand()
    }

    companion object {
        const val ALREADY_HERE_MSG = "Already here."
        const val CANNOT_OPEN_OBJECT_WITH_THIS_LAYOUT = "Cannot open object witht this layout."
    }
}

class RelationValueDVViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val details: ObjectDetailProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val setObjectDetails: UpdateDetail,
    private val urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    copyFileToCache: CopyFileToCacheDirectory,
    addFileToObject: AddFileToObject
) : RelationValueBaseViewModel(
    relations = relations,
    values = values,
    details = details,
    storeOfObjectTypes = storeOfObjectTypes,
    urlBuilder = urlBuilder,
    copyFileToCache = copyFileToCache,
    setObjectDetails = setObjectDetails,
    addFileToObject = addFileToObject,
    dispatcher = dispatcher,
    analytics = analytics
) {

    override suspend fun parseStatusRelationValues(
        ids: List<Id>,
        relationKey: Key
    ) = buildList {
        ids.forEach { id ->
            val option = values.get(id)
            if (option.isNotEmpty()) {
                val wrapper = ObjectWrapper.Option(option)
                add(
                    RelationValueView.Option.Status(
                        id = id,
                        name = wrapper.name.orEmpty(),
                        color = wrapper.color.ifEmpty { null },
                    )
                )
            } else {
                Timber.e("Status option for relation key [$relationKey] was not found")
            }
        }
    }

    override suspend fun parseTagRelationValues(
        ids: List<Id>,
        isRemovable: Boolean,
        relationKey: Key
    ) = buildList {
        ids.forEach { id ->
            val option = values.get(id)
            if (option.isNotEmpty()) {
                val wrapper = ObjectWrapper.Option(option)
                add(
                    RelationValueView.Option.Tag(
                        id = id,
                        name = wrapper.name.orEmpty(),
                        color = wrapper.color.ifEmpty { null },
                        removable = isRemovable,
                        isCheckboxShown = false,
                        isSelected = true,
                    )
                )
            } else {
                Timber.w("Tag option for relation key [$relationKey] was not found")
            }
        }
    }

    fun onAddObjectsOrFilesValueToRecord(
        record: Id,
        relationKey: Key,
        ids: List<Id>
    ) {
        viewModelScope.launch {
            val rec = values.get(record)
            val value = rec[relationKey].addIds(ids)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = record,
                    key = relationKey,
                    value = value
                )
            ).process(
                failure = { Timber.e(it, "Error while add objects or files value to record") },
                success = { Timber.d("Successfully add objects or files value to record") }
            )
        }
    }

    override suspend fun resolveWrapperForObject(id: Id): ObjectWrapper.Basic {
        // For sets, we need to take values from db / store, and not from details.
        return ObjectWrapper.Basic(values.get(target = id))
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val details: ObjectDetailProvider,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val setObjectDetails: UpdateDetail,
        private val urlBuilder: UrlBuilder,
        private val addFileToObject: AddFileToObject,
        private val copyFileToCache: CopyFileToCacheDirectory,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RelationValueDVViewModel(
            relations = relations,
            values = values,
            details = details,
            storeOfObjectTypes = storeOfObjectTypes,
            urlBuilder = urlBuilder,
            setObjectDetails = setObjectDetails,
            addFileToObject = addFileToObject,
            copyFileToCache = copyFileToCache,
            dispatcher = dispatcher,
            analytics = analytics
        ) as T
    }
}

class RelationValueViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val details: ObjectDetailProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val updateDetail: UpdateDetail,
    private val dispatcher: Dispatcher<Payload>,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    copyFileToCache: CopyFileToCacheDirectory,
    addFileToObject: AddFileToObject
) : RelationValueBaseViewModel(
    relations = relations,
    values = values,
    details = details,
    storeOfObjectTypes = storeOfObjectTypes,
    urlBuilder = urlBuilder,
    copyFileToCache = copyFileToCache,
    analytics = analytics,
    dispatcher = dispatcher,
    setObjectDetails = updateDetail,
    addFileToObject = addFileToObject
) {

    override suspend fun parseStatusRelationValues(
        ids: List<Id>,
        relationKey: Key
    ) = buildList {
        ids.forEach { id ->
            val option = details.provide()[id]
            if (option != null) {
                val wrapper = ObjectWrapper.Option(option.map)
                add(
                    RelationValueView.Option.Status(
                        id = id,
                        name = wrapper.name.orEmpty(),
                        color = wrapper.color.ifEmpty { null },
                    )
                )
            } else {
                Timber.e("Status option for relation key [$relationKey] was not found")
            }
        }
    }

    override suspend fun parseTagRelationValues(
        ids: List<Id>,
        isRemovable: Boolean,
        relationKey: Key
    ) = buildList {
        ids.forEach { id ->
            val option = details.provide()[id]
            if (option != null) {
                val wrapper = ObjectWrapper.Option(option.map)
                add(
                    RelationValueView.Option.Tag(
                        id = id,
                        name = wrapper.name.orEmpty(),
                        color = wrapper.color.ifEmpty { null },
                        removable = isRemovable,
                        isCheckboxShown = false,
                        isSelected = true,
                    )
                )
            } else {
                Timber.w("Tag option for relation key [$relationKey] was not found")
            }
        }
    }

    fun onAddObjectsOrFilesValueToObject(
        ctx: Id,
        target: Id,
        relationKey: Key,
        ids: List<Id>
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relationKey].addIds(ids)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relationKey,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while adding objects value to object") },
                success = { payload ->
                    dispatcher.send(payload)
                    sendAnalyticsRelationValueEvent(analytics)
                }
            )
        }
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val dispatcher: Dispatcher<Payload>,
        private val details: ObjectDetailProvider,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val updateDetail: UpdateDetail,
        private val urlBuilder: UrlBuilder,
        private val addFileToObject: AddFileToObject,
        private val copyFileToCache: CopyFileToCacheDirectory,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RelationValueViewModel(
            relations = relations,
            values = values,
            details = details,
            storeOfObjectTypes = storeOfObjectTypes,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            urlBuilder = urlBuilder,
            addFileToObject = addFileToObject,
            copyFileToCache = copyFileToCache,
            analytics = analytics
        ) as T
    }
}