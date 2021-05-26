package com.anytypeio.anytype.domain.page.navigation

import com.anytypeio.anytype.core_models.PageInfoWithLinks
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetPageInfoWithLinks(private val repo: BlockRepository) :
    BaseUseCase<GetPageInfoWithLinks.Response, GetPageInfoWithLinks.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Response> = safe {
        repo.getPageInfoWithLinks(
            pageId = params.pageId
        ).let {
            Response(
                pageInfoWithLinks = it.copy(
                    links = it.links.copy(
                        outbound = it.links.outbound.filterNot { document ->
                            document.fields.isArchived == true
                                    || document.smartBlockType == SmartBlockType.SET
                                    || document.smartBlockType == SmartBlockType.BREADCRUMBS
                                    || document.smartBlockType == SmartBlockType.HOME
                                    || document.smartBlockType == SmartBlockType.BUNDLED_OBJECT_TYPE
                                    || document.smartBlockType == SmartBlockType.CUSTOM_OBJECT_TYPE
                                    || document.smartBlockType == SmartBlockType.MARKETPLACE_TYPE
                                    || document.smartBlockType == SmartBlockType.MARKETPLACE_TEMPLATE
                                    || document.smartBlockType == SmartBlockType.MARKETPLACE_RELATION
                                    || document.smartBlockType == SmartBlockType.FILE
                                    || document.smartBlockType == SmartBlockType.TEMPLATE
                                    || document.smartBlockType == SmartBlockType.BUNDLED_RELATION
                                    || document.smartBlockType == SmartBlockType.INDEXED_RELATION
                                    || document.smartBlockType == SmartBlockType.DATABASE
                                    || document.smartBlockType == SmartBlockType.ANYTYPE_PROFILE
                        },
                        inbound = it.links.inbound.filterNot { document ->
                            document.fields.isArchived == true
                                    || document.smartBlockType == SmartBlockType.SET
                                    || document.smartBlockType == SmartBlockType.BREADCRUMBS
                                    || document.smartBlockType == SmartBlockType.HOME
                                    || document.smartBlockType == SmartBlockType.BUNDLED_OBJECT_TYPE
                                    || document.smartBlockType == SmartBlockType.CUSTOM_OBJECT_TYPE
                                    || document.smartBlockType == SmartBlockType.MARKETPLACE_TYPE
                                    || document.smartBlockType == SmartBlockType.MARKETPLACE_TEMPLATE
                                    || document.smartBlockType == SmartBlockType.MARKETPLACE_RELATION
                                    || document.smartBlockType == SmartBlockType.FILE
                                    || document.smartBlockType == SmartBlockType.TEMPLATE
                                    || document.smartBlockType == SmartBlockType.BUNDLED_RELATION
                                    || document.smartBlockType == SmartBlockType.INDEXED_RELATION
                                    || document.smartBlockType == SmartBlockType.DATABASE
                                    || document.smartBlockType == SmartBlockType.ANYTYPE_PROFILE
                        }
                    )
                )
            )
        }
    }

    data class Params(
        val pageId: String
    )

    data class Response(
        val pageInfoWithLinks: PageInfoWithLinks
    )
}