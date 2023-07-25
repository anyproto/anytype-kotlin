package com.anytypeio.anytype.ui.onboarding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Right
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.MNEMONIC_WORD_COUNT
import com.anytypeio.anytype.core_ui.MnemonicPhrasePaletteColors
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.RootViewDeferringInsetsCallback
import com.anytypeio.anytype.di.common.ComponentManager
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel.SideEffect
import com.anytypeio.anytype.presentation.onboarding.OnboardingViewModel
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingLoginSetupViewModel
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSoulCreationViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingVoidViewModel
import com.anytypeio.anytype.ui.onboarding.screens.AuthScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signin.EnteringTheVoidScreen
import com.anytypeio.anytype.ui.onboarding.screens.signin.RecoveryScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.CreateSoulAnimWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.CreateSoulWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.MnemonicPhraseScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.VoidScreenWrapper
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
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

class OnboardingFragment : Fragment() {

    @Inject
    lateinit var factory: OnboardingViewModel.Factory

    private val onBoardingViewModel by viewModels<OnboardingViewModel> { factory }

    private val mnemonicColorPalette by lazy {
        buildList {
            repeat(MNEMONIC_WORD_COUNT) {
                add(MnemonicPhrasePaletteColors.random())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onDestroy() {
        super.onDestroy()
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
    @OptIn(ExperimentalAnimationApi::class)
    private fun OnboardingScreen() {
        MaterialTheme {
            val navController = rememberAnimatedNavController()
            val defaultBackCallback: BackButtonCallback = { navController.popBackStack() }
            val signUpBackButtonCallback = remember {
                mutableStateOf(defaultBackCallback)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val currentPage = remember { mutableStateOf(OnboardingPage.AUTH) }
                BackgroundCircle()
                Onboarding(
                    currentPage = currentPage,
                    navController = navController,
                    backButtonCallback = signUpBackButtonCallback
                )
                PagerIndicator(
                    pageCount = OnboardingPage.values().filter { it.visible }.size,
                    page = currentPage,
                    onBackClick = {
                        signUpBackButtonCallback.value?.invoke()
                    }
                )
            }
            LaunchedEffect(Unit) {
                onBoardingViewModel.toasts.collect {
                    toast(it)
                }
            }
            DisposableEffect(
                viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)
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

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun Onboarding(
        currentPage: MutableState<OnboardingPage>,
        backButtonCallback: MutableState<BackButtonCallback>,
        navController: NavHostController
    ) {
        AnimatedNavHost(navController, startDestination = OnboardingNavigation.auth) {
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
                route = OnboardingNavigation.void,
                enterTransition = {
                    when (initialState.destination.route) {
                        OnboardingNavigation.auth -> {
                            fadeIn(tween(ANIMATION_LENGTH_FADE))
                        }
                        else -> {
                            slideIntoContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        OnboardingNavigation.auth -> {
                            fadeOut(tween(ANIMATION_LENGTH_FADE))
                        }
                        else -> {
                            slideOutOfContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                }
            ) {
                currentPage.value = OnboardingPage.VOID
                val component = componentManager().onboardingNewVoidComponent
                val vm = daggerViewModel { component.get().getViewModel() }

                VoidScreenWrapper(
                    contentPaddingTop = ContentPaddingTop(),
                    onNextClicked = vm::onNextClicked,
                    screenState = vm.state.collectAsState().value
                )

                backButtonCallback.value = vm::onBackButtonPressed
                BackHandler { vm.onSystemBackPressed() }

                LaunchedEffect(Unit) {
                    vm.navigation.collect { navigation ->
                        when (navigation) {
                            OnboardingVoidViewModel.Navigation.GoBack -> {
                                navController.popBackStack()
                            }

                            OnboardingVoidViewModel.Navigation.NavigateToMnemonic -> {
                                navController.navigate(OnboardingNavigation.mnemonic)
                            }
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    vm.toasts.collect { toast(it) }
                }
                DisposableEffect(Unit) {
                    onDispose { component.release() }
                }
            }
            composable(
                route = OnboardingNavigation.mnemonic,
                enterTransition = {
                    when (initialState.destination.route) {
                        OnboardingNavigation.void -> {
                            slideIntoContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }

                        else -> {
                            slideIntoContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        OnboardingNavigation.void -> {
                            slideOutOfContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }

                        else -> {
                            slideOutOfContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                }
            ) {
                currentPage.value = OnboardingPage.MNEMONIC
                backButtonCallback.value = { navController.popBackStack() }
                Mnemonic(
                    navController = navController,
                    contentPaddingTop = ContentPaddingTop(),
                    mnemonicColorPalette = mnemonicColorPalette
                )
            }
            composable(
                route = OnboardingNavigation.createSoul,
                enterTransition = { slideIntoContainer(Left, tween(ANIMATION_LENGTH_SLIDE)) },
                exitTransition = {
                    when (targetState.destination.route) {
                        OnboardingNavigation.mnemonic -> {
                            slideOutOfContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }

                        else -> {
                            fadeOut(tween(ANIMATION_LENGTH_FADE))
                        }
                    }
                }
            ) {
                currentPage.value = OnboardingPage.SOUL_CREATION
                backButtonCallback.value = { navController.popBackStack() }
                CreateSoul(
                    navController = navController,
                    contentPaddingTop = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                        LocalConfiguration.current.screenHeightDp / 6
                    else
                        ContentPaddingTop()
                )
            }
            composable(
                route = OnboardingNavigation.createSoulAnim,
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                currentPage.value = OnboardingPage.SOUL_CREATION_ANIM
                backButtonCallback.value = null
                CreateSoulAnimation(ContentPaddingTop())
                BackHandler {
                    // Intercepting back-press event but doing nothing.
                }
            }
            composable(
                route = OnboardingNavigation.enterTheVoid,
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                currentPage.value = OnboardingPage.ENTER_THE_VOID
                enterTheVoid(navController)
            }
        }
    }

    @Composable
    private fun ContentPaddingTop() = LocalConfiguration.current.screenHeightDp * 2 / 6

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Recovery(navController: NavHostController) {
        val component = componentManager().onboardingMnemonicLoginComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        val isQrWarningDialogVisible = remember { mutableStateOf(false) }

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
                        val lastDestination = navController.backQueue.lastOrNull()
                        // TODO Temporary workaround to prevent inconsistent state in navigation
                        if (lastDestination?.destination?.route == OnboardingNavigation.recovery) {
                            navController.popBackStack()
                        }
                    }

                    is OnboardingMnemonicLoginViewModel.SideEffect.ProceedWithLogin -> {
                        navController.navigate(OnboardingNavigation.enterTheVoid)
                    }

                    is OnboardingMnemonicLoginViewModel.SideEffect.Error -> {
                        toast(effect.msg)
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
    private fun enterTheVoid(
        navController: NavHostController
    ) {
        val component = componentManager().onboardingLoginSetupComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        EnteringTheVoidScreen(
            error = vm.error.collectAsState().value,
            contentPaddingTop = ContentPaddingTop(),
            onSystemBackPressed = vm::onSystemBackPressed
        )
        LaunchedEffect(Unit) {
            vm.navigation.collect { navigation ->
                when (navigation) {
                    OnboardingLoginSetupViewModel.Navigation.Exit -> {
                        navController.popBackStack()
                    }

                    OnboardingLoginSetupViewModel.Navigation.NavigateToHomeScreen -> {
                        findNavController().navigate(R.id.action_openHome)
                    }

                    OnboardingLoginSetupViewModel.Navigation.NavigateToMigrationErrorScreen -> {
                        findNavController().navigate(R.id.migrationNeededScreen, null, navOptions {
                            popUpTo(R.id.onboarding_nav) {
                                inclusive = false
                            }
                        })
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.toasts.collect { toast(it) }
        }
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }

    @Composable
    private fun CreateSoulAnimation(contentPaddingTop: Int) {
        val component = componentManager().onboardingSoulCreationAnimComponent
        CreateSoulAnimWrapper(
            contentPaddingTop = contentPaddingTop,
            viewModel = daggerViewModel { component.get().getViewModel() },
            onAnimationComplete = {
                findNavController().navigate(R.id.action_openHome)
            }
        )
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }


    @Composable
    private fun CreateSoul(navController: NavHostController, contentPaddingTop: Int) {
        val component = componentManager().onboardingSoulCreationComponent
        val vm = daggerViewModel { component.get().getViewModel() }

        val focusManager = LocalFocusManager.current
        val keyboardInsets = WindowInsets.ime
        val density = LocalDensity.current

        CreateSoulWrapper(vm, contentPaddingTop)

        LaunchedEffect(Unit) {
            vm.navigationFlow.collect { command ->
                when (command) {
                    is OnboardingSoulCreationViewModel.Navigation.OpenSoulCreationAnim -> {
                        if (keyboardInsets.getBottom(density) > 0) {
                            focusManager.clearFocus(force = true)
                            delay(KEYBOARD_HIDE_DELAY)
                        }
                        navController.navigate(
                            route = OnboardingNavigation.createSoulAnim
                        )
                        vm.sendAnalyticsOnboardingScreen()
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
        navController: NavHostController,
        contentPaddingTop: Int,
        mnemonicColorPalette: List<Color>
    ) {
        val component = componentManager().onboardingMnemonicComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        MnemonicPhraseScreenWrapper(
            contentPaddingTop = contentPaddingTop,
            viewModel = vm,
            openSoulCreation = {
                navController.navigate(OnboardingNavigation.createSoul)
                vm.sendAnalyticsOnboardingScreen()
            },
            copyMnemonicToClipboard = ::copyMnemonicToClipboard,
            vm = vm,
            mnemonicColorPalette = mnemonicColorPalette
        )
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }

    private fun copyMnemonicToClipboard(mnemonicPhrase: String) {
        try {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip =
                ClipData.newPlainText("Mnemonic phrase", mnemonicPhrase)
            clipboard.setPrimaryClip(clip)
            toast("Mnemonic phrase copied")
        } catch (e: Exception) {
            toast("Could not copy your mnemonic phrase. Please try again later, or copy it manually.")
        }
    }

    @Composable
    private fun Auth(navController: NavHostController) {
        val component = componentManager().onboardingStartComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        AuthScreenWrapper(vm = vm)
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
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.navigation.collect { navigation ->
                when (navigation) {
                    is OnboardingStartViewModel.AuthNavigation.ProceedWithSignUp -> {
                        navController.navigate(OnboardingNavigation.void)
                        vm.sendAnalyticsOnboardingScreen()
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

    private fun getVideoPlayer(context: Context, videoPath: Uri): Player {
        val player = ExoPlayer.Builder(context).build()
        val source = DefaultDataSource.Factory(
            context,
            DefaultHttpDataSource.Factory().setUserAgent(
                Util.getUserAgent(context, BuildConfig.LIBRARY_PACKAGE_NAME)
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
        componentManager().onboardingComponent.release()
        componentManager().logUnreleasedComponents()
    }
}

private const val ANIMATION_LENGTH_SLIDE = 300
private const val ANIMATION_LENGTH_FADE = 700
private const val KEYBOARD_HIDE_DELAY = 300L

typealias BackButtonCallback = (() -> Unit)?