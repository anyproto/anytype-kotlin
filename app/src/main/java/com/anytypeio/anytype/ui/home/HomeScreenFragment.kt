package com.anytypeio.anytype.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.home.Command
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.objects.creation.CreateObjectOfTypeFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.widgets.SelectWidgetSourceFragment
import com.anytypeio.anytype.ui.widgets.SelectWidgetTypeFragment
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeScreenFragment : BaseComposeFragment() {

    private val deepLink: String? get() = argOrNull(DEEP_LINK_KEY)

    private var isMnemonicReminderDialogNeeded: Boolean
        get() = argOrNull<Boolean>(SHOW_MNEMONIC_KEY) ?: false
        set(value) { arguments?.putBoolean(SHOW_MNEMONIC_KEY, value) }

    @Inject
    lateinit var factory: HomeScreenViewModel.Factory

    private val vm by viewModels<HomeScreenViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography,
                shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp)),
                colors = MaterialTheme.colors.copy(
                    surface = colorResource(id = R.color.background_secondary)
                )
            ) {
                HomeScreen(
                    profileIcon = vm.icon.collectAsState().value,
                    widgets = vm.views.collectAsState().value,
                    mode = vm.mode.collectAsState().value,
                    onExpand = { path -> vm.onExpand(path) },
                    onCreateWidget = vm::onCreateWidgetClicked,
                    onEditWidgets = vm::onEditWidgets,
                    onExitEditMode = vm::onExitEditMode,
                    onWidgetMenuAction = { widget: Id, action: DropDownMenuAction ->
                        vm.onDropDownMenuAction(widget, action)
                    },
                    onWidgetObjectClicked = vm::onWidgetObjectClicked,
                    onWidgetSourceClicked = vm::onWidgetSourceClicked,
                    onChangeWidgetView = vm::onChangeCurrentWidgetView,
                    onToggleExpandedWidgetState = vm::onToggleCollapsedWidgetState,
                    onSearchClicked = {
                        runCatching { navigation().openPageSearch() }
                    },
                    onLibraryClicked = {
                        runCatching { navigation().openLibrary() }
                    },
                    onCreateNewObjectClicked = throttledClick(
                        onClick = { vm.onCreateNewObjectClicked() }
                    ),
                    onCreateNewObjectLongClicked = throttledClick(
                        onClick = {
                            val dialog = CreateObjectOfTypeFragment().apply {
                                onTypeSelected = {
                                    vm.onCreateNewObjectClicked(it)
                                    dismiss()
                                }
                            }
                            dialog.show(childFragmentManager, "TEST")
                        }
                    ),
                    onProfileClicked = throttledClick(
                        onClick = {
                            runCatching {
                                findNavController().navigate(R.id.action_open_spaces)
                            }
                        }
                    ),
                    onSpaceWidgetClicked = throttledClick(
                        onClick = {
                            runCatching {
                                findNavController().navigate(R.id.action_open_space_settings)
                            }
                        }
                    ),
                    onOpenSpacesClicked = throttledClick(
                        onClick = {
                            runCatching {
                                findNavController().navigate(R.id.action_open_spaces)
                            }
                        }
                    ),
                    onBundledWidgetClicked = vm::onBundledWidgetClicked,
                    onMove = vm::onMove,
                    onObjectCheckboxClicked = vm::onObjectCheckboxClicked
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(deepLink?.let { DefaultDeepLinkResolver.resolve(it) })
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.commands.collect { command -> proceed(command) } }
                launch { vm.navigation.collect { command -> proceed(command) } }
                launch { vm.toasts.collect { toast(it) } }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isMnemonicReminderDialogNeeded) showMnemonicReminderAlert()
    }

    private fun proceed(command: Command) {
        when (command) {
            is Command.ChangeWidgetSource -> {
                findNavController().navigate(
                    R.id.selectWidgetSourceScreen,
                    args = SelectWidgetSourceFragment.args(
                        ctx = command.ctx,
                        widget = command.widget,
                        source = command.source,
                        type = command.type,
                        isInEditMode = command.isInEditMode
                    )
                )
            }
            is Command.SelectWidgetSource -> {
                findNavController().navigate(
                    R.id.selectWidgetSourceScreen,
                    args = SelectWidgetSourceFragment.args(
                        target = command.target,
                        isInEditMode = command.isInEditMode
                    )
                )
            }
            is Command.ChangeWidgetType -> {
                findNavController().navigate(
                    R.id.selectWidgetTypeScreen,
                    args = SelectWidgetTypeFragment.args(
                        ctx = command.ctx,
                        widget = command.widget,
                        source = command.source,
                        type = command.type,
                        layout = command.layout,
                        isInEditMode = command.isInEditMode
                    )
                )
            }
            is Command.SelectWidgetType -> {
                findNavController().navigate(
                    R.id.selectWidgetTypeScreen,
                    args = SelectWidgetTypeFragment.args(
                        ctx = command.ctx,
                        source = command.source,
                        layout = command.layout,
                        target = command.target,
                        isInEditMode = command.isInEditMode
                    )
                )
            }
            is Command.Deeplink.CannotImportExperience -> {
                findNavController().navigate(R.id.alertImportExperienceUnsupported)
            }
        }
    }

    private fun proceed(destination: Navigation) {
        Timber.d("New destination: $destination")
        when (destination) {
            is Navigation.OpenObject -> navigation().openDocument(destination.ctx)
            is Navigation.OpenSet -> navigation().openObjectSet(destination.ctx)
            is Navigation.ExpandWidget -> navigation().launchCollections(destination.subscription)
        }
    }

    private fun showMnemonicReminderAlert() {
        isMnemonicReminderDialogNeeded = false
        findNavController().navigate(R.id.dashboardKeychainDialog)
    }

    override fun injectDependencies() {
        componentManager().homeScreenComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().homeScreenComponent.release()
    }

    companion object {
        const val SHOW_MNEMONIC_KEY = "arg.home-screen.show-mnemonic"
        const val DEEP_LINK_KEY = "arg.home-screen.deep-link"
        fun args(deeplink: String?) : Bundle = bundleOf(
            DEEP_LINK_KEY to deeplink
        )
    }
}