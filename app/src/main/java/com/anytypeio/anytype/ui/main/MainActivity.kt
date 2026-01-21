package com.anytypeio.anytype.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import com.anytypeio.anytype.core_ui.features.sharing.SharingModalSheet
import com.anytypeio.anytype.presentation.sharing.IntentToSharedContentConverter
import com.anytypeio.anytype.presentation.sharing.SharingCommand
import com.anytypeio.anytype.presentation.sharing.SharingViewModel
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.NavOptions.Builder
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.app.AnytypeNotificationService
import com.anytypeio.anytype.app.AnytypeNotificationService.Companion.NOTIFICATION_TYPE
import com.anytypeio.anytype.app.DefaultAppActionManager
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.extensions.getGradientDrawableResource
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.parseActionSendMultipleUris
import com.anytypeio.anytype.core_utils.ext.parseActionSendUri
import com.anytypeio.anytype.core_utils.ext.showSnackbar
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.presentation.sharing.SharedContent
import com.google.android.material.snackbar.Snackbar
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.device.AnytypePushService
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.middleware.discovery.MDNSProvider
import com.anytypeio.anytype.navigation.Navigator
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.presentation.main.MainViewModel.Command
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.notifications.NotificationAction
import com.anytypeio.anytype.presentation.notifications.NotificationCommand
import com.anytypeio.anytype.presentation.wallpaper.WallpaperResult
import com.anytypeio.anytype.presentation.wallpaper.WallpaperView
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.editor.CreateObjectFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.multiplayer.SpaceJoinRequestFragment
import com.anytypeio.anytype.ui.notifications.NotificationsFragment
import com.anytypeio.anytype.ui.payments.MembershipFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.vault.SpacesIntroductionScreen
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MainActivity : AppCompatActivity(R.layout.activity_main), AppNavigation.Provider {

    private val vm by viewModels<MainViewModel> { factory }

    private val navigator by lazy { Navigator() }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var factory: MainViewModelFactory

    @Inject
    lateinit var getTheme: GetTheme

    @Inject
    lateinit var themeApplicator: ThemeApplicator

    @Inject
    lateinit var mdnsProvider: MDNSProvider

    @Inject
    lateinit var featureToggles: FeatureToggles

    val container: FragmentContainerView get() = findViewById(R.id.fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1) Enable edge-to-edge with automatic light/dark icons
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                 lightScrim = Color.TRANSPARENT,
                 darkScrim  = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim  = Color.TRANSPARENT
            )
        )
        inject()
        setupTheme()
        setupFeatureIntroductions()

        if (savedInstanceState != null) vm.onRestore()

