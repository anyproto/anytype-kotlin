package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView
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
    protected val types: ObjectTypesProvider,
    protected val urlBuilder: UrlBuilder
) : BaseViewModel() {

    private val jobs = mutableListOf<Job>()

    private val query = MutableStateFlow("")

    protected val views = MutableStateFlow(listOf<RelationValueView>())

    val ui = MutableStateFlow(listOf<RelationValueView>())
    val isAddButtonVisible = MutableStateFlow(true)
    val counter = MutableStateFlow(0)
    val isDismissed = MutableStateFlow(false)
    val isParentDismissed = MutableStateFlow(false)

    val isMultiple = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            views.combine(query) { all, query ->
                if (query.isEmpty())
                    all
                else
                    mutableListOf<RelationValueView>().apply {
                        add(RelationValueView.Create(query))
                        addAll(
                            all.filter { view ->
                                when(view) {
                                    is RelationValueView.Status -> {
                                        view.name.contains(query, true)
                                    }
                                    is RelationValueView.Tag -> {
                                        view.name.contains(query, true)
                                    }
                                    else -> true
                                }
                            }
                        )
                    }
            }.collect { ui.value = it }
        }
    }

    fun onStart(target: Id, relationId: Id) {
        val s1 = relations.subscribe(relationId)
        val s2 = values.subscribe(target)
        jobs += viewModelScope.launch {
            s1.combine(s2) { relation, record ->
                buildViews(relation, record, relationId).also {
                    if (relation.format == Relation.Format.STATUS) {
                        isAddButtonVisible.value = false
                        isMultiple.value = false
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

        val result = mutableListOf<RelationValueView>()

        val items = mutableListOf<RelationValueView>()

        when (relation.format) {
            Relation.Format.TAG -> {
                val related = record[relationId] as? List<*> ?: emptyList<String>()
                val keys = related.typeOf<Id>()
                options.forEach { option ->
                    if (!keys.contains(option.id)) {
                        items.add(
                            RelationValueView.Tag(
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
                            RelationValueView.Status(
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
                        val wrapper = ObjectWrapper.Basic(detail?.map ?: emptyMap())
                        val type = wrapper.type.firstOrNull()
                        val objectType = types.get().find { it.url == type }
                        items.add(
                            RelationValueView.Object(
                                id = id,
                                name = wrapper.name.orEmpty(),
                                typeName = objectType?.name,
                                type = type,
                                icon = ObjectIcon.from(
                                    obj = wrapper,
                                    layout = wrapper.layout,
                                    builder = urlBuilder
                                ),
                                removeable = false,
                                layout = wrapper.layout
                            )
                        )
                    }
                } else if (value is Id) {
                    val detail = details.provide()[value]
                    val wrapper = ObjectWrapper.Basic(detail?.map ?: emptyMap())
                    val type = wrapper.type.firstOrNull()
                    val objectType = types.get().find { it.url == type }
                    items.add(
                        RelationValueView.Object(
                            id = value,
                            name = wrapper.name.orEmpty(),
                            typeName = objectType?.name,
                            type = type,
                            icon = ObjectIcon.from(
                                obj = wrapper,
                                layout = wrapper.layout,
                                builder = urlBuilder
                            ),
                            removeable = false,
                            layout = wrapper.layout
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
                        RelationValueView.File(
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
            result.add(RelationValueView.Empty)
        }

        views.value = result
    }

    fun onFilterInputChanged(input: String) { query.value = input }

    fun onTagClicked(tag: RelationValueView.Tag) {
        views.value = views.value.map { view ->
            if (view is RelationValueView.Tag && view.id == tag.id) {
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
            counter.value = result.count { it is RelationValueView.Selectable && it.isSelected == true }
        }
    }
}

class RelationOptionValueDVAddViewModel(
    details: ObjectDetailProvider,
    types: ObjectTypesProvider,
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
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().title
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
        status: RelationValueView.Status
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
                success = { isParentDismissed.value = true }
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
            if (view is RelationValueView.Tag && view.isSelected == true)
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
                success = { isDismissed.value = true }
            )
        }
    }

    class Factory(
        private val values: ObjectValueProvider,
        private val details: ObjectDetailProvider,
        private val relations: ObjectRelationProvider,
        private val types: ObjectTypesProvider,
        private val addDataViewRelationOption: AddDataViewRelationOption,
        private val addTagToDataViewRecord: AddTagToDataViewRecord,
        private val addStatusToDataViewRecord: AddStatusToDataViewRecord,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationOptionValueDVAddViewModel(
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

class RelationOptionValueAddViewModel(
    details: ObjectDetailProvider,
    types: ObjectTypesProvider,
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
        status: RelationValueView.Status
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
                success = { dispatcher.send(it).also { isParentDismissed.value = true } }
            )
        }
    }

    fun onAddSelectedValuesToObjectClicked(
        ctx: Id,
        obj: Id,
        relation: Id
    ) {
        val tags = views.value.mapNotNull { view ->
            if (view is RelationValueView.Tag && view.isSelected == true)
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
                success = { dispatcher.send(it).also { isDismissed.value = true } }
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
                    color = ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random().title
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
        private val types: ObjectTypesProvider,
        private val addObjectRelationOption: AddObjectRelationOption,
        private val updateDetail: UpdateDetail,
        private val urlBuilder: UrlBuilder,
        private val dispatcher: Dispatcher<Payload>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationOptionValueAddViewModel(
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