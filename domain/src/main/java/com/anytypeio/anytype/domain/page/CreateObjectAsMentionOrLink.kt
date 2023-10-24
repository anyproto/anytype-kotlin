package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager

/**
 * UseCase for creating a new object as mention or as text link markup
 */

class CreateObjectAsMentionOrLink(
    private val repo: BlockRepository,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObjectAsMentionOrLink.Params, CreateObjectAsMentionOrLink.Result>(
    dispatchers.io
) {

    override suspend fun doWork(params: Params): Result {

        val space = SpaceId(spaceManager.get())

        val typeKey = params.typeKey

        val prefilled = buildMap {
            put(Relations.NAME, params.name)
        }

        val command = Command.CreateObject(
            template = params.defaultTemplate,
            prefilled = prefilled,
            internalFlags = listOf(),
            space = space,
            type = typeKey
        )
        val result = repo.createObject(command)

        return Result(
            id = result.id,
            name = params.name
        )
    }

    /**
     * [name] name for new object
     * [type] type for new object
     */
    data class Params(
        val name: String,
        val typeKey: TypeKey,
        val defaultTemplate: String? = null
    )

    data class Result(
        val id: String,
        val name: String
    )
}