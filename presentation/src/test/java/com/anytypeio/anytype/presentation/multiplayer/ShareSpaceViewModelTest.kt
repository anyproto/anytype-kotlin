package com.anytypeio.anytype.presentation.multiplayer

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ApproveLeaveSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceMemberPermissions
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.MakeSpaceShareable
import com.anytypeio.anytype.domain.multiplayer.RemoveSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.RevokeSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.StopSharingSpace
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.presentation.home.UserPermissionProviderStub
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mock

class ShareSpaceViewModelTest {

    @Mock
    lateinit var params: ShareSpaceViewModel.Params
    @Mock
    lateinit var makeSpaceShareable: MakeSpaceShareable

    @Mock
    lateinit var getSpaceInviteLink: GetSpaceInviteLink

    @Mock
    lateinit var generateSpaceInviteLink: GenerateSpaceInviteLink

    @Mock
    lateinit var revokeSpaceInviteLink: RevokeSpaceInviteLink

    @Mock
    lateinit var removeSpaceMembers: RemoveSpaceMembers

    @Mock
    lateinit var approveLeaveSpaceRequest: ApproveLeaveSpaceRequest

    @Mock
    lateinit var changeSpaceMemberPermissions: ChangeSpaceMemberPermissions

    @Mock
    lateinit var stopSharingSpace: StopSharingSpace

    @Mock
    lateinit var container: StorelessSubscriptionContainer

    @Mock
    lateinit var getAccount: GetAccount

    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var analytics: Analytics

//    private val makeSpaceShareable: MakeSpaceShareable,
//    private val getSpaceInviteLink: GetSpaceInviteLink,
//    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
//    private val revokeSpaceInviteLink: RevokeSpaceInviteLink,
//    private val removeSpaceMembers: RemoveSpaceMembers,
//    private val approveLeaveSpaceRequest: ApproveLeaveSpaceRequest,
//    private val changeSpaceMemberPermissions: ChangeSpaceMemberPermissions,
//    private val stopSharingSpace: StopSharingSpace,
//    private val container: StorelessSubscriptionContainer,
//    private val permissions: UserPermissionProvider,
//    private val getAccount: GetAccount,
//    private val urlBuilder: UrlBuilder,
//    private val analytics: Analytics

    var permissions: UserPermissionProvider = UserPermissionProviderStub()

    fun stubObservePermissions(
        permission: SpaceMemberPermissions = SpaceMemberPermissions.OWNER
    ) {
        (permissions as UserPermissionProviderStub).stubObserve(
            SpaceId(""), permission
        )
    }

    @Test
    fun `test 1`() {

    }

    private fun buildViewModel() : ShareSpaceViewModel {
        return ShareSpaceViewModel(
            params = params,
            makeSpaceShareable = makeSpaceShareable,
            getSpaceInviteLink = getSpaceInviteLink,
            generateSpaceInviteLink = generateSpaceInviteLink,
            revokeSpaceInviteLink = revokeSpaceInviteLink,
            removeSpaceMembers = removeSpaceMembers,
            approveLeaveSpaceRequest = approveLeaveSpaceRequest,
            changeSpaceMemberPermissions = changeSpaceMemberPermissions,
            stopSharingSpace = stopSharingSpace,
            container = container,
            permissions = permissions,
            getAccount = getAccount,
            urlBuilder = urlBuilder,
            analytics = analytics
        )
    }
}