package com.anytypeio.anytype.presentation.membership.provider

import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipStatusModel.*
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.payments.GetMembershipStatus
import com.anytypeio.anytype.domain.payments.GetMembershipTiers
import com.anytypeio.anytype.domain.workspace.MembershipChannel
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

interface MembershipProvider {

    val status: StateFlow<MembershipStatus>

    class Default(
        private val dispatchers: AppCoroutineDispatchers,
        private val scope: CoroutineScope,
        private val membershipChannel: MembershipChannel,
        private val awaitAccountStartManager: AwaitAccountStartManager,
        private val getMembershipStatus: GetMembershipStatus,
        private val getTiers: GetMembershipTiers,
        private val localeProvider: LocaleProvider
    ) : MembershipProvider {

        private val _status = MutableStateFlow<MembershipStatus>(MembershipStatus.Unknown)
        override val status: StateFlow<MembershipStatus> = _status.asStateFlow()

        init {
            startMembershipObservation()
        }

        private fun startMembershipObservation() {
            scope.launch(dispatchers.io) {
                observeMembershipEvents()
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private suspend fun observeMembershipEvents() {
            awaitAccountStartManager.isStarted()
                .flatMapLatest { isStarted ->
                    if (isStarted) {
                        proceedWithGettingMembership()
                        membershipChannel.observe()
                    } else {
                        emptyFlow()
                    }
                }
                .collect { events ->
                    events.forEach { event ->
                        if (event is Membership.Event.Update) {
                            Timber.d("Membership event received: $event")
                            proceedWithGettingTiers(event.membership)
                        }
                    }
                }
        }

        private suspend fun proceedWithGettingMembership() {
            val statusParams = GetMembershipStatus.Params(noCache = false)
            Timber.d("Getting membership status with params: $statusParams")
            getMembershipStatus.async(params = statusParams).fold(
                onSuccess = { membership -> proceedWithGettingTiers(membership) },
                onFailure = Timber::e
            )
        }

        private suspend fun proceedWithGettingTiers(membership: Membership?) {
            val tiersParams = GetMembershipTiers.Params(
                noCache = false,
                locale = localeProvider.language() ?: DEFAULT_LOCALE
            )
            Timber.d("Getting membership tiers with params: $tiersParams")
            getTiers.async(params = tiersParams).fold(
                onSuccess = { tiers -> updateMembershipStatus(membership, tiers) },
                onFailure = Timber::e
            )
        }

        private fun updateMembershipStatus(
            membership: Membership?,
            tiers: List<MembershipTierData>
        ) {
            _status.value = mapMembershipToStatus(membership, tiers).also {
                Timber.d("Membership status updated: $it")
            }
        }

        private fun mapMembershipToStatus(
            membership: Membership?,
            tiers: List<MembershipTierData>
        ): MembershipStatus {
            return when (membership?.membershipStatusModel) {
                STATUS_PENDING -> MembershipStatus.Pending
                STATUS_PENDING_FINALIZATION -> MembershipStatus.Finalization
                STATUS_ACTIVE -> createActiveMembershipStatus(membership, tiers)
                STATUS_UNKNOWN, null -> {
                    Timber.e("Invalid or unknown membership status")
                    MembershipStatus.Unknown
                }
                else -> MembershipStatus.Unknown
            }
        }

        private fun createActiveMembershipStatus(
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