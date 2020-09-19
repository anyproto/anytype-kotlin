package com.agileburo.anytype.domain.page.navigation

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository

open class GetListPages(private val repo: BlockRepository) :
    BaseUseCase<GetListPages.Response, Unit>() {

    override suspend fun run(params: Unit): Either<Throwable, Response> = safe {
        val documents = repo.getListPages()
        val pages = documents.filterNot { document ->
            document.fields.isArchived == true
                    || document.type == DocumentInfo.Type.SET
                    || document.type == DocumentInfo.Type.HOME
        }
        Response(pages)
    }

    data class Response(
        val listPages: List<DocumentInfo>
    )
}