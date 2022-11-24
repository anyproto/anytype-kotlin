package com.anytypeio.anytype.presentation.relations.add

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.presentation.relations.MultiValueParser
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.StatusParser

class AddOptionsRelationProvider {

    data class Options(
        val alreadySelected: List<RelationValueView.Option>,
        val notSelected: List<RelationValueView.Option>
    ) {
        val all = alreadySelected + notSelected
    }

    fun provideOptions(
        options: List<ObjectWrapper.Option>,
        relation: ObjectWrapper.Relation,
        record: Map<String, Any?>,
        relationId: Id
    ): Options {

        val selectedIds = MultiValueParser.parse<Id>(record[relationId])
        val views = mutableListOf<RelationValueView.Option>()

        when (relation.format) {
            RelationFormat.TAG -> {
                options.forEach { o ->
                    if (!selectedIds.contains(o.id)) {
                        val tag = RelationValueView.Option.Tag(
                            id = o.id,
                            name = o.title,
                            color = o.color,
                            isSelected = selectedIds.contains(o.id),
                            isCheckboxShown = true,
                        )
                        views.add(tag)
                    }
                }
            }
            RelationFormat.STATUS -> {
                val statusId = StatusParser.parse(record[relation.key])
                options.forEach { o ->
                    if (o.id != statusId) {
                        val statusView = RelationValueView.Option.Status(
                            id = o.id,
                            name = o.title,
                            color = o.color
                        )
                        views.add(statusView)
                    }
                }
            }
            else -> throw IllegalStateException("Unsupported format: ${relation.format}")
        }

        val groupBySelection = views.groupBy { selectedIds.contains(it.id) }

        return Options(
            alreadySelected = groupBySelection.getOrDefault(true, emptyList()),
            notSelected = groupBySelection.getOrDefault(false, emptyList()),
        )
    }
}