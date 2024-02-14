package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository

/**
 * Use case for fetching last open object's id for restoring user session.
 * @see SaveLastOpenedObject
 * @see ClearLastOpenedObject
 */
class GetLastOpenedObject(
    private val settings: UserSettingsRepository,
    private val blockRepo: BlockRepository
) : BaseUseCase<GetLastOpenedObject.Response, GetLastOpenedObject.Params>() {

    override suspend fun run(params: Params) = safe {
        val lastOpenObject = settings.getLastOpenedObject(space = params.space)
        if (lastOpenObject == null) {
            Response.Empty
        } else {
            val searchResults = blockRepo.searchObjects(
                limit = 1,
                filters = listOf(
                    DVFilter(
                        relation = Relations.ID,
                        condition = DVFilterCondition.IN,
                        value = listOf(lastOpenObject, params.space.id)
                    )
                )
            )
            val wrappedObjects = searchResults.map { ObjectWrapper.Basic(it) }
            val lastOpenedObject = wrappedObjects.firstOrNull { it.id == lastOpenObject }
            if (lastOpenedObject != null) {
                Response.Success(lastOpenedObject)
            } else {
                Response.NotFound(lastOpenObject)
            }
        }
    }

    sealed class Response {
        /**
         * There was no information about the last opened object.
         */
        object Empty : Response()

        /**
         * The last opened object could not be found. It might have been deleted.
         */
        data class NotFound(val id: Id) : Response()

        /**
         * The last opened object has been found.
         */
        data class Success(val obj: ObjectWrapper.Basic) : Response()
    }

    data class Params(val space: SpaceId)
}