package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.FetchObject
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface DeepLinkToObjectDelegate {

    /**
     * N.B. Can switch space as side-effect.
     */
    suspend fun onDeepLinkToObject(
        obj: Id,
        space: SpaceId,
        switchSpaceIfObjectFound: Boolean = true
    ): Result

    fun onDeepLinkToObjectAwait(
        obj: Id,
        space: SpaceId,
        switchSpaceIfObjectFound: Boolean = true
    ): Flow<Result>

    class Default @Inject constructor(
        private val spaceManager: SpaceManager,
        private val saveCurrentSpace: SaveCurrentSpace,
        private val userPermissionProvider: UserPermissionProvider,
        private val fetchObject: FetchObject
    ) : DeepLinkToObjectDelegate {

        override suspend fun onDeepLinkToObject(
            obj: Id,
            space: SpaceId,
            switchSpaceIfObjectFound: Boolean
        ): Result {
            val wrapper = fetchObject
                .async(
                    params = FetchObject.Params(
                        obj = obj,
                        space = space
                    )
                )
                .getOrNull()
            if (wrapper != null) {
                if (wrapper.notDeletedNorArchived) {
                    val permission = userPermissionProvider.get(space = space)
                    return if (permission != null && permission.isAtLeastReader()) {
                        if (switchSpaceIfObjectFound) {
                            val switchSpaceResult = spaceManager.set(space = space.id)
                            if (switchSpaceResult.isSuccess) {
                                saveCurrentSpace.async(SaveCurrentSpace.Params(space = space))
                                Result.Success(wrapper)
                            } else {
                                Result.Error.CouldNotOpenSpace
                            }
                        } else {
                            return Result.Success(wrapper)
                        }
                    } else {
                        Result.Error.PermissionNeeded
                    }
                } else {
                    return Result.Error.ObjectNotFound
                }
            } else {
                return Result.Error.ObjectNotFound
            }
        }


        override fun onDeepLinkToObjectAwait(
            obj: Id,
            space: SpaceId,
            switchSpaceIfObjectFound: Boolean
        ): Flow<Result> = flow {
            val wrapper = fetchObject
                .async(
                    params = FetchObject.Params(
                        obj = obj,
                        space = space
                    )
                )
                .getOrNull()

            if (wrapper != null) {
                emitAll(
                    userPermissionProvider
                        .observe(space = space)
                        .filter { permission -> permission != null }
                        .map { permission ->
                            if (permission?.isAtLeastReader() == true) {
                                if (switchSpaceIfObjectFound) {
                                    val switchSpaceResult = spaceManager.set(space = space.id)
                                    if (switchSpaceResult.isSuccess) {
                                        saveCurrentSpace.async(SaveCurrentSpace.Params(space = space))
                                        Result.Success(wrapper)
                                    } else {
                                        Result.Error.CouldNotOpenSpace
                                    }
                                } else {
                                    Result.Success(wrapper)
                                }
                            } else {
                                Result.Error.PermissionNeeded
                            }
                        }
                )
            } else {
                emit(Result.Error.ObjectNotFound)
            }
        }
    }

    sealed class Result {
        sealed class Error : Result() {
            object PermissionNeeded : Error()
            object ObjectNotFound : Error()
            object CouldNotOpenSpace : Error()
        }

        data class Success(val obj: ObjectWrapper.Basic) : Result()
    }
}