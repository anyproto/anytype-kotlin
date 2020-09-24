package com.anytypeio.anytype.domain.page.navigation

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
                                doc.fields.isArchived == true || doc.type == DocumentInfo.Type.SET
                            },
                            inbound = it.links.inbound.filterNot { doc ->
                                doc.fields.isArchived == true || doc.type == DocumentInfo.Type.SET
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