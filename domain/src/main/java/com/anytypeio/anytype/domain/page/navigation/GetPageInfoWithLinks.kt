package com.anytypeio.anytype.domain.page.navigation

import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.PageInfoWithLinks
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetPageInfoWithLinks(private val repo: BlockRepository) :
    BaseUseCase<GetPageInfoWithLinks.Response, GetPageInfoWithLinks.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Response> = safe {
        repo.getPageInfoWithLinks(
            pageId = params.pageId
        )
            .let {
                Response(
                    pageInfoWithLinks = it.copy(
                        links = it.links.copy(
                            outbound = it.links.outbound.filterNot { doc ->
                                doc.fields.isArchived == true
                                        || doc.type == DocumentInfo.Type.SET
                                        || doc.type == DocumentInfo.Type.ARCHIVE
                                        || doc.type == DocumentInfo.Type.OBJECT_TYPE
                                        || doc.type == DocumentInfo.Type.RELATION
                                        || doc.type == DocumentInfo.Type.FILE
                                        || doc.type == DocumentInfo.Type.PROFILE_PAGE
                                        //    TODO Filter by profile object Anytype, maybe will change in future
                                        || doc.id == GetListPages.ANYTYPE_PROFILE_ID
                            },
                            inbound = it.links.inbound.filterNot { doc ->
                                doc.fields.isArchived == true
                                        || doc.type == DocumentInfo.Type.SET
                                        || doc.type == DocumentInfo.Type.ARCHIVE
                                        || doc.type == DocumentInfo.Type.OBJECT_TYPE
                                        || doc.type == DocumentInfo.Type.RELATION
                                        || doc.type == DocumentInfo.Type.FILE
                                        || doc.type == DocumentInfo.Type.PROFILE_PAGE
                                        //    TODO Filter by profile object Anytype, maybe will change in future
                                        || doc.id == GetListPages.ANYTYPE_PROFILE_ID
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