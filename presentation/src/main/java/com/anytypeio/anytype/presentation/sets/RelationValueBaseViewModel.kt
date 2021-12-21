package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.addIds
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.RemoveStatusFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.domain.relations.AddFileToRecord
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class RelationValueBaseViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val details: ObjectDetailProvider,
    private val types: ObjectTypesProvider,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    val navigation = MutableSharedFlow<AppNavigation.Command>()

    private val jobs = mutableListOf<Job>()
    private var relationFormat: Relation.Format? = null

    val isEditing = MutableStateFlow(false)
    val isDimissed = MutableStateFlow(false)
    val name = MutableStateFlow("")
    val views = MutableStateFlow(listOf<RelationValueView>())
    val commands = MutableSharedFlow<ObjectRelationValueCommand>(replay = 0)
    val isLoading = MutableStateFlow<Boolean>(false)

    fun onStart(objectId: Id, relationId: Id) {
        Timber.d("onStart")
        jobs += viewModelScope.launch {
            val s1 = relations.subscribe(relationId)
            val s2 = values.subscribe(objectId)
            s1.combine(s2) { relation, record ->
                initDataViewUIState(relation, record, relationId)
            }.collect()
        }
    }

    fun onStop() {
        Timber.d("onStop")
        jobs.cancel()
    }

    private fun initDataViewUIState(relation: Relation, record: Map<String, Any?>, relationId: Id) {
        val options = relation.selections

        val result = mutableListOf<RelationValueView>()

        val items = mutableListOf<RelationValueView>()

        when (relation.format) {
            Relation.Format.TAG -> {
                relationFormat = Relation.Format.TAG
                val isRemoveable = isEditing.value
                val optionKeys : List<Id> = when(val value = record[relationId]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                optionKeys.forEach { key ->
                    val option = options.find { it.id == key }
                    if (option != null) {
                        items.add(
                            RelationValueView.Tag(
                                id = option.id,
                                name = option.text,
                                color = option.color.ifEmpty { null },
                                removeable = isRemoveable
                            )
                        )
                    } else {
                        Timber.e("Tag option for relation key [$key] was not found")
                    }
                }
            }
            Relation.Format.STATUS -> {
                relationFormat = Relation.Format.STATUS
                val optionKeys : List<Id> = when(val value = record[relationId]) {
                    is Id -> listOf(value)
                    is List<*> -> value.typeOf()
                    else -> emptyList()
                }
                optionKeys.forEach { key ->
                    val option = options.find { it.id == key }
                    if (option != null) {
                        items.add(
                            RelationValueView.Status(
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
                val isRemoveable = isEditing.value
                relationFormat = Relation.Format.OBJECT
                val value = record.getOrDefault(relationId, null)
                if (value is List<*>) {
                    value.typeOf<Id>().forEach { id ->
                        val detail = details.provide()[id]
                        val wrapper = ObjectWrapper.Basic(detail?.map ?: emptyMap())
                        val type = wrapper.type.firstOrNull()
                        val objectType = types.get().find { it.url == type }
                        if (wrapper.isDeleted == true) {
                            items.add(
                                RelationValueView.Object.NonExistent(
                                    id = id,
                                    removeable = isRemoveable
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
                                    removeable = isRemoveable,
                                    layout = wrapper.layout
                                )
                            )
                        }
                    }
                } else if (value is Id) {
                    val detail = details.provide()[value]
                    val wrapper = ObjectWrapper.Basic(detail?.map ?: emptyMap())
                    val type = wrapper.type.firstOrNull()
                    val objectType = types.get().find { it.url == type }
                    if (wrapper.isDeleted == true) {
                        items.add(
                            RelationValueView.Object.NonExistent(
                                id = value,
                                removeable = isRemoveable
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
                                removeable = isRemoveable,
                                layout = wrapper.layout
                            )
                        )
                    }
                }
            }
            Relation.Format.FILE -> {
                relationFormat = Relation.Format.FILE
                val isRemoveable = isEditing.value
                val value = record.getOrDefault(relationId, null)
                if (value != null) {
                    check(value is List<*>) { "Unexpected file data format" }
                    value.typeOf<Id>().forEach { id ->
                        val detail = details.provide()[id]
                        items.add(
                            RelationValueView.File(
                                id = id,
                                name = detail?.name.orEmpty(),
                                mime = detail?.fileMimeType.orEmpty(),
                                ext = detail?.fileExt.orEmpty(),
                                image = detail?.iconImage,
                                removeable = isRemoveable
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

    fun onEditOrDoneClicked() {
        isEditing.value = !isEditing.value
        views.value = views.value.map { v ->
            when (v) {
                is RelationValueView.Object.Default -> v.copy(removeable = isEditing.value)
                is RelationValueView.Object.NonExistent -> v.copy(removeable = isEditing.value)
                is RelationValueView.Tag -> v.copy(removeable = isEditing.value)
                is RelationValueView.Status -> v.copy(removeable = isEditing.value)
                is RelationValueView.File -> v.copy(removeable = isEditing.value)
                else -> v
            }
        }
    }

    fun onAddValueClicked() {
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
                ObjectType.Layout.NOTE -> {
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

    sealed class ObjectRelationValueCommand {
        object ShowAddStatusOrTagScreen : ObjectRelationValueCommand()
        object ShowAddObjectScreen : ObjectRelationValueCommand()
        object ShowFileValueActionScreen : ObjectRelationValueCommand()
        object ShowAddFileScreen : ObjectRelationValueCommand()
    }

    sealed class RelationValueView {

        interface Selectable {
            val isSelected: Boolean?
        }

        object Empty : RelationValueView()

        data class Create(val name: String) : RelationValueView()

        data class Tag(
            val id: Id,
            val name: String,
            val color: String? = null,
            val removeable: Boolean = false,
            override val isSelected: Boolean? = null
        ) : RelationValueView(), Selectable

        data class Status(
            val id: Id,
            val name: String,
            val removeable: Boolean = false,
            val color: String? = null
        ) : RelationValueView()

        sealed class Object : RelationValueView(), Selectable {

            abstract val id: Id

            data class Default(
                override val id: Id,
                val name: String,
                val typeName: String?,
                val type: String?,
                val removeable: Boolean,
                val icon: ObjectIcon,
                val layout: ObjectType.Layout?,
                override val isSelected: Boolean? = null,
                val selectedNumber: String? = null
            ) : Object(), Selectable

            data class NonExistent(
                override val id: Id,
                override val isSelected: Boolean? = null,
                val removeable: Boolean
            ) : Object(), Selectable
        }

        data class File(
            val id: Id,
            val name: String,
            val mime: String,
            val ext: String,
            val removeable: Boolean = false,
            val image: Url?,
            override val isSelected: Boolean? = null,
            val selectedNumber: String? = null
        ) : RelationValueView(), Selectable
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
    private val removeTagFromDataViewRecord: RemoveTagFromDataViewRecord,
    private val removeStatusFromDataViewRecord: RemoveStatusFromDataViewRecord,
    private val updateDataViewRecord: UpdateDataViewRecord,
    private val dispatcher: Dispatcher<Payload>,
    private val urlBuilder: UrlBuilder,
    private val addFileToRecord: AddFileToRecord
) : RelationValueBaseViewModel(
    relations = relations,
    values = values,
    details = details,
    types = types,
    urlBuilder = urlBuilder
) {

    fun onRemoveTagFromDataViewRecordClicked(
        ctx: Id,
        dataview: Id,
        target: Id,
        relation: Id,
        viewer: Id,
        tag: Id
    ) {
        viewModelScope.launch {
            val record = values.get(target)
            removeTagFromDataViewRecord(
                RemoveTagFromDataViewRecord.Params(
                    ctx = ctx,
                    tag = tag,
                    record = record,
                    dataview = dataview,
                    relation = relation,
                    viewer = viewer,
                    target = target
                )
            ).process(
                failure = { Timber.e(it, "Error while removing tag") },
                success = { Timber.d("Successfully removed tag") }
            )
        }
    }

    fun onRemoveStatusFromDataViewRecordClicked(
        ctx: Id,
        dataview: Id,
        target: Id,
        relation: Id,
        viewer: Id,
        status: Id
    ) {
        viewModelScope.launch {
            val record = values.get(target)
            removeStatusFromDataViewRecord(
                RemoveStatusFromDataViewRecord.Params(
                    ctx = ctx,
                    status = status,
                    record = record,
                    dataview = dataview,
                    relation = relation,
                    viewer = viewer,
                    target = target
                )
            ).process(
                failure = { Timber.e(it, "Error while removing status") },
                success = { Timber.d("Successfully removed status") }
            )
        }
    }

    fun onAddFileToRecord(
        ctx: Id,
        dataview: Id,
        record: Id,
        relation: Id,
        filePath: String
    ) {
        viewModelScope.launch {
            isLoading.emit(true)
            val value = values.subscribe(record).first()
            addFileToRecord(
                params = AddFileToRecord.Params(
                    context = ctx,
                    target = dataview,
                    record = record,
                    relation = relation,
                    value = value,
                    path = filePath
                )
            ).process(
                failure = {
                    isLoading.emit(false)
                    Timber.e(it, "Error while adding new file to record")
                },
                success = {
                    isLoading.emit(false)
                    Timber.d("Successfully add new file to record")
                }
            )
        }
    }

    fun onAddObjectsOrFilesValueToRecord(
        ctx: Id,
        dataview: Id,
        record: Id,
        relation: Id,
        ids: List<Id>
    ) {
        viewModelScope.launch {
            val rec = values.get(record)
            val value = rec[relation].addIds(ids)
            val updated = mapOf(relation to value)
            updateDataViewRecord(
                UpdateDataViewRecord.Params(
                    context = ctx,
                    target = dataview,
                    record = record,
                    values = updated
                )
            ).process(
                failure = { Timber.e(it, "Error while add objects or files value to record") },
                success = { Timber.d("Successfully add objects or files value to record") }
            )
        }
    }

    fun onRemoveObjectFromDataViewRecordClicked(
        ctx: Id,
        dataview: Id,
        target: Id,
        relation: Id,
        objectId: Id
    ) {
        viewModelScope.launch {
            val rec = values.get(target)
            val value = rec[relation].filterIdsById(objectId)
            val updated = mapOf(relation to value)
            updateDataViewRecord(
                UpdateDataViewRecord.Params(
                    context = ctx,
                    target = dataview,
                    record = target,
                    values = updated
                )
            ).process(
                failure = { Timber.e(it, "Error while removing object") },
                success = { Timber.d("Successfully removed object") }
            )
        }
    }

    fun onRemoveFileFromDataViewRecordClicked(
        ctx: Id,
        dataview: Id,
        target: Id,
        relation: Id,
        fileId: Id
    ) {
        viewModelScope.launch {
            val rec = values.get(target)
            val value = rec[relation].filterIdsById(fileId)
            val updated = mapOf(relation to value)
            updateDataViewRecord(
                UpdateDataViewRecord.Params(
                    context = ctx,
                    target = dataview,
                    record = target,
                    values = updated
                )
            ).process(
                failure = { Timber.e(it, "Error while removing file") },
                success = { Timber.d("Successfully removed file") }
            )
        }
    }

    fun onDataViewValueOrderChanged(
        ctx: Id,
        dv: Id,
        obj: Id,
        relation: Id,
        order: List<Id>
    ) {
        viewModelScope.launch {
            val updated = mapOf(relation to order)
            updateDataViewRecord(
                UpdateDataViewRecord.Params(
                    context = ctx,
                    target = dv,
                    record = obj,
                    values = updated
                )
            ).process(
                failure = { Timber.e(it, "Error while updating DV record order") },
                success = { Timber.d("DV record order updated") }
            )
        }
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val dispatcher: Dispatcher<Payload>,
        private val details: ObjectDetailProvider,
        private val types: ObjectTypesProvider,
        private val updateDataViewRecord: UpdateDataViewRecord,
        private val removeTagFromRecord: RemoveTagFromDataViewRecord,
        private val removeStatusFromDataViewRecord: RemoveStatusFromDataViewRecord,
        private val urlBuilder: UrlBuilder,
        private val addFileToRecord: AddFileToRecord
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = RelationValueDVViewModel(
            relations = relations,
            values = values,
            details = details,
            types = types,
            dispatcher = dispatcher,
            removeTagFromDataViewRecord = removeTagFromRecord,
            removeStatusFromDataViewRecord = removeStatusFromDataViewRecord,
            urlBuilder = urlBuilder,
            updateDataViewRecord = updateDataViewRecord,
            addFileToRecord = addFileToRecord
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
    private val addFileToObject: AddFileToObject
) : RelationValueBaseViewModel(
    relations = relations,
    values = values,
    details = details,
    types = types,
    urlBuilder = urlBuilder
) {

    fun onObjectValueOrderChanged(
        ctx: Id,
        relation: Id,
        order: List<Id>
    ) {
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = order
                )
            ).process(
                failure = { Timber.e(it, "Error while updating object value order") },
                success = { dispatcher.send(it) }
            )
        }
    }

    fun onRemoveObjectFromObjectClicked(
        ctx: Id,
        target: Id,
        relation: Id,
        objectId: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(objectId)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing object from object") },
                success = { dispatcher.send(it) }
            )
        }
    }

    fun onRemoveFileFromObjectClicked(
        ctx: Id,
        target: Id,
        relation: Id,
        fileId: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(fileId)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing file from object") },
                success = { dispatcher.send(it) }
            )
        }
    }

    fun onRemoveTagFromObjectClicked(
        ctx: Id,
        target: Id,
        relation: Id,
        tag: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(tag)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing tag from object") },
                success = { dispatcher.send(it) }
            )
        }
    }

    fun onRemoveStatusFromObjectClicked(
        ctx: Id,
        target: Id,
        relation: Id,
        status: Id
    ) {
        viewModelScope.launch {
            val obj = values.get(target)
            val remaining = obj[relation].filterIdsById(status)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = remaining
                )
            ).process(
                failure = { Timber.e(it, "Error while removing tag from object") },
                success = { dispatcher.send(it) }
            )
        }
    }

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
                success = { dispatcher.send(it) }
            )
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
                    ctx = ctx,
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

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val dispatcher: Dispatcher<Payload>,
        private val details: ObjectDetailProvider,
        private val types: ObjectTypesProvider,
        private val updateDetail: UpdateDetail,
        private val urlBuilder: UrlBuilder,
        private val addFileToObject: AddFileToObject
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = RelationValueViewModel(
            relations = relations,
            values = values,
            details = details,
            types = types,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            urlBuilder = urlBuilder,
            addFileToObject = addFileToObject
        ) as T
    }
}