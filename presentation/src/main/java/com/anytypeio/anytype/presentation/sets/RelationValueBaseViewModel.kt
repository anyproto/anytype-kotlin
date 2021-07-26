package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.addIds
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.RemoveStatusFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.domain.relations.AddFileToRecord
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.relations.getObjectTypeById
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectTypeProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.type
import com.anytypeio.anytype.presentation.util.Dispatcher
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
    private val types: ObjectTypeProvider,
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
        jobs += viewModelScope.launch {
            val s1 = relations.subscribe(relationId)
            val s2 = values.subscribe(objectId)
            s1.combine(s2) { relation, record ->
                initDataViewUIState(relation, record, relationId)
            }.collect()
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    private fun initDataViewUIState(relation: Relation, record: Map<String, Any?>, relationId: Id) {
        val options = relation.selections

        val result = mutableListOf<RelationValueView>()

        val items = mutableListOf<RelationValueView>()

        val optionKeys = record[relationId] as? List<*> ?: emptyList<String>()

        when (relation.format) {
            Relation.Format.TAG -> {
                relationFormat = Relation.Format.TAG
                val isRemoveable = isEditing.value
                optionKeys.forEach { key ->
                    val option = options.first { it.id == key }
                    items.add(
                        RelationValueView.Tag(
                            id = option.id,
                            name = option.text,
                            color = option.color.ifEmpty { null },
                            removeable = isRemoveable
                        )
                    )
                }
            }
            Relation.Format.STATUS -> {
                relationFormat = Relation.Format.STATUS
                optionKeys.forEach { key ->
                    val option = options.first { it.id == key }
                    items.add(
                        RelationValueView.Status(
                            id = option.id,
                            name = option.text,
                            color = option.color.ifEmpty { null },
                        )
                    )
                }
            }
            Relation.Format.OBJECT -> {
                val isRemoveable = isEditing.value
                relationFormat = Relation.Format.OBJECT
                val value = record.getOrDefault(relationId, null)
                if (value is List<*>) {
                    value.typeOf<Id>().forEach { id ->
                        val detail = details.provide()[id]
                        val type = detail?.map?.type
                        val objectType = types.provide().find { it.url == type }
                        items.add(
                            RelationValueView.Object(
                                id = id,
                                name = detail?.name.orEmpty(),
                                typeName = objectType?.name,
                                type = objectType?.url,
                                emoji = detail?.iconEmoji?.ifEmpty { null },
                                image = detail?.iconImage?.let {
                                    if (it.isEmpty()) null else urlBuilder.thumbnail(it)
                                },
                                removeable = isRemoveable,
                                layout = objectType?.layout
                            )
                        )
                    }
                } else if (value is Id) {
                    val detail = details.provide()[value]
                    val objectType = types.provide().getObjectTypeById(detail?.type)
                    items.add(
                        RelationValueView.Object(
                            id = value,
                            name = detail?.name.orEmpty(),
                            typeName = objectType?.name,
                            type = objectType?.url,
                            emoji = detail?.iconEmoji?.ifEmpty { null },
                            image = detail?.iconImage?.let {
                                if (it.isEmpty()) null else urlBuilder.thumbnail(it)
                            },
                            removeable = isRemoveable,
                            layout = objectType?.layout
                        )
                    )
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
                is RelationValueView.Object -> v.copy(removeable = isEditing.value)
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

    fun onObjectClicked(ctx: Id, id: Id, type: String?) {
        if (id != ctx) {
            types.provide().find { it.url == type }?.let { targetType ->
                when (targetType.layout) {
                    ObjectType.Layout.BASIC, ObjectType.Layout.PROFILE -> {
                        viewModelScope.launch {
                            navigation.emit(AppNavigation.Command.OpenObject(id))
                        }
                    }
                    ObjectType.Layout.SET -> {
                        viewModelScope.launch {
                            navigation.emit(AppNavigation.Command.OpenObjectSet(id))
                        }
                    }
                    else -> Timber.d("Unexpected layout: ${targetType.layout}")
                }
            }
        } else {
            sendToast(ALREADY_HERE_MSG)
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

        data class Object(
            val id: Id,
            val name: String,
            val typeName: String?,
            val type: String?,
            val removeable: Boolean,
            val emoji: String?,
            val image: Url?,
            val layout: ObjectType.Layout?,
            override val isSelected: Boolean? = null,
            val selectedNumber: String? = null
        ) : RelationValueView(), Selectable

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
    private val types: ObjectTypeProvider,
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
            val value = values.get(record)
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
            val updated = values.get(record).toMutableMap()
            updated[relation] = updated[relation].addIds(ids)
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
            val updated = values.get(target).toMutableMap()
            updated[relation] = updated[relation].filterIdsById(objectId)
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
            val updated = values.get(target).toMutableMap()
            updated[relation] = updated[relation].filterIdsById(fileId)
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
            val rec = values.get(obj).toMutableMap().apply {
                set(relation, order)
            }
            updateDataViewRecord(
                UpdateDataViewRecord.Params(
                    context = ctx,
                    target = dv,
                    record = obj,
                    values = rec
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
        private val types: ObjectTypeProvider,
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
    private val types: ObjectTypeProvider,
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
        private val types: ObjectTypeProvider,
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