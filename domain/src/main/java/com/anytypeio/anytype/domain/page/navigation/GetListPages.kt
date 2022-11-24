package com.anytypeio.anytype.domain.page.navigation

import com.anytypeio.anytype.core_models.DocumentInfo
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

@Deprecated("Legacy. Consider switching to ObjectSearch")
open class GetListPages(
    private val repo: BlockRepository
) : BaseUseCase<GetListPages.Response, Unit>() {

    override suspend fun run(params: Unit): Either<Throwable, Response> = safe {
        val documents = repo.getListPages()
        val pages = documents.filterNot { document ->
            document.obj.isArchived == true
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
                    || document.smartBlockType == SmartBlockType.SUB_OBJECT
                    || document.smartBlockType == SmartBlockType.DATABASE
                    || document.smartBlockType == SmartBlockType.ANYTYPE_PROFILE
                    || (document.smartBlockType == SmartBlockType.PROFILE_PAGE
                    && document.id == ANYTYPE_PROFILE_ID)
        }
        Response(pages)
    }

    data class Response(
        val listPages: List<DocumentInfo>
    )

    companion object {
        const val ANYTYPE_PROFILE_ID = "_anytype_profile"
    }
}