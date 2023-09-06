package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Use-case for creating a new record inside data view's database.
 */
class CreateDataViewObject @Inject constructor(
    private val repo: BlockRepository,
    private val getTemplates: GetTemplates,
    private val getDefaultPageType: GetDefaultPageType,
    private val storeOfRelations: StoreOfRelations,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateDataViewObject.Params, CreateDataViewObject.Result>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result {
        val space = SpaceId(spaceManager.get())
        return when (params) {
            is Params.SetByType -> {
                val command = Command.CreateObject(
                    template = params.template,
                    prefilled = resolveSetByTypePrefilledObjectData(
                        filters = params.filters
                    ),
                    internalFlags = listOf(),
                    space = space,
                    type = params.type
                )
                val result = repo.createObject(command)
                Result(
                    objectId = result.id,
                    objectType = params.type
                )
            }
            is Params.SetByRelation -> {
                val type = resolveDefaultObjectType()
                val command = Command.CreateObject(
                    template = params.template,
                    prefilled = resolveSetByRelationPrefilledObjectData(
                        filters = params.filters,
                        relations = params.relations
                    ),
                    internalFlags = listOf(),
                    space = space,
                    type = type
                )
                val result = repo.createObject(command)
                Result(
                    objectId = result.id,
                    objectType = type
                )
            }
            is Params.Collection -> {
                val type = resolveDefaultObjectType()
                val command = Command.CreateObject(
                    template = params.templateId,
                    prefilled = resolveSetByRelationPrefilledObjectData(
                        filters = emptyList(),
                        relations = emptyList()
                    ),
                    internalFlags = listOf(),
                    space = space,
                    type = type
                )
                val result = repo.createObject(command)
                Result(
                    objectId = result.id,
                    objectType = type
                )
            }
        }
    }

    private suspend fun resolveSetByTypePrefilledObjectData(
        filters: List<DVFilter>
    ): Struct = buildMap {
        filters.forEach { filter ->
            val relation = storeOfRelations.getByKey(filter.relation)
            if (relation != null && relation.isReadOnly == false) {
                if (filter.condition == DVFilterCondition.ALL_IN || filter.condition == DVFilterCondition.IN || filter.condition == DVFilterCondition.EQUAL) {
                    filter.value?.let { put(filter.relation, it) }
                }
            }
        }
    }

    private suspend fun resolveSetByRelationPrefilledObjectData(
        filters: List<DVFilter>,
        relations: List<Key>
    ): Struct = try {
        buildMap {
            filters.forEach { filter ->
                val relation = storeOfRelations.getByKey(filter.relation)
                if (relation != null && !relation.isReadonlyValue) {
                    if (filter.condition == DVFilterCondition.ALL_IN || filter.condition == DVFilterCondition.IN || filter.condition == DVFilterCondition.EQUAL) {
                        val value = filter.value
                        if (value != null) {
                            put(filter.relation, value)
                        }
                    } else {
                        put(relation.key, resolveDefaultValueByFormat(relation.relationFormat))
                    }
                }
            }
            relations.forEach { id ->
                val relation = storeOfRelations.getById(id)
                if (relation != null && !containsKey(relation.key)) {
                    put(relation.key, resolveDefaultValueByFormat(relation.relationFormat))
                }
            }
        }
    } catch (e: Exception) {
        emptyMap()
    }

    private suspend fun resolveDefaultObjectType(): TypeKey {
        return getDefaultPageType
            .async(Unit)
            .getOrNull()?.type ?: TypeKey(ObjectTypeUniqueKeys.NOTE)
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
            val filters: List<DVFilter>,
            val relations: List<Id>,
            val template: Id?
        ) : Params()

        data class Collection(
            val templateId: Id?
        ) : Params()
    }

    data class Result(
        val objectId : Id,
        val objectType: TypeKey
    )

    companion object {
        const val EMPTY_STRING_VALUE = ""
    }
}