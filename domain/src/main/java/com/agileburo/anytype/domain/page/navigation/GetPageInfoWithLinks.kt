package com.agileburo.anytype.domain.page.navigation

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository

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
                            outbound = it.links.outbound.filterNot { page -> page.fields.isArchived == true },
                            inbound = it.links.inbound.filterNot { page -> page.fields.isArchived == true }
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