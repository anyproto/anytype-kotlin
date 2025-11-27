package com.anytypeio.anytype.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.profile.ParticipantScreen
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.profile.ParticipantViewModel
import javax.inject.Inject
import timber.log.Timber

class ParticipantFragment: BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ParticipantViewModel.Factory

    private val vm by viewModels<ParticipantViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            ParticipantScreen(
                uiState = vm.uiState.collectAsStateWithLifecycle().value,
                onEvent = vm::onEvent
            )
            LaunchedEffect(Unit) {
                vm.commands.collect { command ->
                    proceedWithCommand(command)
                }
            }
        }
    }

    private fun proceedWithCommand(command: ParticipantViewModel.Command) {
        when (command) {
            ParticipantViewModel.Command.Dismiss -> {
                findNavController().popBackStack()
            }
            is ParticipantViewModel.Command.Toast.Error -> {
                toast(command.msg)
            }
            ParticipantViewModel.Command.OpenSettingsProfile -> {
                runCatching {
                    findNavController().navigate(
                        R.id.profileSettingsScreen,
                        null
                    )
                }.onFailure {
                    Timber.w("Error while opening participant screen")
                }
            }

            is ParticipantViewModel.Command.SwitchToVault -> {
                runCatching {
                    findNavController().popBackStack(R.id.vaultScreen, false)
                }.onFailure {
                    Timber.e(it, "Error while opening space")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun injectDependencies() {
        val vmParams = ParticipantViewModel.VmParams(
            objectId = argString(ARG_OBJECT_ID),
            space = SpaceId(argString(ARG_SPACE))
        )
        componentManager().participantScreenComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().participantScreenComponent.release()
    }

    companion object ProfileScreenNavigation {
        const val ARG_SPACE = "arg.participant.screen.space"
        const val ARG_OBJECT_ID = "arg.participant.screen.object_id"

        fun args(space: Id, objectId: Id) = bundleOf(
            ARG_SPACE to space,
            ARG_OBJECT_ID to objectId
        )
    }
}