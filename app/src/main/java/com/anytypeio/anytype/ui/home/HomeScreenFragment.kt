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
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import com.anytypeio.anytype.ui.main.MainActivity
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.objects.creation.SelectObjectTypeFragment
import com.anytypeio.anytype.ui.settings.space.SpaceSettingsFragment
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
                        onClick = { vm.onCreateNewObjectLongClicked() }
                    ),
                    onProfileClicked = throttledClick(
                        onClick = {
                            runCatching {
                                findNavController().navigate(R.id.action_open_spaces)
                            }
                        }
                    ),
                    onSpaceWidgetClicked = throttledClick(
                        onClick = vm::onSpaceSettingsClicked
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
                    onObjectCheckboxClicked = vm::onObjectCheckboxClicked,
                    onSpaceShareIconClicked = vm::onSpaceShareIconClicked
                )
            }
        }
    }

    override fun onStart() {
        Timber.d("onStart")
        super.onStart()
        vm.onStart()
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
        if (isMnemonicReminderDialogNeeded)
            showMnemonicReminderAlert()
        proceedWithDeepLinks()
    }

    private fun proceedWithDeepLinks() {
        val deepLinkFromFragment = deepLink
        val deepLinkFromActivity = (requireActivity() as? MainActivity)?.deepLink

        when {
            deepLinkFromFragment != null -> {
                vm.onResume(DefaultDeepLinkResolver.resolve(deepLinkFromFragment))
                arguments?.putString(DEEP_LINK_KEY, null)
            }
            deepLinkFromActivity != null -> {
                vm.onResume(DefaultDeepLinkResolver.resolve(deepLinkFromActivity))
                (requireActivity() as? MainActivity)?.deepLink = null
            }
        }
    }

    private fun proceed(command: Command) {
        when (command) {
            is Command.ChangeWidgetSource -> {
                runCatching {
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
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }
            is Command.SelectWidgetSource -> {
                runCatching {
                    findNavController().navigate(
                        R.id.selectWidgetSourceScreen,
                        args = SelectWidgetSourceFragment.args(
                            target = command.target,
                            isInEditMode = command.isInEditMode
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }
            is Command.ChangeWidgetType -> {
                runCatching {
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
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }
            is Command.SelectWidgetType -> {
                runCatching {
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
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }
            is Command.Deeplink.CannotImportExperience -> {
                arguments?.putString(DEEP_LINK_KEY, null)
                findNavController().navigate(R.id.alertImportExperienceUnsupported)
            }
            is Command.Deeplink.Invite -> {
                arguments?.putString(DEEP_LINK_KEY, null)
                findNavController().navigate(
                    R.id.requestJoinSpaceScreen,
                    RequestJoinSpaceFragment.args(link = command.link)
                )
            }
            is Command.Deeplink.GalleryInstallation -> {
                findNavController().navigate(
                    R.id.galleryInstallationScreen,
                    GalleryInstallationFragment.args(
                        deepLinkType = command.deepLinkType,
                        deepLinkSource = command.deepLinkSource
                    )
                )
            }
            is Command.ShareSpace -> {
                findNavController().navigate(
                    R.id.shareSpaceScreen,
                    args = ShareSpaceFragment.args(command.space)
                )
            }
            is Command.OpenSpaceSettings -> {
                runCatching {
                    findNavController().navigate(
                        R.id.action_open_space_settings,
                        SpaceSettingsFragment.args(command.spaceId)
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening space settings")
                }
            }
            is Command.OpenObjectCreateDialog -> {
                val dialog = SelectObjectTypeFragment.new(
                    flow = SelectObjectTypeFragment.FLOW_CREATE_OBJECT,
                    space = command.space.id
                ).apply {
                    onTypeSelected = {
                        vm.onCreateNewObjectClicked(it)
                        dismiss()
                    }
                }
                dialog.show(childFragmentManager, "object-create-dialog")
            }
        }
    }

    private fun proceed(destination: Navigation) {
        Timber.d("New destination: $destination")
        when (destination) {
            is Navigation.OpenObject -> navigation().openDocument(
                target = destination.ctx,
                space = destination.space
            )
            is Navigation.OpenSet -> navigation().openObjectSet(
                target = destination.ctx,
                space = destination.space
            )
            is Navigation.ExpandWidget -> navigation().launchCollections(
                subscription = destination.subscription,
                space = destination.space
            )
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