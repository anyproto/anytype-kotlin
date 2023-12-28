package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Use-case for creating a new record inside data view's database.
 */
class CreateDataViewObject @Inject constructor(
    private val repo: BlockRepository,
    private val storeOfRelations: StoreOfRelations,
    private val spaceManager: SpaceManager,
    private  val dateProvider: DateProvider,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateDataViewObject.Params, CreateDataViewObject.Result>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result {
        val space = SpaceId(spaceManager.get())
        return when (params) {
            is Params.SetByType -> {
                val command = Command.CreateObject(
                    template = params.template,
                    prefilled = prefillObjectDetails(
                        filters = params.filters
                    ),
                    internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
                    space = space,
                    typeKey = params.type
                )
                val result = repo.createObject(command)
                Result(
                    objectId = result.id,
                    objectType = params.type,
                    struct = result.details
                )
            }
            is Params.SetByRelation -> {
                val prefilled = resolveSetByRelationPrefilledObjectData(
                    filters = params.filters,
                    setOfIds = params.relations
                )
                val command = Command.CreateObject(
                    template = params.template,
                    prefilled = prefilled,
                    internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
                    space = space,
                    typeKey = params.type
                )
                val result = repo.createObject(command)
                Result(
                    objectId = result.id,
                    objectType = params.type,
                    struct = result.details
                )
            }
            is Params.Collection -> {
                val command = Command.CreateObject(
                    template = params.template,
                    prefilled = prefillObjectDetails(
                        filters = params.filters
                    ),
                    internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
                    space = space,
                    typeKey = params.type
                )
                val result = repo.createObject(command)
                Result(
                    objectId = result.id,
                    objectType = params.type,
                    struct = result.details
                )
            }
        }
    }

    private suspend fun resolveSetByRelationPrefilledObjectData(
        filters: List<DVFilter>,
        setOfIds: List<Id>
    ): Struct {
        val prefillWithSetOf = buildMap {
            setOfIds.forEach { relation ->
                val relationObject = storeOfRelations.getById(relation) ?: return@forEach
                put(relationObject.key, resolveDefaultValueByFormat(relationObject.relationFormat))
            }
        }
        return prefillWithSetOf + prefillObjectDetails(filters)
    }

    private suspend fun prefillObjectDetails(
        filters: List<DVFilter>
    ): Struct = buildMap {
        filters.forEach { filter ->
            val relationObject = storeOfRelations.getByKey(filter.relation) ?: return@forEach
            if (!relationObject.isReadonlyValue && permittedConditions.contains(filter.condition)) {
                when (filter.relationFormat) {
                    Relation.Format.DATE -> {
                        val value = DateParser.parse(filter.value)
                        val updatedValue = filter.quickOption.getTimestampForQuickOption(
                            value = value,
                            dateProvider = dateProvider
                        )
                        if (updatedValue != null)  {
                            put(filter.relation, updatedValue)
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
                return EMPTY_STRING_VALUE
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

    sealed class Params {
        data class SetByType(
            val type: TypeKey,
            val filters: List<DVFilter>,
            val template: Id?
        ) : Params()

        data class SetByRelation(
            val type: TypeKey,
            val filters: List<DVFilter>,
            val relations: List<Id>,
            val template: Id?
        ) : Params()

        data class Collection(
            val type: TypeKey,
            val filters: List<DVFilter>,
            val template: Id?
        ) : Params()
    }

    data class Result(
        val objectId : Id,
        val objectType: TypeKey,
        val struct: Struct? = null
    )

    companion object {
        const val EMPTY_STRING_VALUE = ""

        val permittedConditions = listOf(
            DVFilterCondition.ALL_IN,
            DVFilterCondition.IN,
            DVFilterCondition.EQUAL,
            DVFilterCondition.GREATER_OR_EQUAL,
            DVFilterCondition.LESS_OR_EQUAL
        )
    }
}

private fun DVFilterQuickOption.getTimestampForQuickOption(value: Long?, dateProvider: DateProvider): Long? {
    val option = this
    val time = dateProvider.getNowInSeconds()
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

const val SECONDS_IN_DAY = 86400
const val DAYS_IN_MONTH = 30
const val DAYS_IN_WEEK = 7