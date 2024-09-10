package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject

class CreateObjectByTypeAndTemplate @Inject constructor(
    private val repo: BlockRepository,
    private val logger: Logger,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObjectByTypeAndTemplate.Param,
        CreateObjectByTypeAndTemplate.Result>(dispatchers.io) {

    override suspend fun doWork(params: Param): Result {
        val wrapper = searchObjectType(params)
        logger.logInfo("CreateObjectByTypeAndTemplate, search object type:[$wrapper]")
        return wrapper?.let {
            createObject(
                typeKey = params.typeKey,
                template = it.defaultTemplateId,
                internalFlags = params.internalFlags,
                prefilled = params.prefilled,
                space = params.space
            )
        } ?: throw RuntimeException("Object type not found")
    }

    @Throws(RuntimeException::class)
    private suspend fun searchObjectType(params: Param): ObjectWrapper.Type? {
        try {
            val struct = repo.searchObjects(
                limit = 1,
                keys = params.keys,
                sorts = params.sorts,
                filters = listOf(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.EQUAL,
                        value = params.typeKey.key
                    ),
                    DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.IS_HIDDEN,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.SPACE_ID,
                        condition = DVFilterCondition.IN,
                        value = listOf(params.space.id)
                    )
                ),
            )
            return if (struct.isNotEmpty()) {
                ObjectWrapper.Type(struct.first())
            } else {
                null
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to search object type", e)
        }
    }

    private suspend fun createObject(
        typeKey: TypeKey,
        template: Id?,
        internalFlags: List<InternalFlags>,
        prefilled: Struct,
        space: SpaceId
    ): Result {
        val createCommand = Command.CreateObject(
            space = space,
            typeKey = typeKey,
            template = template,
            prefilled = prefilled,
            internalFlags = internalFlags
        )

        val result = repo.createObject(createCommand)

        return Result(
            objectId = result.id,
            event = result.event,
            appliedTemplate = template,
            typeKey = typeKey,
            obj = ObjectWrapper.Basic(result.details)
        )
    }

    data class Param(
        val typeKey: TypeKey,
        val space: SpaceId,
        val keys: List<String>,
        val sorts: List<DVSort> = emptyList(),
        val filters: List<DVFilter> = emptyList(),
        val prefilled: Struct = emptyMap(),
        val internalFlags: List<InternalFlags> = emptyList()
    )

    data class Result(
        val objectId: Id,
        val event: Payload,
        val appliedTemplate: String? = null,
        val typeKey: TypeKey,
        val obj: ObjectWrapper.Basic
    )
}