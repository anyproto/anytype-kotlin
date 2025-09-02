package com.anytypeio.anytype.domain.publishing

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetPublishingDomain @Inject constructor(
    private val auth: AuthRepository,
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<GetPublishingDomain.Params, String?>(dispatchers.io) {

    override suspend fun doWork(params: Params): String? {
        val account = auth.getCurrentAccountId()
        val results = repo.searchObjects(
            space = params.space,
            limit = 1,
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.GLOBAL_NAME
            ),
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        value = ObjectType.Layout.PARTICIPANT.code.toDouble(),
                        condition = DVFilterCondition.EQUAL
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.IDENTITY,
                        value = account,
                        condition = DVFilterCondition.EQUAL
                    )
                )
            },
        )
        if (results.isNotEmpty()) {
            val wrapper = ObjectWrapper.SpaceMember(results.first())
            val globalName = wrapper.globalName
            return if (globalName.isNullOrEmpty()) {
                "$FREE_DOMAIN/$account"
            } else {
                "$globalName.$PAID_DOMAIN"
            }
        } else {
            return "$FREE_DOMAIN/$account"
        }
    }

    data class Params(val space: SpaceId)

    companion object {
        private const val FREE_DOMAIN = "any.coop"
        private const val PAID_DOMAIN = "org"
    }
}