package com.anytypeio.anytype.presentation.membership.provider

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipStatusModel.STATUS_ACTIVE
import com.anytypeio.anytype.core_models.membership.MembershipStatusModel.STATUS_PENDING
import com.anytypeio.anytype.core_models.membership.MembershipStatusModel.STATUS_PENDING_FINALIZATION
import com.anytypeio.anytype.core_models.membership.MembershipStatusModel.STATUS_UNKNOWN
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.workspace.MembershipChannel
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan
import timber.log.Timber

interface MembershipProvider {

    fun status(): Flow<MembershipStatus>

    class Default(
        private val dispatchers: AppCoroutineDispatchers,
        private val membershipChannel: MembershipChannel,
        private val awaitAccountStartManager: AwaitAccountStartManager,
        private val localeProvider: LocaleProvider,
        private val repo: BlockRepository
    ) : MembershipProvider {

        override fun status(): Flow<MembershipStatus> {
            return awaitAccountStartManager.isStarted().flatMapLatest { isStarted ->
                if (isStarted) {
                    val membership = proceedWithGettingMembership()
                    val tiers = proceedWithGettingTiers()
                    buildStatusFlow(
                        initial = toMembershipStatus(membership, tiers)
                    )
                } else {
                    emptyFlow()
                }
            }.catch { e -> Timber.e(e) }
                .flowOn(dispatchers.io)
        }

        private fun buildStatusFlow(
            initial: MembershipStatus
        ): Flow<MembershipStatus> {
            val statusFlow = membershipChannel.observe().scan(initial) { status, events ->
                events.fold(status) { acc, event ->
                    when (event) {
                        is Membership.Event.Update -> {
                            Timber.d("Membership event received: $event")
                            val membership = event.membership
                            val tiers = proceedWithGettingTiers()
                            toMembershipStatus(membership, tiers)
                        }
                        else -> acc
                    }
                }
            }
            return statusFlow
        }

        private suspend fun proceedWithGettingMembership(): Membership? {
            val command = Command.Membership.GetStatus(
                noCache = false
            )
            return repo.membershipStatus(command)
        }

        private suspend fun proceedWithGettingTiers(): List<MembershipTierData> {
            val tiersParams = Command.Membership.GetTiers(
                noCache = false,
                locale = localeProvider.language() ?: DEFAULT_LOCALE
            )
            return repo.membershipGetTiers(tiersParams)
        }

        private fun toMembershipStatus(
            membership: Membership?,
            tiers: List<MembershipTierData>
        ): MembershipStatus {
            return when (membership?.membershipStatusModel) {
                STATUS_PENDING -> MembershipStatus.Pending
                STATUS_PENDING_FINALIZATION -> MembershipStatus.Finalization
                STATUS_ACTIVE -> toActiveMembershipStatus(membership, tiers)
                STATUS_UNKNOWN, null -> {
                    Timber.e("Invalid or unknown membership status")
                    MembershipStatus.Unknown
                }

                else -> MembershipStatus.Unknown
            }
        }

        private fun toActiveMembershipStatus(
            membership: Membership,
            tiers: List<MembershipTierData>
        ): MembershipStatus {
            val tier = tiers.firstOrNull { it.id == membership.tier }
            return if (tier != null) {
                MembershipStatus.Active(
                    tier = tier,
                    status = membership.membershipStatusModel,
                    dateEnds = membership.dateEnds,
                    paymentMethod = membership.paymentMethod,
                    anyName = membership.requestedAnyName
                )
            } else {
                Timber.e("Membership tier not found: ${membership.tier}")
                MembershipStatus.Unknown
            }
        }

        companion object {
            const val DEFAULT_LOCALE = "en"
        }
    }
}