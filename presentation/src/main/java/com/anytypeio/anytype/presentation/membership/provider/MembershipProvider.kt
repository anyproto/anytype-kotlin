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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

interface MembershipProvider {

    val status: StateFlow<MembershipStatus>

    class Default(
        dispatchers: AppCoroutineDispatchers,
        scope: CoroutineScope,
        private val membershipChannel: MembershipChannel,
        private val awaitAccountStartManager: AwaitAccountStartManager,
        private val getMembershipStatus: GetMembershipStatus,
        private val getTiers: GetMembershipTiers,
        private val localeProvider: LocaleProvider
    ) : MembershipProvider {

        private val _status = MutableStateFlow<MembershipStatus>(MembershipStatus.Unknown)
        override val status: StateFlow<MembershipStatus> = _status

        init {
            scope.launch(dispatchers.io) {
                observe().collect { events ->
                    if (events.isNotEmpty()) {

                    }
                }
            }
        }

        private suspend fun proceedWithGettingMembership() {
            val statusParams = GetMembershipStatus.Params(
                noCache = false
            )
            getMembershipStatus.async(params = statusParams).fold(
                onSuccess = { membership ->
                    proceedWithGettingTiers(membership)
                },
                onFailure = {
                    Timber.e(it)
                }
            )
        }

        private suspend fun proceedWithGettingTiers(membership: Membership?) {
            val tiersParams = GetMembershipTiers.Params(
                noCache = false,
                locale = localeProvider.language() ?: DEFAULT_LOCALE
            )
            getTiers.async(params = tiersParams).fold(
                onSuccess = { tiers ->
                    mapping(membership, tiers)
                },
                onFailure = {
                    Timber.e(it)
                }
            )
        }

        private fun mapping(
            membership: Membership?,
            tiers: List<MembershipTierData>
        ) {
            val newStatus = when (membership?.membershipStatusModel) {
                STATUS_PENDING -> {
                    MembershipStatus.Pending
                }

                STATUS_PENDING_FINALIZATION -> {
                    MembershipStatus.Finalization
                }

                STATUS_ACTIVE -> {
                    val tier = tiers.firstOrNull { it.id == membership.tier }
                    if (tier == null && membership.tier != 0) {
                        Timber.e("Membership tier ${membership.tier} not found in tiers response")
                        MembershipStatus.Unknown
                    }
                    MembershipStatus.Active(
                        tier = tier,
                        status = membership.membershipStatusModel,
                        dateEnds = membership.dateEnds,
                        paymentMethod = membership.paymentMethod,
                        anyName = membership.requestedAnyName
                    )
                }

                STATUS_UNKNOWN -> {
                    Timber.d("Membership status is unknown")
                    MembershipStatus.Unknown
                }

                null -> {
                    Timber.e("Membership status is null")
                    MembershipStatus.Unknown
                }
            }
            _status.value = newStatus
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun observe(): Flow<List<Membership.Event>> {
            return awaitAccountStartManager.isStarted().flatMapLatest { isStarted ->
                if (isStarted) {
                    proceedWithGettingMembership()
                    membershipChannel.observe()
                } else {
                    emptyFlow()
                }
            }
        }
    }

    companion object {
        const val DEFAULT_LOCALE = "en"
    }
}