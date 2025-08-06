package com.anytypeio.anytype.ui.onboarding

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Right
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navArgument
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_ui.BuildConfig.LIBRARY_PACKAGE_NAME
import com.anytypeio.anytype.core_ui.MNEMONIC_WORD_COUNT
import com.anytypeio.anytype.core_ui.MnemonicPhrasePaletteColors
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.shareFirstFileFromPath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.insets.RootViewDeferringInsetsCallback
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel.SideEffect
import com.anytypeio.anytype.presentation.onboarding.OnboardingViewModel
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingMnemonicViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.onboarding.screens.AuthScreen
import com.anytypeio.anytype.ui.onboarding.screens.signin.RecoveryScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.MnemonicPhraseScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.SetEmailWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.SetProfileNameWrapper
import com.anytypeio.anytype.ui.vault.VaultFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.google.zxing.integration.android.IntentIntegrator
import javax.inject.Inject
import kotlinx.coroutines.delay
import timber.log.Timber
import androidx.lifecycle.compose.currentStateAsState

class OnboardingFragment : Fragment() {

    private val deepLink: String? get() = argOrNull(ONBOARDING_DEEP_LINK_KEY)

    @Inject
    lateinit var factory: OnboardingViewModel.Factory

    private val onBoardingViewModel by viewModels<OnboardingViewModel> { factory }

