package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.templates.GetTemplates

/**
 * Use-case for creating a new record inside data view's database.
 */
class CreateDataViewObject(
    private val repo: BlockRepository,
    private val getTemplates: GetTemplates,
    private val getDefaultEditorType: GetDefaultEditorType,
    private val storeOfRelations: StoreOfRelations
) : BaseUseCase<Id, CreateDataViewObject.Params>() {

    override suspend fun run(params: Params) = safe {
        when (params) {
            is Params.SetByType -> {
                val command = Command.CreateObject(
                    template = resolveTemplateForNewObject(type = params.type),
                    prefilled = resolveSetByTypePrefilledObjectData(
                        filters = params.filters,
                        type = params.type
                    ),
                    internalFlags = listOf()
                )
                val result = repo.createObject(command)
                result.id
            }
            is Params.SetByRelation -> {
                val type = resolveDefaultObjectType()
                val command = Command.CreateObject(
                    template = resolveTemplateForNewObject(type = type),
                    prefilled = resolveSetByRelationPrefilledObjectData(
                        filters = params.filters,
                        relations = params.relations,
                        type = type
                    ),
                    internalFlags = listOf()
                )
                val result = repo.createObject(command)
                result.id
            }
        }
    }

    private suspend fun resolveSetByTypePrefilledObjectData(
        filters: List<DVFilter>,
        type: Id
    ): Struct =
        buildMap {
            filters.forEach { filter ->
                val relation = storeOfRelations.getByKey(filter.relation)
                if (relation != null && relation.isReadOnly == false) {
                    if (filter.condition == DVFilterCondition.ALL_IN || filter.condition == DVFilterCondition.IN || filter.condition == DVFilterCondition.EQUAL) {
                        filter.value?.let { put(filter.relation, it) }
                    }
                }
            }
            put(Relations.TYPE, type)
        }

    private suspend fun resolveTemplateForNewObject(type: Id): Id? {
        val templates = try {
            getTemplates.run(GetTemplates.Params(type))
        } catch (e: Exception) {
            emptyList()
        }
        return templates.singleOrNull()?.id
    }

    private suspend fun resolveSetByRelationPrefilledObjectData(
        filters: List<DVFilter>,
        relations: List<Key>,
        type: Id? = null
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
            if (type != null) {
                put(Relations.TYPE, type)
            }
        }
    } catch (e: Exception) {
        emptyMap()
    }

    private suspend fun resolveDefaultObjectType(): Id {
        return try {
            getDefaultEditorType.run(Unit).type ?: ObjectTypeIds.NOTE
        } catch (e: Exception) {
            ObjectTypeIds.NOTE
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
            val type: Id,
            val filters: List<DVFilter>
        ) : Params()

        data class SetByRelation(
            val filters: List<DVFilter>,
            val relations: List<Id>
        ) : Params()
    }

    companion object {
        const val EMPTY_STRING_VALUE = ""
    }
}