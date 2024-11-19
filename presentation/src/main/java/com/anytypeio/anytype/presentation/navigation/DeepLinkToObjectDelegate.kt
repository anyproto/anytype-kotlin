package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.FetchObject
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
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
    ) : Result

    fun onDeepLinkToObjectAwait(
        obj: Id,
        space: SpaceId,
        switchSpaceIfObjectFound: Boolean = true
    ) : Flow<Result>

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
        ) : Result {
            val wrapper = fetchObject
                .async(
                    params = FetchObject.Params(
                        obj = obj,
                        space = space
                    )
                )
                .getOrNull()
            if (wrapper != null) {
                val permission = userPermissionProvider.get(space = space)
                return if (permission != null && permission.isAtLeastReader()) {
                    if (switchSpaceIfObjectFound) {
                        val switchSpaceResult = spaceManager.set(space = space.id)
                        if (switchSpaceResult.isSuccess) {
                            saveCurrentSpace.async(SaveCurrentSpace.Params(space = space))
                            Result.Success(wrapper)
                        } else {
                            Result.Error
                        }
                    } else {
                        return Result.Success(wrapper)
                    }
                } else {
                    Result.Error
                }
            } else {
                return Result.Error
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
                                        Result.Error
                                    }
                                } else {
                                    Result.Success(wrapper)
                                }
                            } else {
                                Result.Error
                            }
                        }
                )
            } else {
                emit(Result.Error)
            }
        }
    }

    sealed class Result {
        data object Error : Result()
        data class Success(val obj: ObjectWrapper.Basic): Result()
    }
}