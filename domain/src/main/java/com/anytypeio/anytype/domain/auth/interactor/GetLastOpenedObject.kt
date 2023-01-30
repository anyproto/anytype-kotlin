package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use case for fetching last open object's id for restoring user session.
 * @see SaveLastOpenedObject
 * @see ClearLastOpenedObject
 */
class GetLastOpenedObject(
    private val authRepo: AuthRepository,
    private val blockRepo: BlockRepository
) : BaseUseCase<GetLastOpenedObject.Response, BaseUseCase.None>() {

    override suspend fun run(params: None) = safe {
        val lastOpenObjectId = authRepo.getLastOpenedObjectId()
        if (lastOpenObjectId == null) {
            Response.Empty
        } else {
            val searchResults = blockRepo.searchObjects(
                offset = 0,
                limit = 1,
                sorts = emptyList(),
                filters = listOf(
                    Block.Content.DataView.Filter(
                        relation = Relations.ID,
                        condition = Block.Content.DataView.Filter.Condition.EQUAL,
                        operator = Block.Content.DataView.Filter.Operator.AND,
                        value = lastOpenObjectId
                    )
                ),
                fulltext = ""
            )
            val wrappedObjects = searchResults.map { ObjectWrapper.Basic(it) }
            val lastOpenedObject = wrappedObjects.firstOrNull { it.id == lastOpenObjectId }
            if (lastOpenedObject != null) {
                Response.Success(lastOpenedObject)
            } else {
                Response.NotFound(lastOpenObjectId)
            }
        }
    }

    sealed class Response {
        /**
         * There was no information about the last opened object.
         */
        object Empty : Response()

        /**
         * The last opened object could not be found. It might habe been deleted.
         */
        data class NotFound(val id: Id) : Response()

        /**
         * The last opened object has been found.
         */
        data class Success(val obj: ObjectWrapper.Basic) : Response()
    }
}