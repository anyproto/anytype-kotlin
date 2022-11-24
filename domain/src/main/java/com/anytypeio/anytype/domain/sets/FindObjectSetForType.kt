package com.anytypeio.anytype.domain.sets

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.sets.FindObjectSetForType.Params
import com.anytypeio.anytype.domain.sets.FindObjectSetForType.Response

/**
 * Use-case for finding an object set for a type.
 * @see Response for details.
 * @see Params for details.
 */
class FindObjectSetForType(
    private val repo: BlockRepository
) : BaseUseCase<Response, Params>() {

    override suspend fun run(params: Params) = safe {
        val results = repo.searchObjects(
            limit = 1,
            filters = listOf(
                DVFilter(
                    relationKey = Relations.TYPE,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectTypeIds.SET
                ),
                DVFilter(
                    relationKey = Relations.SET_OF,
                    condition = DVFilterCondition.IN,
                    value = listOf(params.type)
                )
            ),
            sorts = emptyList(),
            fulltext = "",
            offset = 0
        )
        if (results.isNotEmpty()) {
            val obj = ObjectWrapper.Basic(results.first())
            check(obj.layout == ObjectType.Layout.SET) { "Unexpected layout for set" }
            Response.Success(
                type = params.type,
                obj = obj
            )
        } else {
            Response.NotFound(type = params.type)
        }
    }

    /**
     * @property [type] object type id
     */
    data class Params(val type: Id)

    sealed class Response {
        /**
         * Could not found a set for this [type].
         */
        data class NotFound(val type: Id) : Response()

        /**
         * Found a set for this [type].
         */
        data class Success(val type: Id, val obj: ObjectWrapper.Basic) : Response()
    }
}