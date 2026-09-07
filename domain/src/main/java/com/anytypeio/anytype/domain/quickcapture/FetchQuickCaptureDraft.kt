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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads a quick-capture draft from the object store, scoped to this account.
 *
 * A draft is unique per space *per participant*: every member of a shared space keeps their
 * own, and nothing in quick capture may surface or act on someone else's.
 *
 * That check is applied to the RESULT, and vetoes only when it is certain — it is
 * deliberately not a filter in the query. As a filter it excluded real drafts: an object
 * whose `creator` is unset, or whose owning participant this client cannot resolve, simply
 * never came back, and "no draft found" is indistinguishable from "this space has no draft".
 * A space holding text then looked empty, and the draft in it was orphaned by creating a
 * second one beside it. Fetching by id and refusing only a *provably* foreign creator fails
 * the safe way: our own draft is always found, another member's is still refused.
 *
 * The participant id differs per space for the same account, which is why it is resolved
 * against [SpaceId] rather than taken as a single account-wide value.
 */
class FetchQuickCaptureDraft @Inject constructor(
    private val repo: BlockRepository,
    private val participants: ParticipantSubscriptionContainer,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<FetchQuickCaptureDraft.Params, ObjectWrapper.Basic?>(dispatchers.io) {

    override suspend fun doWork(params: Params): ObjectWrapper.Basic? {
        val result = repo.searchObjects(
            space = params.space,
            filters = listOf(
                DVFilter(
                    relation = Relations.ID,
                    value = params.draft,
                    condition = DVFilterCondition.EQUAL
                )
            ),
            limit = 1,
            keys = params.keys
        )
        val obj = result.firstOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?.let { ObjectWrapper.Basic(it) }
            ?: return null

        // Veto only on certainty: both sides known and different. An absent creator, or a
        // participant we cannot resolve, must never hide our own draft.
        val creator = obj.getSingleValue<Id>(Relations.CREATOR)
        if (creator != null) {
            val mine = ownParticipantId(params.space)
            if (mine != null && creator != mine) return null
        }
        return obj
    }

    /** This account's participant object in [space] — the value recorded as `creator`. */
    private suspend fun ownParticipantId(space: SpaceId): Id? {
        val identity = runCatching { auth.getCurrentAccountId() }.getOrNull() ?: return null
        fun find(members: List<ObjectWrapper.SpaceMember>): Id? = members.firstOrNull { member ->
            member.identity == identity && member.spaceId == space.id
        }?.id
        find(participants.get())?.let { return it }
        // Not populated yet — wait briefly. firstOrNull, not first: a flow that completes
        // without ever matching makes `first` THROW, which would fail the whole lookup and
        // report the draft as missing — the very outcome this veto must never cause.
        return withTimeoutOrNull(PARTICIPANT_AWAIT_TIMEOUT_MS) {
            participants.observe().firstOrNull { find(it) != null }?.let(::find)
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
