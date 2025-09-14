package com.anytypeio.anytype.ui.publishtoweb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.publishtoweb.MySitesViewModel
import com.anytypeio.anytype.ui.settings.typography
import timber.log.Timber
import javax.inject.Inject

class MySitesFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: MySitesViewModel.Factory

    private val vm by viewModels<MySitesViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    val clipboardManager = LocalClipboardManager.current
                    
                    MySitesScreen(
                        viewState = vm.viewState.collectAsStateWithLifecycle().value,
                        onViewObjectClicked = vm::onOpenObject,
                        onOpenInBrowserClicked = vm::onOpenInBrowser,
                        onCopyWebLinkClicked = vm::onCopyWebLink,
                        onUnpublishClicked = vm::onUnpublishClicked
                    )
                    LaunchedEffect(Unit) {
                        vm.commands.collect { command ->
                            Timber.d("MySites New command: $command")
                            when (command) {
                                is MySitesViewModel.Command.ShowToast -> {
                                    context.toast(command.message)
                                }
                                is MySitesViewModel.Command.CopyToClipboard -> {
                                    clipboardManager.setText(AnnotatedString(command.text))
                                }
                                is MySitesViewModel.Command.Browse -> {
                                    ActivityCustomTabsHelper.openUrl(
                                        activity = requireActivity(),
                                        url = command.url
                                    )
                                }
                                is MySitesViewModel.Command.OpenObject -> {
                                    // TODO: Open object editor
                                    Timber.d("OpenObject: ${command.objectId} in space ${command.spaceId}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun injectDependencies() {
        componentManager()
            .mySitesComponent
            .get(params = MySitesViewModel.VmParams)
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().mySitesComponent.release()
    }
}