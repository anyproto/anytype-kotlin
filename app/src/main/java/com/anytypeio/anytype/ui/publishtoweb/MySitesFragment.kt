package com.anytypeio.anytype.ui.publishtoweb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.publishtoweb.MySitesViewModel
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.settings.typography
import timber.log.Timber
import javax.inject.Inject

class MySitesFragment : BaseComposeFragment() {

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
                        onUnpublishClicked = vm::onUnpublishClicked,
                        onBackClicked = {
                            findNavController().popBackStack()
                        }
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
                                    val nav = findNavController()
                                    nav.safeNavigateOrLog(
                                        id = R.id.objectNavigation,
                                        args = EditorFragment.args(
                                            ctx = command.objectId,
                                            space = command.spaceId.id
                                        ),
                                        errorTag = "object from my-sites",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToHomeOrChatThen(
        nav: NavController,
        chat: Id?,
        space: Id,
        then: () -> Unit
    ) {
        if (chat.isNullOrBlank()) {
            nav.safeNavigateOrLog(
                id = R.id.homeScreen,
                args = HomeScreenFragment.args(deeplink = null, space = space)
            )
        } else {
            nav.safeNavigateOrLog(
                id = R.id.chatScreen,
                args = ChatFragment.args(space = space, ctx = chat)
            )
        }
        then()
    }

    private fun NavController.safeNavigateOrLog(
        @IdRes id: Int,
        args: Bundle? = null,
        errorTag: String? = null
    ) {
        val currentId = currentDestination?.id ?: return
        safeNavigate(currentId, id, args, errorTag)
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do not apply
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