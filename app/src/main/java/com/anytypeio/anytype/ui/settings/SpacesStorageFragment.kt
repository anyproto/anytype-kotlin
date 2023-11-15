package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.intents.SystemAction
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.SpacesStorageFactory
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel
import com.anytypeio.anytype.ui_settings.space.SpaceStorageScreen
import javax.inject.Inject
import kotlinx.coroutines.launch

class SpacesStorageFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SpacesStorageFactory

    private val vm by viewModels<SpacesStorageViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeDialogView(
            context = requireContext(),
            dialog = requireDialog()
        ).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    SpaceStorageScreen(
                        data = vm.viewState.collectAsStateWithLifecycle().value,
                        onManageFilesClicked = { throttle { vm.event(SpacesStorageViewModel.Event.OnManageFilesClicked) } },
                        onGetMoreSpaceClicked = { throttle { vm.event(SpacesStorageViewModel.Event.OnGetMoreSpaceClicked) } },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(PADDING_TOP)
        collectCommands()
    }

    override fun onStart() {
        super.onStart()
        proceed(vm.toasts) { toast(it) }
        vm.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    private fun collectCommands() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.commands.collect { command -> processCommands(command) }
            }
        }
    }

    private fun processCommands(command: SpacesStorageViewModel.Command) {
        when (command) {
            is SpacesStorageViewModel.Command.OpenRemoteFilesManageScreen -> {
                openRemoteStorageScreen(
                    subscription = command.subscription
                )
            }
            is SpacesStorageViewModel.Command.SendGetMoreSpaceEmail -> {
                proceedWithAction(
                    SystemAction.MailTo(
                        generateSupportMail(
                            account = command.account,
                            limit = command.limit,
                            name = command.name
                        )
                    )
                )
            }
        }
    }

    private fun openRemoteStorageScreen(subscription: String) {
        findNavController().safeNavigate(
            R.id.spacesStorageScreen,
            R.id.remoteStorageFragment,
            bundleOf(RemoteFilesManageFragment.SUBSCRIPTION_KEY to subscription)
        )
    }

    private fun generateSupportMail(
        account: Id,
        name: String,
        limit: String,

        ): String {
        val bodyString = resources.getString(R.string.mail_more_space_body, limit, account, name)
        return "storage@anytype.io" +
                "?subject=Get%20more%20storage,%20account%20$account" +
                "&body=$bodyString"
    }

    override fun injectDependencies() {
        componentManager().spacesStorageComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spacesStorageComponent.release()
    }
}

private const val PADDING_TOP = 54
