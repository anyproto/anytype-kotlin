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
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceJoinRequestScreen
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceUpgradeScreen
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.multiplayer.SpaceJoinRequestViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

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
                    when(val state = vm.viewState.collectAsStateWithLifecycle().value) {
                        SpaceJoinRequestViewModel.ViewState.Error -> {
                            // TODO Send toast.
                        }
                        SpaceJoinRequestViewModel.ViewState.Init -> {
                            // Draw nothing.
                        }
                        is SpaceJoinRequestViewModel.ViewState.Success -> {
                            SpaceJoinRequestScreen(
                                state = state,
                                onAddViewerClicked = vm::onJoinAsReaderClicked,
                                onAddEditorClicked = vm::onJoinAsEditorClicked,
                                onRejectClicked = vm::onRejectRequestClicked
                            )
                        }
                        is SpaceJoinRequestViewModel.ViewState.Upgrade -> {
                            SpaceUpgradeScreen(
                                state = state,
                                onRejectClicked = vm::onRejectRequestClicked,
                                onUpgradeClicked = vm::onUpgradeClicked
                            )
                        }
                    }
                    LaunchedEffect(Unit) {
                        vm.toasts.collect { toast(it) }
                    }
                    LaunchedEffect(Unit) {
                        vm.isDismissed.collect { isDismissed ->
                            if (isDismissed) dismiss()
                        }
                    }
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().spaceJoinRequestComponent.get(
            SpaceJoinRequestViewModel.Params(
                space = SpaceId(space),
                member = member,
                route = analyticsRoute
            )
        ).inject(fragment = this)
    }

    override fun releaseDependencies() {
        componentManager().spaceJoinRequestComponent.release()
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