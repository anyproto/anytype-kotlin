package com.anytypeio.anytype.presentation.sets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ext.addIds
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.misc.UrlBuilder
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
    private val types: ObjectTypesProvider,
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

    fun onStart(objectId: Id, relationId: Id) {
        Timber.d("onStart")
        jobs += viewModelScope.launch {
            combine(
                relations.observe(relationId),
                values.subscribe(objectId)
            ) { relation, record -> initDataViewUIState(relation, record, relationId) }.collect()
        }
    }

    fun onStop() {
        Timber.d("onStop")
        jobs.cancel()
    }

    private suspend fun initDataViewUIState(relation: Relation, record: Map<String, Any?>, relationId: Id) {
        val options = relation.selections

        val result = mutableListOf<RelationValueView>()

        val items = mutableListOf<RelationValueView>()

        when (relation.format) {
            Relation.Format.TAG -> {
                relationFormat = Relation.Format.TAG
                val isRemovable = isEditing.value
                val optionKeys: List<Id> = when (val value = record[relationId]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                optionKeys.forEach { key ->
                    val option = options.find { it.id == key }
                    if (option != null) {
                        items.add(
                            RelationValueView.Option.Tag(
                                id = option.id,
                                name = option.text,
                                color = option.color.ifEmpty { null },
                                removable = isRemovable,
                                isCheckboxShown = false,
                                isSelected = true,
                            )
                        )
                    } else {
                        Timber.e("Tag option for relation key [$key] was not found")
                    }
                }
            }
            Relation.Format.STATUS -> {
                relationFormat = Relation.Format.STATUS
                val optionKeys: List<Id> = when (val value = record[relationId]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                optionKeys.forEach { key ->
                    val option = options.find { it.id == key }
                    if (option != null) {
                        items.add(
                            RelationValueView.Option.Status(
                                id = option.id,
                                name = option.text,
                                color = option.color.ifEmpty { null },
                            )
                        )
                    } else {
                        Timber.e("Status option for relation key [$key] was not found")
                    }
                }
            }
            Relation.Format.OBJECT -> {
                val isRemovable = isEditing.value
                relationFormat = Relation.Format.OBJECT
                val value = record.getOrDefault(relationId, null)
                if (value is List<*>) {
                    value.typeOf<Id>().forEach { id ->
                        val wrapper = resolveWrapperForObject(id)
                        val type = wrapper.type.firstOrNull()
                        val objectType = types.get().find { it.url == type }
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
                                    type = objectType?.url,
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
                    val objectType = types.get().find { it.url == type }
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
                                type = objectType?.url,
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
            Relation.Format.FILE -> {
                relationFormat = Relation.Format.FILE
                val isRemovable = isEditing.value
                val value = record.getOrDefault(relationId, null)
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
        name.value = relation.name
    }

    open suspend fun resolveWrapperForObject(id: Id): ObjectWrapper.Basic {
        val detail = details.provide()[id]
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
        relation: Id,
        tag: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(tag)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relation,
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
        relation: Id,
        status: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(status)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relation,
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
        relation: Id,
        objectId: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(objectId)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relation,
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
        relation: Id,
        fileId: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(fileId)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relation,
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
        relation: Id,
        order: List<Id>
    ) {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = target,
                    key = relation,
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
                else -> Timber.d("Unexpected layout: $layout")
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
        relation: Id,
        filePath: String
    ) {
        viewModelScope.launch {
            isLoading.emit(true)
            val obj = values.get(target)
            addFileToObject(
                params = AddFileToObject.Params(
                    ctx = target,
                    relation = relation,
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
    }
}

class RelationValueDVViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val details: ObjectDetailProvider,
    private val types: ObjectTypesProvider,
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
    types = types,
    urlBuilder = urlBuilder,
    copyFileToCache = copyFileToCache,
    setObjectDetails = setObjectDetails,
    addFileToObject = addFileToObject,
    dispatcher = dispatcher,
    analytics = analytics
) {

    fun onAddObjectsOrFilesValueToRecord(
        record: Id,
        relation: Id,
        ids: List<Id>
    ) {
        viewModelScope.launch {
            val rec = values.get(record)
            val value = rec[relation].addIds(ids)
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = record,
                    key = relation,
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
        private val types: ObjectTypesProvider,
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
            types = types,
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
    private val types: ObjectTypesProvider,
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
    types = types,
    urlBuilder = urlBuilder,
    copyFileToCache = copyFileToCache,
    analytics = analytics,
    dispatcher = dispatcher,
    setObjectDetails = updateDetail,
    addFileToObject = addFileToObject
) {

    fun onAddObjectsOrFilesValueToObject(
        ctx: Id,
        target: Id,
        relation: Id,
        ids: List<Id>
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].addIds(ids)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while adding objects value to object") },
                success = {
                    dispatcher.send(it)
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
        private val types: ObjectTypesProvider,
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
            types = types,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            urlBuilder = urlBuilder,
            addFileToObject = addFileToObject,
            copyFileToCache = copyFileToCache,
            analytics = analytics
        ) as T
    }
}