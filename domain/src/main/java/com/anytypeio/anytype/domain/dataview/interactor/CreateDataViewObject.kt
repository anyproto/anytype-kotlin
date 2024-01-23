package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Use-case for creating a new record inside data view's database.
 */
class CreateDataViewObject @Inject constructor(
    private val repo: BlockRepository,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateDataViewObject.Params, CreateDataViewObject.Result>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result {
        val space = SpaceId(spaceManager.get())
        return when (params) {
            is Params.SetByType -> {
                val command = Command.CreateObject(
                    template = params.template,
                    prefilled = params.prefilled,
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
                val command = Command.CreateObject(
                    template = params.template,
                    prefilled = params.prefilled,
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
                    prefilled = params.prefilled,
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

    sealed class Params {
        data class SetByType(
            val type: TypeKey,
            val filters: List<DVFilter>,
            val template: Id?,
            val prefilled: Struct
        ) : Params()

        data class SetByRelation(
            val type: TypeKey,
            val filters: List<DVFilter>,
            val template: Id?,
            val prefilled: Struct
        ) : Params()

        data class Collection(
            val type: TypeKey,
            val filters: List<DVFilter>,
            val template: Id?,
            val prefilled: Struct
        ) : Params()
    }

    data class Result(
        val objectId : Id,
        val objectType: TypeKey,
        val struct: Struct? = null
    )
}