    private val mnemonicColorPalette by lazy {
        buildList {
            var idx = 0
            repeat(MNEMONIC_WORD_COUNT) {
                if (idx > MnemonicPhrasePaletteColors.lastIndex) {
                    idx = 0
                }
                val color = MnemonicPhrasePaletteColors[idx]
                add(color)
                idx += 1
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            runCatching {
                WindowCompat
                    .getInsetsController(
                        requireActivity().window,
                        requireActivity().window.decorView
                    ).isAppearanceLightStatusBars = false
            }.onFailure {
                Timber.e(it, "Error while changing status bars in onCreate")
            }
        }
        injectDependencies()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)  {
            runCatching {
                WindowCompat
                    .getInsetsController(
                        requireActivity().window,
                        requireActivity().window.decorView
                    ).isAppearanceLightStatusBars = true
            }.onFailure {
                Timber.e(it, "Error while changing status bars in onDestroy")
            }
        }
        releaseDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OnboardingScreen()
            }
        }
    }

    @Composable
    private fun OnboardingScreen() {
        MaterialTheme {
            val navController = rememberNavController()
            val defaultBackCallback: BackButtonCallback = { navController.popBackStack() }
            val signUpBackButtonCallback = remember {
                mutableStateOf(defaultBackCallback)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                                Modifier.windowInsetsPadding(insets = WindowInsets.systemBars)
                            else
                                Modifier
                        )
                ) {
                    val currentPage = remember { mutableStateOf(OnboardingPage.AUTH) }
                    BackgroundCircle()
                    Onboarding(
                        currentPage = currentPage,
                        navController = navController,
                        backButtonCallback = signUpBackButtonCallback
                    )
                    PagerIndicator(
                        pageCount = OnboardingPage.entries.filter { it.visible }.size,
                        page = currentPage,
                        onBackClick = {
                            signUpBackButtonCallback.value?.invoke()
                        }
                    )
                }
            }
            LaunchedEffect(Unit) {
                onBoardingViewModel.toasts.collect {
                    toast(it)
                }
            }
            DisposableEffect(
                viewLifecycleOwner.lifecycle.currentStateAsState().value.isAtLeast(Lifecycle.State.DESTROYED)
            ) {
                onDispose {
                    signUpBackButtonCallback.value = null
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onApplyWindowRootInsets(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Disabling workaround to prevent background circle with video shrinking.
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun onApplyWindowRootInsets(view: View) {
        if ( Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = 0
            )

            ViewCompat.setWindowInsetsAnimationCallback(view, deferringInsetsListener)
            ViewCompat.setOnApplyWindowInsetsListener(view, deferringInsetsListener)
        } else {
            // Enabling workaround to prevent background circle with video shrinking.
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    @Composable
    private fun Onboarding(
        currentPage: MutableState<OnboardingPage>,
        backButtonCallback: MutableState<BackButtonCallback>,
        navController: NavHostController
    ) {
        NavHost(navController, startDestination = OnboardingNavigation.auth) {
            composable(
                route = OnboardingNavigation.auth,
                enterTransition = { null },
                exitTransition = {
                    fadeOut(tween(150))
                }
            ) {
                currentPage.value = OnboardingPage.AUTH
                Auth(navController)
            }
            composable(
                route = OnboardingNavigation.recovery,
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                currentPage.value = OnboardingPage.RECOVERY
                Recovery(navController)
            }
            composable(
                route = "${OnboardingNavigation.mnemonic}?$ONBOARDING_SPACE_PARAM={$ONBOARDING_SPACE_PARAM}&$ONBOARDING_STARTING_OBJECT_PARAM={$ONBOARDING_STARTING_OBJECT_PARAM}",
                arguments = listOf(
                    navArgument(ONBOARDING_SPACE_PARAM) { type = NavType.StringType },
                    navArgument(ONBOARDING_STARTING_OBJECT_PARAM) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = {
                    when (initialState.destination.route) {
                        OnboardingNavigation.setProfileName -> {
                            slideIntoContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }
                        else -> {
                            slideIntoContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        OnboardingNavigation.setProfileName -> {
                            slideOutOfContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                        else -> {
                            slideOutOfContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                }
            ) {
                currentPage.value = OnboardingPage.MNEMONIC
                backButtonCallback.value = {
                    // Do nothing
                }
                val spaceId = it.arguments?.getString(ONBOARDING_SPACE_PARAM)
                val startingObjectId = it.arguments?.getString(ONBOARDING_STARTING_OBJECT_PARAM)
                if (!spaceId.isNullOrEmpty()) {
                    Mnemonic(
                        mnemonicColorPalette = mnemonicColorPalette,
                        space = spaceId,
                        startingObject = startingObjectId,
                        navController = navController
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_error_while_creating_account_space_is_missing),
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                BackHandler {
                    toast("You're just one step away from finishing this registration.")
                }
            }
            composable(
                route = OnboardingNavigation.setProfileName,
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                val focus = LocalFocusManager.current
                val onBackClicked : () -> Unit = {
                    val lastDestination = navController.currentBackStackEntry
                    if (lastDestination?.destination?.route == OnboardingNavigation.setProfileName) {
                        focus.clearFocus(true)
                        navController.popBackStack()
                    }  else {
                        Timber.d("Skipping exit click...")
                    }
                }
                currentPage.value = OnboardingPage.SET_PROFILE_NAME
                backButtonCallback.value = onBackClicked
                SetProfileName(
                    navController = navController,
                    onBackClicked = onBackClicked
                )
                BackHandler { onBackClicked() }
            }
            composable(
                route = "${OnboardingNavigation.setEmail}?$ONBOARDING_SPACE_PARAM={$ONBOARDING_SPACE_PARAM}&$ONBOARDING_STARTING_OBJECT_PARAM={$ONBOARDING_STARTING_OBJECT_PARAM}",
                arguments = listOf(
                    navArgument(ONBOARDING_SPACE_PARAM) { type = NavType.StringType },
                    navArgument(ONBOARDING_STARTING_OBJECT_PARAM) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                val focus = LocalFocusManager.current
                val spaceId = it.arguments?.getString(ONBOARDING_SPACE_PARAM)
                val startingObjectId = it.arguments?.getString(ONBOARDING_STARTING_OBJECT_PARAM)
                val onBackClicked : () -> Unit = {
                    val lastDestination = navController.currentBackStackEntry
                    if (lastDestination?.destination?.route?.startsWith(OnboardingNavigation.setEmail) == true) {
                        focus.clearFocus(true)
                        navController.popBackStack()
                    } else {
                        Timber.d("Skipping exit click...")
                    }
                }
                currentPage.value = OnboardingPage.SET_EMAIL
                backButtonCallback.value = onBackClicked
                if (!spaceId.isNullOrEmpty()) {
                    AddEmail(
                        space = spaceId,
                        startingObject = startingObjectId,
                        onBackClicked = onBackClicked
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_error_while_creating_account_space_is_missing),
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                BackHandler { onBackClicked() }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Recovery(navController: NavHostController) {
        val component = componentManager().onboardingMnemonicLoginComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        val isQrWarningDialogVisible = remember { mutableStateOf(false) }
        val errorText = remember { mutableStateOf(NO_VALUE) }
        val isErrorDialogVisible = remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val r = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (r != null && r.contents != null) {
                vm.onGetEntropyFromQRCode(entropy = r.contents)
            }
        }
        RecoveryScreenWrapper(
            vm = vm,
            onBackClicked = vm::onBackButtonPressed,
            onScanQrClick = {
                isQrWarningDialogVisible.value = true
                vm.onScanQrCodeClicked()
            },
        )
        LaunchedEffect(Unit) {
            vm.sideEffects.collect { effect ->
                when (effect) {
                    is OnboardingMnemonicLoginViewModel.SideEffect.Exit -> {
                        val lastDestination = navController.currentBackStackEntry
                        // TODO Temporary workaround to prevent inconsistent state in navigation
                        if (lastDestination?.destination?.route == OnboardingNavigation.recovery) {
                            navController.popBackStack()
                        }
                    }
                    is OnboardingMnemonicLoginViewModel.SideEffect.Error.Unknown -> {
                        errorText.value = effect.msg
                        isErrorDialogVisible.value = true
                    }
                    is OnboardingMnemonicLoginViewModel.SideEffect.Error.InvalidMnemonic -> {
                        errorText.value = getString(R.string.error_invalid_recovery_phrase)
                        isErrorDialogVisible.value = true
                    }
                    is OnboardingMnemonicLoginViewModel.SideEffect.Error.NetworkIdMismatch -> {
                        errorText.value = getString(R.string.error_login_network_id_mismatch)
                        isErrorDialogVisible.value = true
                    }
                    is OnboardingMnemonicLoginViewModel.SideEffect.Error.SelectVaultError -> {
                        errorText.value = getString(R.string.error_login_select_vault_error)
                        isErrorDialogVisible.value = true
                    }
                    is OnboardingMnemonicLoginViewModel.SideEffect.Error.AccountDeletedError -> {
                        errorText.value = getString(R.string.error_login_account_deleted_error)
                        isErrorDialogVisible.value = true
                    }
                    is OnboardingMnemonicLoginViewModel.SideEffect.Error.NeedUpdateError -> {
                        errorText.value = getString(R.string.error_login_account_need_update_error)
                        isErrorDialogVisible.value = true
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.command.collect { command ->
                Timber.d("Command: $command")
                when (command) {
                    OnboardingMnemonicLoginViewModel.Command.Exit -> {
                        navController.popBackStack()
                    }
                    OnboardingMnemonicLoginViewModel.Command.NavigateToVaultScreen -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                        }.onFailure {
                            Timber.e(it, "Error while trying to open vault screen from onboarding")
                        }
                    }
                    is OnboardingMnemonicLoginViewModel.Command.ShareDebugGoroutines -> {
                        try {
                            this@OnboardingFragment.shareFirstFileFromPath(command.path, command.uriFileProvider)
                        } catch (e: Exception) {
                            Timber.e(e, "Error while stack goroutines debug").also {
                                toast("Error while stack goroutines debug. Please try again later.")
                            }
                        }
                    }
                    is OnboardingMnemonicLoginViewModel.Command.ShowToast -> {
                        toast(command.message)
                    }
                }
            }
        }
        if (isQrWarningDialogVisible.value) {
            BaseAlertDialog(
                dialogText = stringResource(id = R.string.alert_qr_camera),
                buttonText = stringResource(id = R.string.alert_qr_camera_ok),
                onButtonClick = {
                    isQrWarningDialogVisible.value = false
                    proceedWithQrCodeActivity(launcher)
                },
                onDismissRequest = { isQrWarningDialogVisible.value = false }
            )
        }
        if (isErrorDialogVisible.value) {
            BaseAlertDialog(
                dialogText = errorText.value,
                buttonText = stringResource(id = R.string.alert_qr_camera_ok),
                onButtonClick = {
                    isErrorDialogVisible.value = false
                    errorText.value = NO_VALUE
                },
                onDismissRequest = {
                    isErrorDialogVisible.value = false
                    errorText.value = NO_VALUE
                }
            )
        }
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }

    private fun proceedWithQrCodeActivity(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        try {
            launcher.launch(
                IntentIntegrator
                    .forSupportFragment(this)
                    .setBeepEnabled(false)
                    .createScanIntent()
            )
        } catch (e: Exception) {
            toast("Error while scanning QR code")
            Timber.e(e, "Error while scanning QR code")
        }
    }

    @Composable
    private fun SetProfileName(
        navController: NavHostController,
        onBackClicked: () -> Unit
    ) {
        val component = componentManager().onboardingSoulCreationComponent
        val vm = daggerViewModel { component.get().getViewModel() }

        val focusManager = LocalFocusManager.current
        val keyboardInsets = WindowInsets.ime
        val density = LocalDensity.current

        SetProfileNameWrapper(
            viewModel = vm,
            onBackClicked = onBackClicked
        )

        LaunchedEffect(Unit) {
            vm.navigation.collect { command ->
                when (command) {
                    is OnboardingSetProfileNameViewModel.Navigation.NavigateToMnemonic -> {
                        if (keyboardInsets.getBottom(density) > 0) {
                            focusManager.clearFocus(force = true)
                            delay(KEYBOARD_HIDE_DELAY)
                        }
                        val space = command.space
                        val startingObject = command.startingObject
                        navController.navigate(
                            route = buildString {
                                append("${OnboardingNavigation.mnemonic}?$ONBOARDING_SPACE_PARAM=${space.id}")
                                startingObject?.let { append("&$ONBOARDING_STARTING_OBJECT_PARAM=${it}") }
                            }
                        )
                    }
                    is OnboardingSetProfileNameViewModel.Navigation.GoBack -> {
                        // do nothing
                    }

                    is OnboardingSetProfileNameViewModel.Navigation.OpenStartingObject -> {
                        //do nothing
                    }
                    OnboardingSetProfileNameViewModel.Navigation.OpenVault -> {
                        //do nothing
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.toasts.collect {
                toast(it)
            }
        }
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }

    @Composable
    private fun Mnemonic(
        mnemonicColorPalette: List<Color>,
        space: Id,
        startingObject: Id?,
        navController: NavHostController
    ) {
        val component = componentManager().onboardingMnemonicComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        MnemonicPhraseScreenWrapper(
            space = space,
            startingObject = startingObject,
            viewModel = vm,
            copyMnemonicToClipboard = ::copyMnemonicToClipboard,
            vm = vm,
            mnemonicColorPalette = mnemonicColorPalette
        )
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
        LaunchedEffect(Unit) {
            vm.commands.collect { command ->
                when(command) {
                    OnboardingMnemonicViewModel.Command.OpenVault -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                        }.onFailure {
                            Timber.e(it, "Error while navigation to vault")
                        }
                    }
                    is OnboardingMnemonicViewModel.Command.OpenStartingObject -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                            findNavController().navigate(
                                R.id.actionOpenSpaceFromVault,
                                HomeScreenFragment.args(
                                    space = command.space.id,
                                    deeplink = null
                                )
                            )
                            findNavController().navigate(
                                R.id.objectNavigation,
                                EditorFragment.args(
                                    ctx = command.startingObject,
                                    space = command.space.id,
                                )
                            )
                        }.onFailure {
                            Timber.e(it, "Error while navigation to vault")
                        }
                    }
                    is OnboardingMnemonicViewModel.Command.NavigateToAddEmailScreen -> {
                        val startingObject = command.startingObject
                        val space = command.space
                        navController.navigate(
                            route = "${OnboardingNavigation.setEmail}?$ONBOARDING_SPACE_PARAM=$space&$ONBOARDING_STARTING_OBJECT_PARAM=$startingObject"
                        )
                    }
                }
            }
        }
    }

    private fun copyMnemonicToClipboard(mnemonicPhrase: String) {
        try {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip =
                ClipData.newPlainText("Mnemonic phrase", mnemonicPhrase)
            clip.apply {
                description.extras = PersistableBundle().apply {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
            }
            clipboard.setPrimaryClip(clip)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                toast("Mnemonic phrase copied")
            }
        } catch (e: Exception) {
            toast("Could not copy your mnemonic phrase. Please try again later, or copy it manually.")
        }
    }

    @Composable
    private fun Auth(navController: NavHostController) {
        val component = componentManager().onboardingStartComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        AuthScreen(vm = vm)
        ErrorScreen(vm = vm)
        LaunchedEffect(Unit) {
            vm.sideEffects.collect { effect ->
                when (effect) {
                    SideEffect.OpenPrivacyPolicy -> {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.about_privacy_policy_link))
                            )
                            startActivity(intent)
                        } catch (e: Exception) {
                            Timber.e(e, "Error while opening privacy policy")
                        }
                    }

                    SideEffect.OpenTermsOfUse -> {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.about_terms_and_conditions_link))
                            )
                            startActivity(intent)
                        } catch (e: Exception) {
                            Timber.e(e, "Error while opening terms of use")
                        }
                    }
                    SideEffect.OpenNetworkSettings -> {
                        val dialog = OnboardingNetworkSetupDialog()
                        dialog.show(childFragmentManager, "")
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.navigation.collect { navigation ->
                when (navigation) {
                    is OnboardingStartViewModel.AuthNavigation.ProceedWithSignUp -> {
                        navController.navigate(OnboardingNavigation.setProfileName)
                    }

                    is OnboardingStartViewModel.AuthNavigation.ProceedWithSignIn -> {
                        navController.navigate(OnboardingNavigation.recovery)
                    }
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen(vm: OnboardingStartViewModel) {
        when (val errorState = vm.errorState.collectAsStateWithLifecycle().value) {
            OnboardingStartViewModel.ErrorState.Hidden -> {
                // No dialog to show
            }
            is OnboardingStartViewModel.ErrorState.Generic -> {
                BaseAlertDialog(
                    dialogText = errorState.message,
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.NetworkError -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_network_error),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.OfflineDevice -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_offline_device),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            is OnboardingStartViewModel.ErrorState.WalletSetupError -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.error_wallet_setup),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.AccountCreatedButFailedToStartNode -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_account_created_but_failed_to_start_node),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.AccountCreatedButFailedToSetName -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_account_created_but_failed_to_set_name),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.FailedToStopRunningNode -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_failed_to_stop_running_node),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.FailedToWriteConfig -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_failed_to_write_config),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.FailedToCreateLocalRepo -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_failed_to_create_local_repo),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.AccountCreationCanceled -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_account_creation_canceled),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.ConfigFileNotFound -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_config_file_not_found),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.ConfigFileInvalid -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_config_file_invalid),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
            OnboardingStartViewModel.ErrorState.ConfigFileNetworkIdMismatch -> {
                BaseAlertDialog(
                    dialogText = stringResource(id = R.string.onboarding_error_config_file_network_id_mismatch),
                    buttonText = stringResource(id = R.string.button_ok),
                    onButtonClick = vm::onErrorDismissed,
                    onDismissRequest = vm::onErrorDismissed
                )
            }
        }
    }

    @Composable
    fun BackgroundCircle() {
        val context = LocalContext.current
        val videoPath = RawResourceDataSource.buildRawResourceUri(R.raw.shader)

        val paddingTop = LocalConfiguration.current.screenHeightDp / 5
        val padding = remember {
            paddingTop
        }

        val exoPlayer = remember { getVideoPlayer(context, videoPath) }
        Box(modifier = Modifier.fillMaxSize()) {
            DisposableEffect(
                AndroidView(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = padding.dp)
                        .scale(1.7f),
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                        }
                    }
                )
            ) {
                onDispose {
                    exoPlayer.release()
                }
            }
        }
    }

    @Composable
    private fun AddEmail(
        space: Id,
        startingObject: Id?,
        onBackClicked: () -> Unit
    ) {
        val component = componentManager().onboardingSoulCreationComponent
        val vm = daggerViewModel { component.get().getViewModel() }

        SetEmailWrapper(
            viewModel = vm,
            startingObject = startingObject,
            space = space,
            onBackClicked = onBackClicked
        )

        LaunchedEffect(Unit) {
            vm.navigation.collect { command ->
                when (command) {
                    is OnboardingSetProfileNameViewModel.Navigation.NavigateToMnemonic -> {
                        //do nothing
                    }
                    is OnboardingSetProfileNameViewModel.Navigation.GoBack -> {
                        //
                    }

                    is OnboardingSetProfileNameViewModel.Navigation.OpenStartingObject -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                            findNavController().navigate(
                                R.id.actionOpenSpaceFromVault,
                                HomeScreenFragment.args(
                                    space = command.space.id,
                                    deeplink = null
                                )
                            )
                            findNavController().navigate(
                                R.id.objectNavigation,
                                EditorFragment.args(
                                    ctx = command.startingObject,
                                    space = command.space.id,
                                )
                            )
                        }.onFailure {
                            Timber.e(it, "Error while navigation to vault")
                        }
                    }
                    OnboardingSetProfileNameViewModel.Navigation.OpenVault -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                        }.onFailure {
                            Timber.e(it, "Error while navigation to vault")
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.toasts.collect {
                toast(it)
            }
        }
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }

    private fun getVideoPlayer(context: Context, videoPath: Uri): Player {
        val player = ExoPlayer.Builder(context).build()
        val source = DefaultDataSource.Factory(
            context,
            DefaultHttpDataSource.Factory().setUserAgent(
                Util.getUserAgent(context, LIBRARY_PACKAGE_NAME)
            )
        )
        val mediaSource = ProgressiveMediaSource
            .Factory(source)
            .createMediaSource(MediaItem.fromUri(videoPath))
        player.seekTo(0)
        player.setMediaSource(mediaSource)
        player.playWhenReady = true
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.prepare()
        return player
    }

    fun injectDependencies() {
        componentManager().onboardingComponent.get().inject(this)
    }

    fun releaseDependencies() {
        with(componentManager()) {
            onboardingComponent.release()
            onboardingMnemonicComponent.release()
            onboardingMnemonicLoginComponent.release()
            onboardingStartComponent.release()
        }
        componentManager().onboardingComponent.release()
    }

    companion object {
        fun args(deepLink: String?) = bundleOf(
            ONBOARDING_DEEP_LINK_KEY to deepLink
        )
        private const val ONBOARDING_DEEP_LINK_KEY = "arg.onboarding.deep-link-key"

        private const val ONBOARDING_SPACE_PARAM = "space"
        private const val ONBOARDING_STARTING_OBJECT_PARAM = "startingObject"

        private const val ONBOARDING_NAME_PARAM = "startingObject"
    }
}

private const val ANIMATION_LENGTH_SLIDE = 300
private const val ANIMATION_LENGTH_FADE = 700
private const val KEYBOARD_HIDE_DELAY = 300L

typealias BackButtonCallback = (() -> Unit)?