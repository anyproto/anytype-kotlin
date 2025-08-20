package com.anytypeio.anytype.ui.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.MultiplayerError
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceJoinRequestScreen
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.multiplayer.SpaceJoinRequestViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class SpaceJoinRequestFragment : BaseBottomSheetComposeFragment() {

    private val space get() = arg<Id>(SPACE_ID_KEY)
    private val member get() = arg<Id>(MEMBER_ID_KEY)
    private val analyticsRoute get() = arg<String>(ANALYTICS_ROUTE_KEY)

    @Inject
    lateinit var factory: SpaceJoinRequestViewModel.Factory

    private val vm by viewModels<SpaceJoinRequestViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    SpaceJoinRequestScreen(
                        state = vm.viewState.collectAsStateWithLifecycle().value,
                        onAddViewerClicked = vm::onJoinAsReaderClicked,
                        onAddEditorClicked = vm::onJoinAsEditorClicked,
                        onRejectClicked = vm::onRejectRequestClicked,
                        onUpgradeClicked = vm::onUpgradeClicked
                    )
                    LaunchedEffect(Unit) {
                        vm.toasts.collect { toast(it) }
                    }
                    LaunchedEffect(Unit) {
                        vm.isDismissed.collect { isDismissed ->
                            if (isDismissed) dismiss()
                        }
                    }
                    LaunchedEffect(Unit) {
                        vm.commands.collect { command ->
                            proceedWithCommand(command)
                        }
                    }
                }
            }
        }
    }

    private fun proceedWithCommand(command: SpaceJoinRequestViewModel.Command) {
        Timber.d("proceedWithCommand: $command")
        when (command) {
            is SpaceJoinRequestViewModel.Command.NavigateToMembership -> {
                findNavController().navigate(R.id.paymentsScreen)
            }
            is SpaceJoinRequestViewModel.Command.NavigateToMembershipUpdate -> {
                findNavController().navigate(R.id.membershipUpdateScreen)
            }
            is SpaceJoinRequestViewModel.Command.ShowGenericMultiplayerError -> {
                when(command.error) {
                    is MultiplayerError.Generic.LimitReached -> {
                        toast(resources.getString(R.string.multiplayer_error_limit_reached))
                    }
                    is MultiplayerError.Generic.NotShareable -> {
                        toast(resources.getString(R.string.multiplayer_error_not_shareable))
                    }
                    is MultiplayerError.Generic.RequestFailed -> {
                        toast(resources.getString(R.string.multiplayer_error_request_failed))
                    }
                    is MultiplayerError.Generic.SpaceIsDeleted -> {
                        toast(resources.getString(R.string.multiplayer_error_space_is_deleted))
                    }
                    is MultiplayerError.Generic.IncorrectPermissions -> {
                        toast(resources.getString(R.string.share_space_error_incorrect_permissions))
                    }
                    is MultiplayerError.Generic.NoSuchSpace -> {
                        toast(resources.getString(R.string.share_space_error_no_such_space))
                    }
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().spaceJoinRequestComponent.get(
            param = SpaceJoinRequestViewModel.VmParams(
                space = SpaceId(space),
                member = member,
                route = analyticsRoute
            ),
            key = space+member
        ).inject(fragment = this)
    }

    override fun releaseDependencies() {
        componentManager().spaceJoinRequestComponent.release(
            id = space+member
        )
    }

    companion object {
        const val SPACE_ID_KEY = "arg.space-join-request.space-id-key"
        const val MEMBER_ID_KEY = "arg.space-join-request.member-id-key"
        const val ANALYTICS_ROUTE_KEY = "arg.space-join-request.analytics-route-key"
        fun args(
            space: SpaceId,
            member: Id,
            analyticsRoute: String
        ): Bundle = bundleOf(
            SPACE_ID_KEY to space.id,
            MEMBER_ID_KEY to member,
            ANALYTICS_ROUTE_KEY to analyticsRoute
        )
    }
}