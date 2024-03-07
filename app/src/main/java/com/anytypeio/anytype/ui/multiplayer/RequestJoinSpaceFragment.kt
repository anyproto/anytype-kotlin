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
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.multiplayer.JoinSpaceScreen
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.common.ViewState
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class RequestJoinSpaceFragment : BaseBottomSheetComposeFragment() {

    private val link get() = arg<Id>(ARG_LINK_KEY)

    @Inject
    lateinit var factory: RequestJoinSpaceViewModel.Factory

    private val vm by viewModels<RequestJoinSpaceViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    when(val state = vm.state.collectAsStateWithLifecycle().value) {
                        is ViewState.Error -> {
                            toast(state.error).also { dismiss() }
                        }
                        ViewState.Loading -> {
                            // Render nothing.
                        }
                        is ViewState.Success -> {
                            JoinSpaceScreen(
                                onRequestJoinSpaceClicked = vm::onRequestToJoinClicked,
                                spaceName = state.data.spaceName,
                                createdByName = state.data.creatorName
                            )
                        }
                    }
                    LaunchedEffect(Unit) {
                        vm.toasts.collect { toast(it) }
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

    private fun proceedWithCommand(command: RequestJoinSpaceViewModel.Command) {
        when (command) {
            RequestJoinSpaceViewModel.Command.Dismiss -> {
                dismiss()
            }
            RequestJoinSpaceViewModel.Command.Toast.RequestSent -> {
                toast(getString(R.string.multiplayer_request_sent_toast))
            }
        }
    }

    override fun injectDependencies() {
        componentManager().requestToJoinSpaceComponent.get(
            RequestJoinSpaceViewModel.Params(link = link)
        ).inject(fragment = this)
    }

    override fun releaseDependencies() {
        componentManager().requestToJoinSpaceComponent.release()
    }

    companion object {
        fun args(link: String) = bundleOf(ARG_LINK_KEY to link)
        const val ARG_LINK_KEY = "arg.request-to-join-space.link"
    }
}