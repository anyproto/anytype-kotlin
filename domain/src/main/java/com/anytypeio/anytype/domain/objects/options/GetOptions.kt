package com.anytypeio.anytype.domain.objects.options

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetOptions(
    private val repo: BlockRepository
) : BaseUseCase<List<ObjectWrapper.Option>, GetOptions.Params>() {

    override suspend fun run(params: Params) = safe {
        val filters = buildList {
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.RELATION_OPTION.code.toDouble()
                )
            )
            add(
                DVFilter(
                    relation = Relations.IS_DELETED,
                    condition = DVFilterCondition.EQUAL,
                    value = false
                )
            )
            add(
                DVFilter(
                    relation = Relations.IS_ARCHIVED,
                    condition = DVFilterCondition.EQUAL,
                    value = false
                )
            )
            add(
                DVFilter(
                    relation = Relations.RELATION_KEY,
                    condition = DVFilterCondition.EQUAL,
                    value = params.relation
                )
            )
            add(
                DVFilter(
                    relation = Relations.SPACE_ID,
                    condition = DVFilterCondition.EQUAL,
                    value = params.space
                )
            )
        }
        repo.searchObjects(
            sorts = emptyList(),
            filters = filters,
            limit = 0,
            offset = 0,
            keys = listOf(
                Relations.ID,
                Relations.SPACE_ID,
                Relations.NAME,
                Relations.RELATION_OPTION_COLOR
            ),
            fulltext = "",
        ).map {
            ObjectWrapper.Option(it)
        }
    }

    data class Params(val space: Id, val relation: Key)
}