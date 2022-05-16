package com.anytypeio.anytype.presentation.relations.add

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.presentation.relations.RelationValueView

class AddOptionsRelationProvider {

    data class Options(
        val alreadySelected: List<RelationValueView.Option>,
        val notSelected: List<RelationValueView.Option>
    ) {
        val all = alreadySelected + notSelected
    }

    fun provideOptions(
        relation: Relation,
        record: Map<String, Any?>,
        relationId: Id
    ): Options {
        val selections = relation.selections
        val relatedIds = record[relationId] as? List<*> ?: emptyList<Id>()
        val selectedIds = relatedIds.typeOf<Id>()
        val options: List<RelationValueView.Option> = when (relation.format) {
            Relation.Format.TAG -> provideForTag(selections, selectedIds)
            Relation.Format.STATUS -> provideForStatus(selections)
            else -> throw IllegalStateException("Unsupported format: ${relation.format}")
        }
        val groupBySelection = options.groupBy { selectedIds.contains(it.id) }

        return Options(
            alreadySelected = groupBySelection.getOrDefault(true, emptyList()),
            notSelected = groupBySelection.getOrDefault(false, emptyList()),
        )
    }

    private fun provideForTag(
        options: List<Relation.Option>,
        selectedIds: List<Id>
    ): List<RelationValueView.Option> {
        return options.map { option ->
            RelationValueView.Option.Tag(
                id = option.id,
                name = option.text,
                color = option.color.ifEmpty { null },
                isSelected = selectedIds.contains(option.id),
                isCheckboxShown = true,
            )
        }
    }

    private fun provideForStatus(
        options: List<Relation.Option>
    ): List<RelationValueView.Option> {
        return options.map { option ->
            RelationValueView.Option.Status(
                id = option.id,
                name = option.text,
                color = option.color.ifEmpty { null },
            )
        }
    }
}