//        setFragmentLifecycleCallbacks()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.toasts.collect { toast(it) }
                }
                launch {
                    vm.dispatcher.collect { command ->
                        proceedWithNotificationCommand(command)
                    }
                }
                launch {
                    vm.wallpaperState.collect { wallpaper ->
                        setWallpaper(wallpaper)
                    }
                }
                launch {
                    vm.commands.collect { command ->
                        when (command) {
                            is Command.ShowDeletedAccountScreen -> {
                                navigator.deletedAccountScreen(
                                    deadline = command.deadline
                                )
                            }
                            is Command.LogoutDueToAccountDeletion -> {
                                navigator.logout()
                            }
                            is Command.OpenCreateNewType -> {
                                findNavController(R.id.fragment)
                                    .navigate(
                                        R.id.action_global_createObjectFragment,
                                        bundleOf(
                                            CreateObjectFragment.TYPE_KEY to command.type
                                        )
                                    )
                            }
                            is Command.Error -> {
                                toast(command.msg)
                            }
                            is Command.Notifications -> {
                                NotificationsFragment().show(supportFragmentManager, null)
                            }
                            is Command.RequestNotificationPermission -> {
                                runCatching {
                                    val controller = findNavController(R.id.fragment)
                                    val currentDestination = controller.currentDestination
                                    if (currentDestination?.id != R.id.requestNotificationPermissionDialog) {
                                        controller.navigate(R.id.requestNotificationPermissionDialog)
                                    } else {
                                        Timber.w("Request permission dialog already in stack.")
                                    }
                                }.onFailure {
                                    Timber.e(it, "Error while navigation")
                                }
                            }
                            is Command.Navigate -> {
                                proceedWithOpenObjectNavigation(command.destination)
                            }
                            is Command.Deeplink.DeepLinkToObjectNotWorking -> {
                                toast(getString(R.string.multiplayer_deeplink_to_your_object_error))
                            }
                            is Command.LaunchChat -> {
                                runCatching {
                                    val controller = findNavController(R.id.fragment)
                                    controller.popBackStack(R.id.vaultScreen, false)
                                    controller.navigate(
                                        R.id.actionOpenSpaceFromVault,
                                        WidgetsScreenFragment.args(
                                            space = command.space,
                                            deeplink = null
                                        )
                                    )
                                    controller.navigate(
                                        R.id.chatScreen,
                                        ChatFragment.args(
                                            space = command.space,
                                            ctx = command.chat,
                                            triggeredByPush = command.triggeredByPush
                                        )
                                    )
                                }.onFailure {
                                    if (BuildConfig.DEBUG) {
                                        toast("Failed to open chat from push notification")
                                    }
                                }
                            }
                            is Command.Deeplink.DeepLinkToObject -> {
                                when(val effect = command.sideEffect) {
                                    is Command.Deeplink.DeepLinkToObject.SideEffect.SwitchSpace -> {
                                        runCatching {
                                            val controller = findNavController(R.id.fragment)
                                            controller.popBackStack(R.id.vaultScreen, false)
                                            if (effect.chat != null && effect.spaceUxType == SpaceUxType.CHAT) {
                                                controller.navigate(
                                                    R.id.actionOpenChatFromVault,
                                                    ChatFragment.args(
                                                        space = command.space,
                                                        ctx = effect.chat.orEmpty()
                                                    )
                                                )
                                            } else {
                                                controller.navigate(
                                                    R.id.actionOpenSpaceFromVault,
                                                    WidgetsScreenFragment.args(
                                                        space = command.space,
                                                        deeplink = null
                                                    )
                                                )
                                            }
                                            proceedWithOpenObjectNavigation(command.navigation)
                                        }.onFailure {
                                            Timber.e(it, "Error while switching space when handling deep link to object")
                                        }
                                    }
                                    null -> {
                                        proceedWithOpenObjectNavigation(command.navigation)
                                    }
                                }
                            }
                            is Command.Deeplink.GalleryInstallation -> {
                                runCatching {
                                    findNavController(R.id.fragment).navigate(
                                        R.id.galleryInstallationScreen,
                                        GalleryInstallationFragment.args(
                                            deepLinkType = command.deepLinkType,
                                            deepLinkSource = command.deepLinkSource
                                        )
                                    )
                                }.onFailure {
                                    Timber.e(it, "Error while navigation for deep link gallery installation")
                                }
                            }
                            is Command.Deeplink.Invite -> {
                                runCatching {
                                    findNavController(R.id.fragment).navigate(
                                        R.id.requestJoinSpaceScreen,
                                        RequestJoinSpaceFragment.args(link = command.link)
                                    )
                                }.onFailure {
                                    Timber.e(it, "Error while navigation for deep link invite")
                                }
                            }
                            is Command.Deeplink.MembershipScreen -> {
                                runCatching {
                                    findNavController(R.id.fragment).navigate(
                                        R.id.paymentsScreen,
                                        MembershipFragment.args(tierId = command.tierId),
                                        NavOptions.Builder().setLaunchSingleTop(true).build()
                                    )
                                }.onFailure {
                                    Timber.w(it, "Error while navigation for deep link membership tier")
                                }
                            }
                            is Command.Deeplink.InitiateOneToOneChat -> {
                                // Navigate to vault where the deeplink will be processed
                                runCatching {
                                    val controller = findNavController(R.id.fragment)
                                    // Try to pop to Vault; if fails (not in stack), navigate to Vault
                                    if (!controller.popBackStack(R.id.vaultScreen, false)) {
                                        controller.navigate(R.id.vaultScreen)
                                    }
                                    // The VaultViewModel will handle the 1-1 chat initiation
                                }.onFailure {
                                    Timber.w(
                                        it,
                                        "Error while navigation for 1-1 chat initiation deeplink"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (savedInstanceState == null) {
            Timber.d("onSaveInstanceStateNull")
            when (intent.action) {
                Intent.ACTION_VIEW -> {
                    intent.data?.let { uri ->
                        val data = uri.toString()
                        if (DefaultDeepLinkResolver.isDeepLink(data)) {
                            vm.handleNewDeepLink(DefaultDeepLinkResolver.resolve(data))
                        }
                    }
                }
                Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> {
                    proceedWithShareIntent(intent)
                }
            }
        } else {
            Timber.d("onSaveInstanceStateNotNull")
        }
    }

    private fun setWallpaper(result: WallpaperResult) {
        when (result) {
            is WallpaperResult.Gradient -> {
                container.setBackgroundResource(getGradientDrawableResource(result.gradientCode))
                container.background?.alpha = WallpaperView.WALLPAPER_DEFAULT_ALPHA
            }
            is WallpaperResult.SolidColor -> {
                try {
                    container.setBackgroundColor(Color.parseColor(result.colorHex))
                    container.background?.alpha = WallpaperView.WALLPAPER_DEFAULT_ALPHA
                } catch (e: IllegalArgumentException) {
                    Timber.w(e, "Invalid color format: ${result.colorHex}")
                    container.background = null
                }
            }
            WallpaperResult.None -> {
                container.background = null
            }
        }
    }

    /**
     * Gets the default fallback drawable resource
     */
    @androidx.annotation.DrawableRes
    private fun getDefaultDrawableResource(): Int = 0

    private fun setFragmentLifecycleCallbacks() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
                    super.onFragmentResumed(fm, fragment)
                    Timber.d("onFragmentAdded: $fragment is ${fragment.tag}")
                }

                override fun onFragmentPaused(fm: FragmentManager, fragment: Fragment) {
                    super.onFragmentPaused(fm, fragment)
                    Timber.d("onFragmentPaused: $fragment is ${fragment.tag}")
                }
            }, true
        )
    }

    private fun proceedWithOpenObjectNavigation(dest: OpenObjectNavigation) {
        when (dest) {
            is OpenObjectNavigation.OpenDataView -> {
                runCatching {
                    findNavController(R.id.fragment).navigate(
                        R.id.dataViewNavigation,
                        args = ObjectSetFragment.args(
                            ctx = dest.target,
                            space = dest.space
                        ),
                        navOptions = Builder()
                            .setPopUpTo(R.id.homeScreen, true)
                            .build()
                    )
                }.onFailure {
                    Timber.e(it, "Error while data view navigation")
                }
            }

            is OpenObjectNavigation.OpenParticipant -> {
                runCatching {
                    findNavController(R.id.fragment).navigate(
                        R.id.participantScreen,
                        ParticipantFragment.args(
                            objectId = dest.target,
                            space = dest.space
                        )
                    )
                }.onFailure {
                    Timber.w("Error while opening participant screen")
                }
            }

            is OpenObjectNavigation.OpenEditor -> {
                runCatching {
                    findNavController(R.id.fragment).navigate(
                        R.id.objectNavigation,
                        args = EditorFragment.args(
                            ctx = dest.target,
                            space = dest.space
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while editor navigation")
                }
            }

            is OpenObjectNavigation.OpenChat -> {
                toast("Cannot open chat from here")
            }

            is OpenObjectNavigation.UnexpectedLayoutError -> {
                toast(getString(R.string.error_unexpected_layout))
            }

            OpenObjectNavigation.NonValidObject -> {
                toast(getString(R.string.error_non_valid_object))
            }

            is OpenObjectNavigation.OpenDateObject -> {
                runCatching {
                    findNavController(R.id.fragment).navigate(
                        R.id.dateObjectScreen,
                        args = DateObjectFragment.args(
                            objectId = dest.target,
                            space = dest.space
                        ),
                        navOptions = Builder()
                            .setPopUpTo(R.id.homeScreen, true)
                            .build()
                    )
                }.onFailure {
                    Timber.e(it, "Error while date object navigation")
                }
            }
            is OpenObjectNavigation.OpenType -> {
                runCatching {
                    findNavController(R.id.fragment).navigate(
                        resId = R.id.objectTypeNavigation,
                        args = ObjectTypeFragment.args(
                            objectId = dest.target,
                            space = dest.space
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening object type in main activity")
                }
            }
            is OpenObjectNavigation.OpenBookmarkUrl -> {
                try {
                    ActivityCustomTabsHelper.openUrl(
                        activity = this,
                        url = dest.url
                    )
                } catch (e: Throwable) {
                    Timber.e(e, "Error opening bookmark URL: ${dest.url}")
                    toast("Failed to open URL")
                }
            }
        }
    }

    private fun setupTheme() {
        runBlocking {
            getTheme(BaseUseCase.None).proceed(
                success = {
                    setTheme(it)
                },
                failure = {
                    Timber.e(it, "Error while setting current app theme")
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (BuildConfig.DEBUG) {
            Timber.d("on NewIntent: $intent")
        }
        when(intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    val data = uri.toString()
                    if (DefaultDeepLinkResolver.isDeepLink(data)) {
                        vm.handleNewDeepLink(DefaultDeepLinkResolver.resolve(data))

                        // Optionally clear to prevent repeat
                        intent.action = null
                        intent.data = null
                        intent.replaceExtras(Bundle())
                    }
                } ?: run {
                    intent.extras?.getString(DefaultAppActionManager.ACTION_CREATE_NEW_TYPE_KEY)?.let {
                        vm.onIntentCreateObject(it)
                    }
                }
            }
            Intent.ACTION_SEND -> {
                proceedWithShareIntent(intent, checkDeepLink = true)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                proceedWithShareIntent(intent, checkDeepLink = true)
            }
            AnytypeNotificationService.NOTIFICATION_INTENT_ACTION -> {
                proceedWithNotificationIntent(intent)
            }
            AnytypePushService.ACTION_OPEN_CHAT -> {
                proceedWithOpenChatIntent(intent)
            }
        }
    }

    private fun proceedWithOpenChatIntent(intent: Intent) {
        val chatId = intent.getStringExtra(Relations.CHAT_ID)
        val spaceId = intent.getStringExtra(Relations.SPACE_ID)
        if (!chatId.isNullOrEmpty() && !spaceId.isNullOrEmpty()) {
            if (!isChatFragmentVisible(chatId)) {
                vm.onOpenChatTriggeredByPush(
                    chatId = chatId,
                    spaceId = spaceId
                )
                // Clearing from-notification-to-chat intent.
                intent.replaceExtras(Bundle())
            } else {
                // Do nothing, already there.
            }
        }
    }

    private fun isChatFragmentVisible(chatId: String): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
        return if (currentFragment is ChatFragment) {
            currentFragment.ctx == chatId
        } else {
            false
        }
    }

    /**
     * Single entry point for all share intents.
     * Deep links are checked here, all other content is routed to SharingFragment.
     * SharingFragment handles MIME type detection internally.
     */
    private fun proceedWithShareIntent(intent: Intent, checkDeepLink: Boolean = false) {
        if (BuildConfig.DEBUG) Timber.d("Proceeding with share intent: type=${intent.type}, action=${intent.action}")

        // Check for deep links in text content first
        if (checkDeepLink && intent.type == Mimetype.MIME_TEXT_PLAIN.value) {
            val raw = intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.dataString
            if (raw != null && DefaultDeepLinkResolver.isDeepLink(raw)) {
                vm.handleNewDeepLink(DefaultDeepLinkResolver.resolve(raw))
                return
            }
        }

        // Single entry point: pass intent to SharingFragment via ViewModel
        vm.onShareIntent(intent)
    }

    private fun proceedWithNotificationIntent(intent: Intent) {
        when(val type = intent.getIntExtra(NOTIFICATION_TYPE, -1)) {
            AnytypeNotificationService.REQUEST_TO_JOIN_TYPE -> {
                val space = intent.getStringExtra(Relations.SPACE_ID)
                val identity = intent.getStringExtra(Relations.IDENTITY)
                if (!space.isNullOrEmpty() && !identity.isNullOrEmpty()) {
                    val notification = intent.getStringExtra(AnytypeNotificationService.NOTIFICATION_ID_KEY).orEmpty()
                    vm.onInterceptNotificationAction(
                        action = NotificationAction.Multiplayer.ViewSpaceJoinRequest(
                            notification = notification,
                            space = SpaceId(space),
                            identity = identity
                        )
                    )
                } else {
                    Timber.w("Missing space or identity")
                }
            }
            AnytypeNotificationService.REQUEST_TO_LEAVE_TYPE -> {
                val space = intent.getStringExtra(Relations.SPACE_ID)
                val identity = intent.getStringExtra(Relations.IDENTITY)
                if (!space.isNullOrEmpty() && !identity.isNullOrEmpty()) {
                    val notification = intent.getStringExtra(AnytypeNotificationService.NOTIFICATION_ID_KEY).orEmpty()
                    vm.onInterceptNotificationAction(
                        action = NotificationAction.Multiplayer.ViewSpaceLeaveRequest(
                            notification = notification,
                            space = SpaceId(space)
                        )
                    )
                } else {
                    Timber.w("Missing space or identity")
                }
            }
            AnytypeNotificationService.REQUEST_APPROVED_TYPE -> {
                val space = intent.getStringExtra(Relations.SPACE_ID)
                if (!space.isNullOrEmpty()) {
                    val notification = intent
                        .getStringExtra(AnytypeNotificationService.NOTIFICATION_ID_KEY)
                        .orEmpty()
                    vm.onInterceptNotificationAction(
                        action = NotificationAction.Multiplayer.GoToSpace(
                            notification = notification,
                            space = SpaceId(space)
                        )
                    )
                }
            }
            else -> {
                toast("Unknown type: $type")
            }
        }
    }

    private fun proceedWithNotificationCommand(command: NotificationCommand) {
        when (command) {
            is NotificationCommand.ViewSpaceJoinRequest -> {
                runCatching {
                    findNavController(R.id.fragment).navigate(
                        R.id.spaceJoinRequestScreen,
                        SpaceJoinRequestFragment.args(
                            space = command.space,
                            member = command.member,
                            analyticsRoute = EventsDictionary.Routes.notification
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }

            is NotificationCommand.ViewSpaceLeaveRequest -> {
                runCatching {
                    findNavController(R.id.fragment).navigate(
                        R.id.shareSpaceScreen,
                        ShareSpaceFragment.args(space = command.space)
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }

            is NotificationCommand.GoToSpace -> {
                runCatching {
                    findNavController(R.id.fragment).popBackStack(R.id.vaultScreen, false)
                    findNavController(R.id.fragment).navigate(
                        R.id.actionOpenSpaceFromVault,
                        WidgetsScreenFragment.args(
                            space = command.space.id,
                            deeplink = null
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }
        }
    }

    private fun setTheme(themeMode: ThemeMode) {
        themeApplicator.apply(themeMode)
    }

    override fun onResume() {
        super.onResume()
        mdnsProvider.start()
        navigator.bind(findNavController(R.id.fragment))
    }

    override fun onPause() {
        super.onPause()
        mdnsProvider.stop()
        navigator.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        mdnsProvider.stop()
        release()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        runCatching {
            permissions.forEachIndexed { index, permission ->
                when(permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> {
                        val result = grantResults[index]
                        if (result == PackageManager.PERMISSION_GRANTED)
                            vm.onNotificationPermissionGranted()
                        else
                            vm.onNotificationPermissionDenied()
                    }
                }
            }
        }.onFailure {
            Timber.e(it, "Error while handling permission results")
        }
    }

    override fun nav(): AppNavigation = navigator

    /**
     * Sets up Compose overlays for modals and introductions.
     * Handles:
     * - SpacesIntroductionScreen for existing users
     * - SharingModalSheet for share intents
     */
    @OptIn(ExperimentalMaterial3Api::class)
    private fun setupFeatureIntroductions() {
        findViewById<ComposeView>(R.id.composeOverlay).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val showSpacesIntroduction by vm.showSpacesIntroduction.collectAsState()
                val sharingIntent by vm.sharingIntent.collectAsState()

                // Spaces Introduction Screen
                if (showSpacesIntroduction != null) {
                    SpacesIntroductionScreen(
                        onDismiss = {
                            vm.onSpacesIntroductionDismissed()
                        },
                        onComplete = {
                            vm.onSpacesIntroductionDismissed()
                        },
                        onPageChanged = { step ->
                            vm.sendOnboardingTooltipEvent(step)
                        }
                    )
                }

                // Sharing Modal Sheet
                sharingIntent?.let { intent ->
                    SharingModalHost(
                        intent = intent,
                        onDismiss = {
                            vm.onSharingDismissed()
                        }
                    )
                }
            }
        }
    }

    /**
     * Composable host for the sharing modal that manages SharingViewModel lifecycle.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SharingModalHost(
        intent: Intent,
        onDismiss: () -> Unit
    ) {
        // Get or create SharingComponent and ViewModel
        val sharingComponent = remember {
            componentManager().sharingComponent.get()
        }

        // Create ViewModel using the factory from the component
        val viewModel = remember {
            val factory = sharingComponent.viewModelFactory()
            factory.create(SharingViewModel::class.java)
        }

        // Convert intent to SharedContent and pass to ViewModel
        LaunchedEffect(intent) {
            val sharedContent = IntentToSharedContentConverter.convert(intent)
            viewModel.onSharedDataReceived(sharedContent)
        }

        // Collect screen state
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        // Handle commands from ViewModel
        LaunchedEffect(Unit) {
            viewModel.commands.collect { command ->
                when (command) {
                    is SharingCommand.Dismiss -> {
                        onDismiss()
                    }
                    is SharingCommand.ShowToast -> {
                        toast(command.message)
                    }
                    is SharingCommand.ShowSnackbarWithOpenAction -> {
                        // Dismiss modal and show Snackbar
                        onDismiss()
                        val message = getSnackbarMessage(
                            contentType = command.contentType,
                            destinationName = command.destinationName,
                            spaceName = command.spaceName
                        )
                        findViewById<android.view.View>(android.R.id.content)?.showSnackbar(
                            msg = message,
                            length = Snackbar.LENGTH_INDEFINITE,
                            actionMessage = getString(R.string.button_ok),
                            action = {
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }

        // Clean up when the composable leaves composition
        DisposableEffect(Unit) {
            onDispose {
                componentManager().sharingComponent.release()
            }
        }

        // Render the modal sheet
        SharingModalSheet(
            state = screenState,
            onSpaceSelected = viewModel::onSpaceSelected,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onCommentChanged = viewModel::onCommentChanged,
            onSendClicked = viewModel::onSendClicked,
            onObjectSelected = viewModel::onObjectSelected,
            onBackPressed = viewModel::onBackPressed,
            onDismiss = onDismiss,
            onRetryClicked = viewModel::onRetryClicked
        )
    }

    /**
     * Returns content-specific Snackbar message based on the shared content type.
     * If spaceName is provided, uses "linked to" format; otherwise uses "added to" format.
     */
    private fun getSnackbarMessage(
        contentType: SharedContent,
        destinationName: String,
        spaceName: String?
    ): String {
        return if (spaceName != null) {
            // Linked to object format: "Content linked to 'ObjectName' in 'SpaceName'"
            val stringResId = when (contentType) {
                is SharedContent.Text -> R.string.sharing_snackbar_text_linked
                is SharedContent.Url -> R.string.sharing_snackbar_link_linked
                is SharedContent.SingleMedia -> when (contentType.type) {
                    SharedContent.MediaType.IMAGE -> R.string.sharing_snackbar_image_linked
                    SharedContent.MediaType.VIDEO -> R.string.sharing_snackbar_video_linked
                    SharedContent.MediaType.AUDIO -> R.string.sharing_snackbar_audio_linked
                    SharedContent.MediaType.PDF -> R.string.sharing_snackbar_pdf_linked
                    SharedContent.MediaType.FILE -> R.string.sharing_snackbar_file_linked
                }
                is SharedContent.MultipleMedia -> when (contentType.type) {
                    SharedContent.MediaType.IMAGE -> R.string.sharing_snackbar_images_linked
                    else -> R.string.sharing_snackbar_files_linked
                }
            }
            getString(stringResId, destinationName, spaceName)
        } else {
            // Added to space/chat format: "Content added to 'Name'"
            val stringResId = when (contentType) {
                is SharedContent.Text -> R.string.sharing_snackbar_text_added
                is SharedContent.Url -> R.string.sharing_snackbar_link_added
                is SharedContent.SingleMedia -> when (contentType.type) {
                    SharedContent.MediaType.IMAGE -> R.string.sharing_snackbar_image_added
                    SharedContent.MediaType.VIDEO -> R.string.sharing_snackbar_video_added
                    SharedContent.MediaType.AUDIO -> R.string.sharing_snackbar_audio_added
                    SharedContent.MediaType.PDF -> R.string.sharing_snackbar_pdf_added
                    SharedContent.MediaType.FILE -> R.string.sharing_snackbar_file_added
                }
                is SharedContent.MultipleMedia -> when (contentType.type) {
                    SharedContent.MediaType.IMAGE -> R.string.sharing_snackbar_images_added
                    else -> R.string.sharing_snackbar_files_added
                }
            }
            getString(stringResId, destinationName)
        }
    }

    fun inject() {
        componentManager().mainEntryComponent.get().inject(this)
    }

    fun release() {
        componentManager().mainEntryComponent.release()
    }
}
