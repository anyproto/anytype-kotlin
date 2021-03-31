package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectTypeProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectRelationValueViewModel.ObjectRelationValueView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class AddObjectRelationValueViewModel(
    protected val values: ObjectValueProvider,
    protected val details: ObjectDetailProvider,
    protected val relations: ObjectRelationProvider,
    protected val types: ObjectTypeProvider,
    protected val urlBuilder: UrlBuilder
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()
    val isAddButtonVisible = MutableStateFlow(true)
    val counter = MutableStateFlow(0)
    val isDimissed = MutableStateFlow(false)
    val views = MutableStateFlow(listOf<ObjectRelationValueView>())

    fun onStart(target: Id, relationId: Id) {
        val s1 = relations.subscribe(relationId)
        val s2 = values.subscribe(target)
        jobs += viewModelScope.launch {
            s1.combine(s2) { relation, record ->
                buildViews(relation, record, relationId).also {
                    if (relation.format == Relation.Format.STATUS) {
                        isAddButtonVisible.value = false
                    }
                }
            }.collect()
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    private fun buildViews(relation: Relation, record: Map<String, Any?>, relationId: Id) {
        val options = relation.selections

        val result = mutableListOf<ObjectRelationValueView>()

        val items = mutableListOf<ObjectRelationValueView>()

        when (relation.format) {
            Relation.Format.TAG -> {
                val related = record[relationId] as? List<*> ?: emptyList<String>()
                val keys = related.typeOf<Id>()
                options.forEach { option ->
                    if (!keys.contains(option.id)) {
                        items.add(
                            ObjectRelationValueView.Tag(
                                id = option.id,
                                name = option.text,
                                color = option.color.ifEmpty { null },
                                isSelected = false
                            )
                        )
                    }
                }
            }
            Relation.Format.STATUS -> {
                val related = record[relationId] as? List<*> ?: emptyList<String>()
                val keys = related.typeOf<Id>()
                options.forEach { option ->
                    if (!keys.contains(option.id)) {
                        items.add(
                            ObjectRelationValueView.Status(
                                id = option.id,
                                name = option.text,
                                color = option.color.ifEmpty { null },
                            )
                        )
                    }
                }
            }
            Relation.Format.OBJECT -> {
                val value = record.getOrDefault(relationId, null)
                if (value is List<*>) {
                    value.typeOf<Id>().forEach { id ->
                        val detail = details.provide()[id]
                        val objectType = types.provide().find { it.url == detail?.type }
                        items.add(
                            ObjectRelationValueView.Object(
                                id = id,
                                name = detail?.name.orEmpty(),
                                type = objectType?.name,
                                emoji = detail?.iconEmoji?.ifEmpty { null },
                                image = detail?.iconImage?.let {
                                    if (it.isEmpty()) null else urlBuilder.thumbnail(it)
                                },
                                removeable = false,
                                layout = objectType?.layout
                            )
                        )
                    }
                } else if (value is Id) {
                    val detail = details.provide()[value]
                    val objectType = types.provide().find { it.url == detail?.type }
                    items.add(
                        ObjectRelationValueView.Object(
                            id = value,
                            name = detail?.name.orEmpty(),
                            type = objectType?.name,
                            emoji = detail?.iconEmoji?.ifEmpty { null },
                            image = detail?.iconImage?.let {
                                if (it.isEmpty()) null else urlBuilder.thumbnail(it)
                            },
                            removeable = false,
                            layout = objectType?.layout
                        )
                    )
                }
            }
            Relation.Format.FILE -> {
                val value = record.getOrDefault(relationId, null)
                check(value is List<*>) { "Unexpected file data format" }
                value.typeOf<Id>().forEach { id ->
                    val detail = details.provide()[id]
                    items.add(
                        ObjectRelationValueView.File(
                            id = id,
                            name = detail?.name.orEmpty(),
                            mime = detail?.fileMimeType.orEmpty(),
                            ext = detail?.fileExt.orEmpty(),
                            image = detail?.iconImage
                        )
                    )
                }
            }
            else -> throw IllegalStateException("Unsupported format: ${relation.format}")
        }

        result.addAll(items)

        if (result.isEmpty()) {
            result.add(ObjectRelationValueView.Empty)
        }

        views.value = result
    }

    fun onFilterInputChanged(input: String) {
        views.value = views.value.filterNot { it is ObjectRelationValueView.Create }.toMutableList().apply {
            if (input.isNotEmpty()) add(0, ObjectRelationValueView.Create(input))
        }
    }

    fun onTagClicked(tag: ObjectRelationValueView.Tag) {
        views.value = views.value.map { view ->
            if (view is ObjectRelationValueView.Tag && view.id == tag.id) {
                view.copy(
                    isSelected = if (view.isSelected != null)
                        !view.isSelected
                    else
                        true
                )
            } else {
                view
            }
        }.also { result ->
            counter.value = result.count { it is ObjectRelationValueView.Selectable && it.isSelected == true }
        }
    }
}

class AddObjectSetObjectRelationValueViewModel(
    details: ObjectDetailProvider,
    types: ObjectTypeProvider,
    urlBuilder: UrlBuilder,
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    private val addDataViewRelationOption: AddDataViewRelationOption,
    private val addTagToDataViewRecord: AddTagToDataViewRecord,
    private val addStatusToDataViewRecord: AddStatusToDataViewRecord,
    private val dispatcher: Dispatcher<Payload>,
) : AddObjectRelationValueViewModel(
    details = details,
    values = values,
    types = types,
    urlBuilder = urlBuilder,
    relations = relations
) {

    fun onCreateDataViewRelationOptionClicked(
        ctx: Id,
        dataview: Id,
        viewer: Id,
        relation: Id,
        target: Id,
        name: String
    ) {
        viewModelScope.launch {
            addDataViewRelationOption(
                AddDataViewRelationOption.Params(
                    ctx = ctx,
                    relation = relation,
                    dataview = dataview,
                    record = target,
                    name = name,
                    color = ThemeColor.values().random().title
                )
            ).proceed(
                success = { (payload, option) ->
                    dispatcher.send(payload)
                    if (option != null) {
                        when (relations.get(relation).format) {
                            Relation.Format.TAG -> {
                                proceedWithAddingTagToDataViewRecord(
                                    ctx = ctx,
                                    dataview = dataview,
                                    viewer = viewer,
                                    relation = relation,
                                    target = target,
                                    tags = listOf(option)
                                )
                            }
                            Relation.Format.STATUS -> {
                                proceedWithAddingStatusToDataViewRecord(
                                    ctx = ctx,
                                    dataview = dataview,
                                    viewer = viewer,
                                    relation = relation,
                                    obj = target,
                                    status = option
                                )
                            }
                            else -> Timber.e("Trying to create option for wrong relation.")
                        }
                    }
                },
                failure = { Timber.e(it, "Error while creating a new option") }
            )
        }
    }

    fun onAddObjectSetStatusClicked(
        ctx: Id,
        dataview: Id,
        viewer: Id,
        relation: Id,
        obj: Id,
        status: ObjectRelationValueView.Status
    ) {
        proceedWithAddingStatusToDataViewRecord(ctx, dataview, viewer, relation, obj, status.id)
    }

    private fun proceedWithAddingStatusToDataViewRecord(
        ctx: Id,
        dataview: Id,
        viewer: Id,
        relation: Id,
        obj: Id,
        status: Id
    ) {
        viewModelScope.launch {
            addStatusToDataViewRecord(
                AddStatusToDataViewRecord.Params(
                    ctx = ctx,
                    dataview = dataview,
                    viewer = viewer,
                    relation = relation,
                    obj = obj,
                    status = status,
                    record = values.get(target = obj)
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = { isDimissed.value = true }
            )
        }
    }

    fun onAddSelectedValuesToDataViewClicked(
        ctx: Id,
        dataview: Id,
        target: Id,
        relation: Id,
        viewer: Id
    ) {
        val tags = views.value.mapNotNull { view ->
            if (view is ObjectRelationValueView.Tag && view.isSelected == true)
                view.id
            else
                null
        }
        proceedWithAddingTagToDataViewRecord(
            target = target,
            ctx = ctx,
            dataview = dataview,
            relation = relation,
            viewer = viewer,
            tags = tags
        )
    }

    private fun proceedWithAddingTagToDataViewRecord(
        target: Id,
        ctx: Id,
        dataview: Id,
        relation: Id,
        viewer: Id,
        tags: List<Id>
    ) {
        viewModelScope.launch {
            val record = values.get(target = target)
            addTagToDataViewRecord(
                AddTagToDataViewRecord.Params(
                    ctx = ctx,
                    tags = tags,
                    record = record,
                    dataview = dataview,
                    relation = relation,
                    viewer = viewer,
                    target = target
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = { isDimissed.value = true }
            )
        }
    }

    class Factory(
        private val values: ObjectValueProvider,
        private val details: ObjectDetailProvider,
        private val relations: ObjectRelationProvider,
        private val types: ObjectTypeProvider,
        private val addDataViewRelationOption: AddDataViewRelationOption,
        private val addTagToDataViewRecord: AddTagToDataViewRecord,
        private val addStatusToDataViewRecord: AddStatusToDataViewRecord,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddObjectSetObjectRelationValueViewModel(
                details = details,
                values = values,
                relations = relations,
                types = types,
                urlBuilder = urlBuilder,
                addDataViewRelationOption = addDataViewRelationOption,
                addTagToDataViewRecord = addTagToDataViewRecord,
                addStatusToDataViewRecord = addStatusToDataViewRecord,
                dispatcher = dispatcher
            ) as T
        }
    }
}

class AddObjectObjectRelationValueViewModel(
    details: ObjectDetailProvider,
    types: ObjectTypeProvider,
    urlBuilder: UrlBuilder,
    values: ObjectValueProvider,
    relations: ObjectRelationProvider,
    private val addObjectRelationOption: AddObjectRelationOption,
    private val updateDetail: UpdateDetail,
    private val dispatcher: Dispatcher<Payload>,
) : AddObjectRelationValueViewModel(
    details = details,
    values = values,
    types = types,
    urlBuilder = urlBuilder,
    relations = relations
) {

    fun onAddObjectStatusClicked(
        ctx: Id,
        relation: Id,
        status: ObjectRelationValueView.Status
    ) = proceedWithAddingStatusToObject(
        ctx = ctx,
        relation = relation,
        status = status.id
    )

    private fun proceedWithAddingStatusToObject(
        ctx: Id,
        relation: Id,
        status: Id
    ) {
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = listOf(status)
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = { dispatcher.send(it).also { isDimissed.value = true } }
            )
        }
    }

    fun onAddSelectedValuesToObjectClicked(
        ctx: Id,
        obj: Id,
        relation: Id
    ) {
        val tags = views.value.mapNotNull { view ->
            if (view is ObjectRelationValueView.Tag && view.isSelected == true)
                view.id
            else
                null
        }
        proceedWithAddingTagToObject(
            obj = obj,
            ctx = ctx,
            relation = relation,
            tags = tags
        )
    }

    private fun proceedWithAddingTagToObject(
        obj: Id,
        ctx: Id,
        relation: Id,
        tags: List<Id>
    ) {
        viewModelScope.launch {
            val obj = values.get(target = obj)
            val result = mutableListOf<Id>()
            val value = obj[relation]
            if (value is List<*>) {
                result.addAll(value.typeOf())
            } else if (value is Id) {
                result.add(value)
            }
            result.addAll(tags)
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relation,
                    value = result
                )
            ).process(
                failure = { Timber.e(it, "Error while adding tag") },
                success = { dispatcher.send(it).also { isDimissed.value = true } }
            )
        }
    }

    fun onCreateObjectRelationOptionClicked(
        ctx: Id,
        relation: Id,
        name: String,
        obj: Id
    ) {
        viewModelScope.launch {
            addObjectRelationOption(
                AddObjectRelationOption.Params(
                    ctx = ctx,
                    relation = relation,
                    name = name,
                    color = ThemeColor.values().random().title
                )
            ).proceed(
                success = { (payload, option) ->
                    dispatcher.send(payload)
                    if (option != null) {
                        when (val format = relations.get(relation).format) {
                            Relation.Format.TAG -> {
                                proceedWithAddingTagToObject(
                                    ctx = ctx,
                                    relation = relation,
                                    obj = obj,
                                    tags = listOf(option)
                                )
                            }
                            Relation.Format.STATUS -> {
                                proceedWithAddingStatusToObject(
                                    ctx = ctx,
                                    relation = relation,
                                    status = option
                                )
                            }
                            else -> {
                                Timber.e("Trying to create an option for relation format: $format")
                            }
                        }
                    }
                },
                failure = { Timber.e(it, "Error while creating a new option for object") }
            )
        }
    }

    class Factory(
        private val values: ObjectValueProvider,
        private val details: ObjectDetailProvider,
        private val relations: ObjectRelationProvider,
        private val types: ObjectTypeProvider,
        private val addObjectRelationOption: AddObjectRelationOption,
        private val updateDetail: UpdateDetail,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddObjectObjectRelationValueViewModel(
                details = details,
                values = values,
                relations = relations,
                types = types,
                urlBuilder = urlBuilder,
                addObjectRelationOption = addObjectRelationOption,
                updateDetail = updateDetail,
                dispatcher = dispatcher
            ) as T
        }
    }
}