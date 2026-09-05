package com.anytypeio.anytype.domain.quickcapture

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads a quick-capture draft from the object store, scoped to this account.
 *
 * A draft is unique per space *per participant*: every member of a shared space keeps their
 * own, and other members' drafts are invisible — they are hidden objects belonging to
 * someone else, and nothing in quick capture may surface or act on one. The creator filter
 * is therefore part of the query rather than a check applied to the result: the store never
 * returns another participant's draft in the first place, so there is no window in which one
 * could be adopted, counted, or deleted.
 *
 * The participant id differs per space for the same account, which is why it is resolved
 * against [SpaceId] rather than taken as a single account-wide value.
 *
 * If our participant cannot be resolved (the subscription has not populated yet), the query
 * falls back to an id-only lookup rather than returning nothing — otherwise a cold start
 * would report "no draft" and strand the existing one behind a freshly created replacement.
 * That fallback is safe because the id came from this device's own pointer.
 */
class FetchQuickCaptureDraft @Inject constructor(
    private val repo: BlockRepository,
    private val participants: ParticipantSubscriptionContainer,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<FetchQuickCaptureDraft.Params, ObjectWrapper.Basic?>(dispatchers.io) {

    override suspend fun doWork(params: Params): ObjectWrapper.Basic? {
        val creator = ownParticipantId(params.space)
        val filters = buildList {
            add(
                DVFilter(
                    relation = Relations.ID,
                    value = params.draft,
                    condition = DVFilterCondition.EQUAL
                )
            )
            if (creator != null) {
                add(
                    DVFilter(
                        relation = Relations.CREATOR,
                        value = creator,
                        condition = DVFilterCondition.EQUAL
                    )
                )
            }
        }
        val result = repo.searchObjects(
            space = params.space,
            filters = filters,
            limit = 1,
            keys = params.keys
        )
        return result.firstOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?.let { ObjectWrapper.Basic(it) }
    }

    /** This account's participant object in [space] — the value recorded as `creator`. */
    private suspend fun ownParticipantId(space: SpaceId): Id? {
        val identity = runCatching { auth.getCurrentAccountId() }.getOrNull() ?: return null
        fun find(members: List<ObjectWrapper.SpaceMember>): Id? = members.firstOrNull { member ->
            member.identity == identity && member.spaceId == space.id
        }?.id
        find(participants.get())?.let { return it }
        // Not populated yet — wait briefly rather than silently querying unscoped.
        return withTimeoutOrNull(PARTICIPANT_AWAIT_TIMEOUT_MS) {
            participants.observe().first { find(it) != null }.let(::find)
        }
    }

    data class Params(
        val space: SpaceId,
        val draft: Id,
        val keys: List<Key>
    )

    companion object {
        const val PARTICIPANT_AWAIT_TIMEOUT_MS = 1_000L
    }
}
