package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.templates.GetTemplates
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CreateNewObject(
    private val getDefaultEditorType: GetDefaultEditorType,
    private val getTemplates: GetTemplates,
    private val createPage: CreatePage,
) : ResultInteractor<Unit, Id>() {

    private suspend fun createPageWithType(type: Id): Id {
        val template = getTemplates.run(GetTemplates.Params(type)).firstOrNull()?.id
        return createPage.run(
            CreatePage.Params(
                ctx = null,
                isDraft = template == null,
                type = type,
                emoji = null,
                template = template
            )
        )
    }

    override suspend fun doWork(params: Unit) = getDefaultEditorType(Unit)
        .map { it.type }
        .catch { emit(null) }
        .map { type ->
            if (type == null) {
                createPage.run(
                    CreatePage.Params(
                        isDraft = true
                    )
                )
            } else {
                createPageWithType(type)
            }
        }
        .first()
}