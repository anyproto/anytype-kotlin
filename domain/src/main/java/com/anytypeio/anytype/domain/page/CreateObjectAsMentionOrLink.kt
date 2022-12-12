package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.templates.GetTemplates

/**
 * UseCase for creating a new object as mention or as text link markup
 */

class CreateObjectAsMentionOrLink(
    private val repo: BlockRepository,
    private val getDefaultEditorType: GetDefaultEditorType,
    private val getTemplates: GetTemplates
) : ResultInteractor<CreateObjectAsMentionOrLink.Params, CreateObjectAsMentionOrLink.Result>() {

    override suspend fun doWork(params: Params): Result {

        val type = params.type ?: getDefaultEditorType.run(Unit).type

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