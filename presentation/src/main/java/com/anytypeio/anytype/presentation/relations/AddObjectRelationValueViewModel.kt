package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


abstract class AddObjectRelationValueViewModel(
    protected val values: ObjectValueProvider,
    protected val details: ObjectDetailProvider,
    protected val relations: ObjectRelationProvider,
    protected val types: ObjectTypesProvider,
    protected val urlBuilder: UrlBuilder,
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
                println("views=$all, query=$query")
                filterRelationsBy(query, all)
            }.collect { ui.value = it }
        }
    }

    private fun filterRelationsBy(
        query: String,
        all: List<RelationValueView>
    ): List<RelationValueView> {
        return if (query.isEmpty())
            all
        else {
            val result = mutableListOf<RelationValueView>()
            val filteredRelationViews = all.filter { view ->
                when (view) {
                    is RelationValueView.Status -> {
                        view.name.contains(query, true)
                    }
                    is RelationValueView.Tag -> {
                        view.name.contains(query, true)
                    }
                    else -> true
                }
            }
            val searchedExistentTag =
                filteredRelationViews.filterIsInstance<RelationValueView.Tag>()
                    .any { view -> view.name == query }

            if (!searchedExistentTag) {
                result.add(RelationValueView.Create(query))
            }
            result.addAll(filteredRelationViews)
            result
        }
    }

    fun onStart(target: Id, relationId: Id) {
        val s1 = relations.subscribe(relationId)
        val s2 = values.subscribe(target)
        jobs += viewModelScope.launch {
            s1.combine(s2) { relation, record ->
                println("relation=$relation, record=$record")
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
        println("views.value set $result")
    }

    fun onFilterInputChanged(input: String) {
        query.value = input
    }

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
            counter.value =
                result.count { it is RelationValueView.Selectable && it.isSelected == true }
        }
    }
}

