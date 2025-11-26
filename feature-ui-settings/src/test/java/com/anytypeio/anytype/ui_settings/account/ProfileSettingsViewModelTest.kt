package com.anytypeio.anytype.ui_settings.account

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.icon.RemoveObjectIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.icon.SetImageIcon
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.ui_settings.util.DefaultCoroutineTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class ProfileSettingsViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock private lateinit var analytics: Analytics
    @Mock private lateinit var container: StorelessSubscriptionContainer
    @Mock private lateinit var setObjectDetails: SetObjectDetails
    @Mock private lateinit var configStorage: ConfigStorage
    @Mock private lateinit var urlBuilder: UrlBuilder
    @Mock private lateinit var setImageIcon: SetDocumentImageIcon
    @Mock private lateinit var membershipProvider: MembershipProvider
    @Mock private lateinit var getNetworkMode: GetNetworkMode
    @Mock private lateinit var profileContainer: ProfileSubscriptionManager
    @Mock private lateinit var removeObjectIcon: RemoveObjectIcon
    @Mock private lateinit var notificationPermissionManager: NotificationPermissionManager

    private val defaultNetworkMode = NetworkMode.CUSTOM
    private val defaultMembershipStatus = MembershipStatus(
        activeTier = TierId(1),
        status = Membership.Status.STATUS_ACTIVE,
        paymentMethod = MembershipPaymentMethod.METHOD_INAPP_GOOGLE,
        anyName = "testName",
        tiers = emptyList(),
        dateEnds = 123,
        formattedDateEnds = "123"
    )


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        setupDefaultMocks()
    }


    private fun createViewModel(): ProfileSettingsViewModel {
        return ProfileSettingsViewModel(
            analytics = analytics,
            container = container,
            setObjectDetails = setObjectDetails,
            configStorage = configStorage,
            urlBuilder = urlBuilder,
            setImageIcon = setImageIcon,
            membershipProvider = membershipProvider,
            getNetworkMode = getNetworkMode,
            profileContainer = profileContainer,
            removeObjectIcon = removeObjectIcon,
            notificationPermissionManager = notificationPermissionManager
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should update membership status on init based on network mode`() = runTest {

        val vm = createViewModel()
        advanceUntilIdle()

        verifyBlocking(getNetworkMode, times(1)) { async(Unit) }

        assertEquals(
            expected = ShowMembership(true),
            actual = vm.showMembershipState.value
        )

        assertEquals(
            expected = defaultMembershipStatus,
            actual = vm.membershipStatusState.value
        )

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `object details get changed when name changes`() = runTest {
        val name = "test name"
        val stubConfig = StubConfig()
        val setObjectDetailsParams = SetObjectDetails.Params(
            ctx = stubConfig.profile,
            details = mapOf(Relations.NAME to name)
        )
        val payload = Payload(context = "Test ctx", events = emptyList())
        whenever(configStorage.getOrNull()).thenReturn(stubConfig)
        whenever(setObjectDetails.execute(any())).thenReturn(Resultat.Success(payload))

        val vm = createViewModel()

        vm.onNameChange(name)

        advanceUntilIdle()

        verifyBlocking(setObjectDetails, times(1)) { execute(setObjectDetailsParams) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `object details does not get changed when profile is null`() = runTest {
        val name = "test name"
        val stubConfig = StubConfig()
        val setObjectDetailsParams = SetObjectDetails.Params(
            ctx = stubConfig.profile,
            details = mapOf(Relations.NAME to name)
        )
        whenever(configStorage.getOrNull()).thenReturn(null)

        val vm = createViewModel()

        vm.onNameChange(name)

        advanceUntilIdle()

        verifyBlocking(setObjectDetails, times(0)) { execute(setObjectDetailsParams) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `image icon gets set when image picked from device`() = runTest {
        val path = "test path"
        val stubConfig = StubConfig()
        val params = SetImageIcon.Params(
            target = stubConfig.profile,
            path = path,
            spaceId = SpaceId(stubConfig.techSpace)
        )
        val testId: Hash = "uploaded-file-id"

        whenever(configStorage.getOrNull()).thenReturn(stubConfig)
        whenever(setImageIcon(params)).thenReturn(
            Either.Right(
                Pair(
                    Payload(context = "Test ctx", events = emptyList()),
                    testId
                )
            )
        )

        val vm = createViewModel()
        vm.onPickedImageFromDevice(path)

        advanceUntilIdle()

        verifyBlocking(setImageIcon, times(1)) { invoke(params) }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `image icon gets does not set when config is null`() = runTest {
        val path = "test path"
        whenever(configStorage.getOrNull()).thenReturn(null)

        val vm = createViewModel()
        vm.onPickedImageFromDevice(path)

        advanceUntilIdle()

        verifyBlocking(setImageIcon, never()) { invoke(any()) }

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `remove object called when profile image is cleared`() = runTest {
        val stubConfig = StubConfig()
        val removeObjectIconParams = RemoveObjectIcon.Params(
            objectId = stubConfig.profile
        )

        whenever(configStorage.getOrNull()).thenReturn(stubConfig)
        whenever(removeObjectIcon.async(removeObjectIconParams)).thenReturn(Resultat.Success(Unit))

        val vm = createViewModel()
        vm.onClearProfileImage()

        advanceUntilIdle()

        verifyBlocking(removeObjectIcon, times(1)) { async(removeObjectIconParams) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `remove object not called when profile image is cleared as config is null`() = runTest {
        whenever(configStorage.getOrNull()).thenReturn(null)

        val vm = createViewModel()
        vm.onClearProfileImage()

        advanceUntilIdle()

        verifyBlocking(removeObjectIcon, never()) { async(any()) }
    }

    @Test
    fun `refresh permission state`() {
        val vm = createViewModel()
        vm.refreshPermissionState()
        verify(notificationPermissionManager).refreshPermissionState()
    }


    private fun setupDefaultMocks() {

        runBlocking {
            whenever(getNetworkMode.async(any())).thenReturn(Resultat.Success(NetworkModeConfig(defaultNetworkMode)))
        }
        whenever(membershipProvider.status()).thenReturn(
            flowOf(defaultMembershipStatus)
        )
    }

}