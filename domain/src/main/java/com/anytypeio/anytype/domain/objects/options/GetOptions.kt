package com.anytypeio.anytype.domain.objects.options

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeIds
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
                    relationKey = Relations.TYPE,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectTypeIds.RELATION_OPTION
                )
            )
            add(
                DVFilter(
                    relationKey = Relations.IS_DELETED,
                    condition = DVFilterCondition.EQUAL,
                    value = false
                )
            )
            add(
                DVFilter(
                    relationKey = Relations.IS_ARCHIVED,
                    condition = DVFilterCondition.EQUAL,
                    value = false
                )
            )
            add(
                DVFilter(
                    relationKey = Relations.RELATION_KEY,
                    condition = DVFilterCondition.EQUAL,
                    value = params.relation
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
                Relations.RELATION_OPTION_TEXT,
                Relations.RELATION_OPTION_COLOR
            ),
            fulltext = "",
        ).map {
            ObjectWrapper.Option(it)
        }
    }

    data class Params(val relation: Key)
}