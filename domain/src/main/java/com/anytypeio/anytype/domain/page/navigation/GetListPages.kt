package com.anytypeio.anytype.domain.page.navigation

import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

open class GetListPages(private val repo: BlockRepository) :
    BaseUseCase<GetListPages.Response, Unit>() {

    override suspend fun run(params: Unit): Either<Throwable, Response> = safe {
        val documents = repo.getListPages()
        val pages = documents.filterNot { document ->
            document.fields.isArchived == true
                || document.type == DocumentInfo.Type.SET
                || document.type == DocumentInfo.Type.HOME
                || document.type == DocumentInfo.Type.RELATION
                || document.type == DocumentInfo.Type.OBJECT_TYPE
        }
        Response(pages)
    }

    data class Response(
        val listPages: List<DocumentInfo>
    )
}