package com.anytypeio.anytype.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.icon.SetImageIcon
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val setObjectDetails: SetObjectDetails,
    private val urlBuilder: UrlBuilder,
    private val setImageIcon: SetDocumentImageIcon,
    private val membershipProvider: MembershipProvider,
    private val profileContainer: ProfileSubscriptionManager,
    private val fieldsParser: FieldParser
) : ViewModel() {

    val membershipStatusState = MutableStateFlow<MembershipStatus?>(null)

    init {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenSettingsAccount
            )
        }
        viewModelScope.launch {
            membershipProvider.status().collect { status ->
                membershipStatusState.value = status
            }
        }
        viewModelScope.launch {
            profileContainer.observe().collect { profile ->
                AccountProfile.Data(
                    name = fieldsParser.getObjectName(profile),
                    icon = profile.profileIcon(urlBuilder)
                )
            }
        }
    }

    fun onNameChange(name: String) {
        Timber.d("onNameChange, name:[$name]")
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.ctx,
                details = mapOf(Relations.NAME to name)
            )
            setObjectDetails.async(params).fold(
                onFailure = {
                    Timber.e(it, "Error while updating object details")
                },
                onSuccess = {
                    // do nothing
                }
            )
        }
    }

    fun onPickedImageFromDevice(path: String) {
        viewModelScope.launch {
            setImageIcon(
                SetImageIcon.Params(
                    target = vmParams.ctx,
                    path = path,
                    spaceId = vmParams.space
                )
            ).process(
                failure = {
                    Timber.e("Error while setting image icon")
                },
                success = {
                    // do nothing
                }
            )
        }
    }

    sealed class AccountProfile {
        data object Idle : AccountProfile()
        class Data(
            val name: String,
            val icon: ProfileIconView
        ) : AccountProfile()
    }

    data class VmParams(
        val space: SpaceId,
        val ctx: Id
    )

    class Factory(
        private val vmParams: VmParams,
        private val analytics: Analytics,
        private val setObjectDetails: SetObjectDetails,
        private val urlBuilder: UrlBuilder,
        private val setDocumentImageIcon: SetDocumentImageIcon,
        private val membershipProvider: MembershipProvider,
        private val profileSubscriptionManager: ProfileSubscriptionManager,
        private val fieldsParser: FieldParser
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(
                vmParams = vmParams,
                analytics = analytics,
                setObjectDetails = setObjectDetails,
                urlBuilder = urlBuilder,
                setImageIcon = setDocumentImageIcon,
                membershipProvider = membershipProvider,
                profileContainer = profileSubscriptionManager,
                fieldsParser = fieldsParser
            ) as T
        }
    }
}