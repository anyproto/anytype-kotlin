package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.templates.GetTemplates

/**
 * UseCase for creating a new object as mention or as text link markup
 */

class CreateObjectAsMentionOrLink(
    private val repo: BlockRepository,
    private val getDefaultPageType: GetDefaultPageType,
    private val getTemplates: GetTemplates,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateObjectAsMentionOrLink.Params, CreateObjectAsMentionOrLink.Result>(
    dispatchers.io
) {

    override suspend fun doWork(params: Params): Result {

        val type = params.type ?: getDefaultPageType.run(Unit).type

        val prefilled = buildMap {
            if (type != null) put(Relations.TYPE, type)
            put(Relations.NAME, params.name)
        }

        val template = if (type != null) {
            getTemplates.run(GetTemplates.Params(type)).singleOrNull()?.id
        } else {
            null
        }

        val command = Command.CreateObject(
            template = template,
            prefilled = prefilled,
            internalFlags = listOf()
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
        val type: String?
    )

    data class Result(
        val id: String,
        val name: String
    )
}