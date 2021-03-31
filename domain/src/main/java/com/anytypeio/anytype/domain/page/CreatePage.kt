package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.config.MainConfig
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

/**
 * A use-case for creating a new page.
 * Currently used for creating a new page inside a dashboard.
 */
class CreatePage(
    private val repo: BlockRepository,
    private val documentEmojiIconProvider: DocumentEmojiIconProvider
) : BaseUseCase<Id, CreatePage.Params>() {

    override suspend fun run(params: Params) = try {
        if (params.id == MainConfig.HOME_DASHBOARD_ID) {
            repo.getConfig().let { config ->
                repo.createPage(
                    parentId = config.home,
                    emoji = documentEmojiIconProvider.random()
                ).let {
                    Either.Right(it)
                }
            }
        } else {
            repo.createPage(
                parentId = params.id,
                emoji = documentEmojiIconProvider.random()
            ).let {
                Either.Right(it)
            }
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * @property id parent id for a new page
     */
    data class Params(val id: String) {
        companion object {
            fun insideDashboard() = Params(MainConfig.HOME_DASHBOARD_ID)
        }
    }
}