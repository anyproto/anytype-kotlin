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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_discussions.ui.DiscussionScreenWrapper
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.home.Command
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import com.anytypeio.anytype.ui.library.LibraryFragment
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.objects.creation.SelectObjectTypeFragment
import com.anytypeio.anytype.ui.payments.MembershipFragment
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

    @Inject
    lateinit var featureToggles: FeatureToggles

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
                    onSearchClicked = vm::onSearchIconClicked,
                    onLibraryClicked = {
                        vm.onLibraryClicked()
                    },
                    onCreateNewObjectClicked = throttledClick(
                        onClick = { vm.onCreateNewObjectClicked() }
                    ),
                    onCreateNewObjectLongClicked = throttledClick(
                        onClick = { vm.onCreateNewObjectLongClicked() }
                    ),
                    onBackClicked = throttledClick(
                        onClick = vm::onBackClicked
                    ),
                    onSpaceWidgetClicked = throttledClick(
                        onClick = vm::onSpaceSettingsClicked
                    ),
                    onBundledWidgetClicked = vm::onBundledWidgetClicked,
                    onMove = vm::onMove,
                    onObjectCheckboxClicked = vm::onObjectCheckboxClicked,
                    onSpaceShareIconClicked = vm::onSpaceShareIconClicked,
                    onSeeAllObjectsClicked = vm::onSeeAllObjectsClicked,
                    onCreateObjectInsideWidget = vm::onCreateObjectInsideWidget,
                    onCreateDataViewObject = vm::onCreateDataViewObject
                )

                if (featureToggles.enableDiscussionDemo) {
                    DiscussionScreenWrapper()
                }
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
        val deepLinkFromFragmentArgs = deepLink
        if (deepLinkFromFragmentArgs != null) {
            Timber.d("Deeplink  from fragment args")
            vm.onResume(DefaultDeepLinkResolver.resolve(deepLinkFromFragmentArgs))
            arguments?.putString(DEEP_LINK_KEY, null)
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
            is Command.Deeplink.Invite -> {
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
            is Command.Deeplink.MembershipScreen -> {
                findNavController().navigate(
                    R.id.paymentsScreen,
                    MembershipFragment.args(command.tierId),
                    NavOptions.Builder().setLaunchSingleTop(true).build()
                )
            }
            is Command.Deeplink.DeepLinkToObjectNotWorking -> {
                toast(
                    getString(R.string.multiplayer_deeplink_to_your_object_error)
                )
            }
            is Command.ShareSpace -> {
                findNavController().navigate(
                    R.id.shareSpaceScreen,
                    args = ShareSpaceFragment.args(command.space)
                )
            }
            is Command.CreateSourceForNewWidget -> {
                val dialog = SelectObjectTypeFragment.new(
                    flow = SelectObjectTypeFragment.FLOW_CREATE_OBJECT,
                    space = command.space.id
                ).apply {
                    onTypeSelected = { type ->
                        vm.onNewWidgetSourceTypeSelected(
                            type = type,
                            space = command.space,
                            widgets = command.widgets
                        )
                    }
                }
                dialog.show(childFragmentManager, null)
            }
            is Command.CreateObjectForWidget -> {
                val dialog = SelectObjectTypeFragment.new(
                    flow = SelectObjectTypeFragment.FLOW_CREATE_OBJECT,
                    space = command.space.id
                ).apply {
                    onTypeSelected = { type ->
                        vm.onCreateObjectForWidget(
                            type = type,
                            widget = command.widget,
                            source = command.source
                        )
                    }
                }
                dialog.show(childFragmentManager, null)
            }
            is Command.OpenSpaceSettings -> {
                runCatching {
                    findNavController().navigate(
                        R.id.action_open_space_settings,
                        SpaceSettingsFragment.args(space = command.spaceId)
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
            is Command.OpenGlobalSearchScreen -> {
                runCatching {
                    navigation().openGlobalSearch(
                        space = command.space
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening global search screen")
                }
            }
            is Command.OpenVault -> {
                runCatching {
                    findNavController().navigate(R.id.action_open_vault)
                }.onFailure {
                    Timber.e(it, "Error while opening vault from home screen")
                }
            }
        }
    }

    private fun proceed(destination: Navigation) {
        Timber.d("New destination: $destination")
        when (destination) {
            is Navigation.OpenObject -> runCatching {
                navigation().openDocument(
                    target = destination.ctx,
                    space = destination.space
                )
            }
            is Navigation.OpenSet -> runCatching {
                navigation().openObjectSet(
                    target = destination.ctx,
                    space = destination.space,
                    view = destination.view
                )
            }
            is Navigation.ExpandWidget -> runCatching {
                navigation().launchCollections(
                    subscription = destination.subscription,
                    space = destination.space
                )
            }
            is Navigation.OpenLibrary -> runCatching {
                findNavController().navigate(
                    R.id.libraryFragment,
                    args = LibraryFragment.args(destination.space)
                )
            }.onFailure { e ->
                Timber.e(e, "Error while opening space library from widgets")
            }
            is Navigation.OpenAllContent -> {
                runCatching {
                    navigation().openAllContent(space = destination.space)
                }.onFailure { e ->
                    Timber.e(e, "Error while opening all content from widgets")
                }
            }
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

    override fun onApplyWindowRootInsets(view: View) {
        if (!featureToggles.enableDiscussionDemo) {
            super.onApplyWindowRootInsets(view)
        }
    }

    companion object {
        const val SHOW_MNEMONIC_KEY = "arg.home-screen.show-mnemonic"
        const val DEEP_LINK_KEY = "arg.home-screen.deep-link"
        fun args(deeplink: String?) : Bundle = bundleOf(
            DEEP_LINK_KEY to deeplink
        )
    }
}