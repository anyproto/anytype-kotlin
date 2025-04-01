package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.sun.swing.internal.plaf.basic.resources.basic_ko
import javax.inject.Inject
import kotlin.math.log

class GetSuggestedWidgetTypes @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository,
    private val logger: Logger
) : ResultInteractor<GetSuggestedWidgetTypes.Params, List<ObjectWrapper.Type>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<ObjectWrapper.Type> {

        val alreadyUsedObjectTypes = getAlreadyUsedTypes(
            space = params.space,
            widgets = params.ctx
        )

        logger.logWarning("Already used: $alreadyUsedObjectTypes")

        val types = repo.searchObjects(
            space = params.space,
            limit = DEFAULT_LIMIT,
            keys = params.objectTypeKeys,
            filters = buildList {
                addAll(params.objectTypeFilters)
                if (alreadyUsedObjectTypes.isNotEmpty()) {
                    add(
                        DVFilter(
                            relation = Relations.ID,
                            condition = DVFilterCondition.NOT_IN,
                            value = alreadyUsedObjectTypes
                        )
                    )
                }
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeUniqueKeys.OBJECT_TYPE
                    )
                )
            }
        ).map { result ->
            ObjectWrapper.Type(result)
        }

        return types
    }

    private suspend fun getAlreadyUsedTypes(space: SpaceId, widgets: Id) : List<Id> {
        val result = mutableListOf<Id>()
        val preview = repo.getObject(space = space, id = widgets)
        val map = preview.blocks.asMap()
        logger.logWarning("Build map: ${map}")
        map.getOrDefault(widgets, emptyList()).forEach { block ->
            if (block.content is Block.Content.Widget && block.children.isNotEmpty()) {
                val link = preview.blocks.find { it.id == block.children.first() }
                val content = link?.content
                if (content is Block.Content.Link) {
                    val source = preview.details.getOrDefault(
                        content.target,
                        emptyMap()
                    )
                    val wrapper = ObjectWrapper.Basic(source)
                    logger.logWarning("Found wrapper: $wrapper")
                    if (wrapper.layout == ObjectType.Layout.OBJECT_TYPE) {
                        result.add(wrapper.id)
                    }
                } else {
                    logger.logWarning("Already used: empty content, content: $content")
                    // Do nothing
                }
            }
        }

        return result.distinct()
    }


    data class Params(
        val space: Space,
        val ctx: Id,
        val objectTypeFilters: List<DVFilter>,
        val objectTypeKeys: List<Id>
    )
    
    companion object {
        const val DEFAULT_LIMIT = 5
    }
}