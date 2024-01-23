package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.dataview.interactor.DAYS_IN_MONTH
import com.anytypeio.anytype.domain.dataview.interactor.DAYS_IN_WEEK
import com.anytypeio.anytype.domain.dataview.interactor.SECONDS_IN_DAY
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.StoreOfRelations

suspend fun ObjectWrapper.Relation.resolveSetByRelationPrefilledObjectData(
    dvFilters: List<DVFilter>,
    objSetByRelation: ObjectWrapper.Relation,
    dvRelationLinks: List<RelationLink>,
    dateProvider: DateProvider,
    storeOfRelations: StoreOfRelations
): Struct {
    val prefillWithSetOf = buildMap {
        val relationFormat = objSetByRelation.relationFormat
        val defaultValue = resolveDefaultValueByFormat(relationFormat)
        put(objSetByRelation.key, defaultValue)
    }
    return prefillWithSetOf + prefillObjectDetails(
        dvFilters,
        dvRelationLinks,
        storeOfRelations,
        dateProvider
    )
}

private suspend fun prefillObjectDetails(
    filters: List<DVFilter>,
    dataViewRelationLinks: List<RelationLink>,
    storeOfRelations: StoreOfRelations,
    dateProvider: DateProvider
): Struct = buildMap {
    filters.forEach { filter ->
        val relationObject = storeOfRelations.getByKey(filter.relation) ?: return@forEach
        if (!relationObject.isReadonlyValue && CreateDataViewObject.permittedConditions.contains(
                filter.condition
            )
        ) {
            //Relation format should be taken from DataView relation links
            val filterRelationFormat =
                dataViewRelationLinks.firstOrNull { it.key == filter.relation }?.format
            when (filterRelationFormat) {
                Relation.Format.DATE -> {
                    val value = DateParser.parse(filter.value)
                    val updatedValue = filter.quickOption.getTimestampForQuickOption(
                        value = value,
                        dateProvider = dateProvider
                    )
                    if (updatedValue != null) {
                        put(filter.relation, updatedValue.toDouble())
                    }
                }

                else -> {
                    filter.value?.let { put(filter.relation, it) }
                }
            }
        }
    }
}

private fun resolveDefaultValueByFormat(format: RelationFormat): Any? {
    when (format) {
        Relation.Format.LONG_TEXT,
        Relation.Format.SHORT_TEXT,
        Relation.Format.URL,
        Relation.Format.EMAIL,
        Relation.Format.PHONE,
        Relation.Format.EMOJI -> {
            return CreateDataViewObject.EMPTY_STRING_VALUE
        }

        Relation.Format.NUMBER -> {
            return null
        }

        Relation.Format.CHECKBOX -> {
            return false
        }

        else -> {
            return null
        }
    }
}

private fun DVFilterQuickOption.getTimestampForQuickOption(
    value: Long?,
    dateProvider: DateProvider
): Long? {
    val option = this
    val time = dateProvider.getCurrentTimestampInSeconds()
    return when (option) {
        DVFilterQuickOption.DAYS_AGO -> {
            if (value == null) return null
            time - SECONDS_IN_DAY * value
        }

        DVFilterQuickOption.LAST_MONTH -> time - SECONDS_IN_DAY * DAYS_IN_MONTH
        DVFilterQuickOption.LAST_WEEK -> time - SECONDS_IN_DAY * DAYS_IN_WEEK
        DVFilterQuickOption.YESTERDAY -> time - SECONDS_IN_DAY
        DVFilterQuickOption.CURRENT_WEEK,
        DVFilterQuickOption.CURRENT_MONTH,
        DVFilterQuickOption.TODAY -> time

        DVFilterQuickOption.TOMORROW -> time + SECONDS_IN_DAY
        DVFilterQuickOption.NEXT_WEEK -> time + SECONDS_IN_DAY * DAYS_IN_WEEK
        DVFilterQuickOption.NEXT_MONTH -> time + SECONDS_IN_DAY * DAYS_IN_MONTH
        DVFilterQuickOption.DAYS_AHEAD -> {
            if (value == null) return null
            time + SECONDS_IN_DAY * value
        }

        DVFilterQuickOption.EXACT_DATE -> value
    }
}