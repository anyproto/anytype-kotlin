package com.anytypeio.anytype.ui.onboarding

import android.app.Activity
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
import android.view.ViewGroup
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
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
import com.anytypeio.anytype.core_ui.MNEMONIC_WORD_COUNT
import com.anytypeio.anytype.core_ui.MnemonicPhrasePaletteColors
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.shareFirstFileFromPath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel.SideEffect
import com.anytypeio.anytype.presentation.onboarding.OnboardingViewModel
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingEmailAndSelectionViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingMnemonicViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.anytypeio.anytype.ui.onboarding.screens.AuthScreen
import com.anytypeio.anytype.ui.onboarding.screens.signin.RecoveryScreen
import com.anytypeio.anytype.ui.onboarding.screens.signup.MnemonicPhraseScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.OnboardingSelectionScreen
import com.anytypeio.anytype.ui.onboarding.screens.signup.OnboardingUsecaseScreen
import com.anytypeio.anytype.ui.onboarding.screens.signup.SetEmailWrapper
import com.anytypeio.anytype.ui.onboarding.screens.signup.SetProfileNameWrapper
import com.anytypeio.anytype.ui.qrcode.QrScannerActivity
import com.anytypeio.anytype.ui.vault.VaultFragment
import javax.inject.Inject
import kotlinx.coroutines.delay
import timber.log.Timber

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
    ) = content {
        OnboardingScreen()
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
                    .background(colorResource(R.color.background_primary))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(insets = WindowInsets.systemBars)
                ) {
                    val currentPage = remember { mutableStateOf(OnboardingPage.AUTH) }
                    //BackgroundCircle()
                    Onboarding(
                        currentPage = currentPage,
                        navController = navController,
                        backButtonCallback = signUpBackButtonCallback
                    )
                }
            }
            LaunchedEffect(Unit) {
                onBoardingViewModel.toasts.collect {
                    toast(it)
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    signUpBackButtonCallback.value = null
                }
            }
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
                route = buildOnboardingRoute(OnboardingNavigation.mnemonic),
                arguments = onboardingArguments(),
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
                val (spaceId, startingObjectId, profileId) = it.arguments.extractOnboardingParams()
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
                route = buildOnboardingRoute(OnboardingNavigation.setProfileName),
                arguments = onboardingArguments(),
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                val focus = LocalFocusManager.current
                val (spaceId, startingObjectId, profileId) = it.arguments.extractOnboardingParams()
                val onBackClicked : () -> Unit = {
                    val lastDestination = navController.currentBackStackEntry
                    if (lastDestination?.destination?.route?.startsWith(OnboardingNavigation.setProfileName) == true) {
                        focus.clearFocus(true)
                        navController.popBackStack()
                    }  else {
                        Timber.d("Skipping exit click...")
                    }
                }
                currentPage.value = OnboardingPage.SET_PROFILE_NAME
                backButtonCallback.value = onBackClicked
                if (!spaceId.isNullOrEmpty() && !profileId.isNullOrEmpty()) {
                    SetProfileName(
                        navController = navController,
                        spaceId = spaceId,
                        startingObjectId = startingObjectId,
                        profileId = profileId,
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
            composable(
                route = buildOnboardingRoute(OnboardingNavigation.setEmail),
                arguments = onboardingArguments(),
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                val focus = LocalFocusManager.current
                val (spaceId, startingObjectId, profileId) = it.arguments.extractOnboardingParams()
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
                        navController = navController,
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
            composable(
                route = buildOnboardingRoute(OnboardingNavigation.selection),
                arguments = onboardingArguments(),
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                currentPage.value = OnboardingPage.SELECTION
                val (spaceId, startingObjectId, _) = it.arguments.extractOnboardingParams()
                val onBackClicked : () -> Unit = {
                    val lastDestination = navController.currentBackStackEntry
                    if (lastDestination?.destination?.route?.startsWith(OnboardingNavigation.selection) == true) {
                        navController.popBackStack()
                    } else {
                        Timber.d("Skipping exit click...")
                    }
                }
                backButtonCallback.value = onBackClicked
                if (!spaceId.isNullOrEmpty()) {
                    Selection(
                        navController = navController,
                        spaceId = spaceId,
                        startingObjectId = startingObjectId,
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
            composable(
                route = buildOnboardingRoute(OnboardingNavigation.usecase),
                arguments = onboardingArguments(),
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                },
                exitTransition = {
                    fadeOut(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                currentPage.value = OnboardingPage.USECASE
                val (spaceId, startingObjectId, _) = it.arguments.extractOnboardingParams()
                val onBackClicked : () -> Unit = {
                    val lastDestination = navController.currentBackStackEntry
                    if (lastDestination?.destination?.route?.startsWith(OnboardingNavigation.usecase) == true) {
                        navController.popBackStack()
                    } else {
                        Timber.d("Skipping exit click...")
                    }
                }
                backButtonCallback.value = onBackClicked
                if (!spaceId.isNullOrEmpty()) {
                    Usecase(
                        navController = navController,
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
        val errorText = remember { mutableStateOf(NO_VALUE) }
        val isErrorDialogVisible = remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(QrScannerActivity.SCAN_RESULT)?.let { qrCode ->
                    vm.onGetEntropyFromQRCode(entropy = qrCode)
                }
            }
        }

        RecoveryScreen(
            onBackClicked = vm::onBackButtonPressed,
            onNextClicked = vm::onLoginClicked,
            onActionDoneClicked = vm::onActionDone,
            onScanQrClicked = {
                proceedWithQrCodeActivity(launcher)
                vm.onScanQrCodeClicked()
            },
            state = vm.state.collectAsStateWithLifecycle().value,
            onEnterMyVaultClicked = vm::onEnterMyVaultClicked,
            onDebugAccountTraceClicked = {
                vm.onAccountThraceButtonClicked()
            },
            onRetryMigrationClicked = vm::onRetryMigrationClicked,
            onStartMigrationClicked = vm::onStartMigrationClicked
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
                Intent(requireContext(), QrScannerActivity::class.java)
            )
        } catch (e: Exception) {
            toast("Error while scanning QR code")
            Timber.e(e, "Error while scanning QR code")
        }
    }

    @Composable
    private fun SetProfileName(
        navController: NavHostController,
        spaceId: String,
        startingObjectId: String?,
        profileId: String,
        onBackClicked: () -> Unit
    ) {
        val component = componentManager().onboardingSoulCreationComponent
        val vm = daggerViewModel { component.get().getViewModel() }

        val focusManager = LocalFocusManager.current
        val keyboardInsets = WindowInsets.ime
        val density = LocalDensity.current

        SetProfileNameWrapper(
            viewModel = vm,
            spaceId = spaceId,
            startingObjectId = startingObjectId,
            profileId = profileId,
            onBackClicked = onBackClicked
        )

        LaunchedEffect(Unit) {
            vm.navigation.collect { command ->
                Timber.d("Navigation command: $command")
                when (command) {
                    is OnboardingSetProfileNameViewModel.Navigation.NavigateToSetEmail -> {
                        if (keyboardInsets.getBottom(density) > 0) {
                            focusManager.clearFocus(force = true)
                            delay(KEYBOARD_HIDE_DELAY)
                        }
                        val route = buildSelectProfessionRoute(
                            spaceId = command.spaceId,
                            startingObjectId = command.startingObjectId,
                        )
                        navController.navigate(route)
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
            copyMnemonicToClipboard = ::copyMnemonicToClipboard,
            vm = vm,
            mnemonicColorPalette = mnemonicColorPalette,
            onBackClicked = { navController.popBackStack() }
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
                                WidgetsScreenFragment.args(
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
                    is OnboardingMnemonicViewModel.Command.NavigateToSetProfileName -> {
                    }
                    is OnboardingMnemonicViewModel.Command.NavigateToAddEmailScreen -> {
                        val route = buildSetEmailRoute(
                            spaceId = command.space,
                            startingObjectId = command.startingObject
                        )
                        navController.navigate(route)
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
    private fun Selection(
        navController: NavHostController,
        spaceId: String,
        startingObjectId: String?,
        onBackClicked: () -> Unit
    ) {
        val component = componentManager().onboardingSoulCreationComponent
        val vm = daggerViewModel { component.get().getEmailAndSelectionViewModel() }

        LaunchedEffect(Unit) {
            vm.navigation.collect { command ->
                when (command){
                    OnboardingEmailAndSelectionViewModel.Navigation.GoBack -> {
                        navController.popBackStack()
                    }
                    is OnboardingEmailAndSelectionViewModel.Navigation.NavigateToUsecase -> {
                        val route = buildUsecaseRoute(
                            spaceId = command.spaceId,
                            startingObjectId = command.startingObjectId
                        )
                        navController.navigate(route)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }

        OnboardingSelectionScreen(
            vm = vm,
            isLoading = false,
            onBackClicked = onBackClicked,
            onContinueClicked = { selectedProfession ->
                vm.onSelectionContinueClicked(
                    professionItem = selectedProfession,
                    spaceId = spaceId,
                    startingObjectId = startingObjectId
                )
            },
            onSkipClicked = {
                vm.onSelectionSkipClicked(spaceId, startingObjectId)
            }
        )
    }

    @Composable
    private fun Usecase(
        navController: NavHostController,
        space: Id,
        startingObject: Id?,
        onBackClicked: () -> Unit
    ) {
        val component = componentManager().onboardingSoulCreationComponent
        val vm = daggerViewModel { component.get().getEmailAndSelectionViewModel() }

        LaunchedEffect(Unit) {
            vm.navigation.collect { command ->
                when (command){
                    OnboardingEmailAndSelectionViewModel.Navigation.GoBack -> {
                        navController.popBackStack()
                    }
                    is OnboardingEmailAndSelectionViewModel.Navigation.OpenStartingObject -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                            findNavController().navigate(
                                R.id.actionOpenSpaceFromVault,
                                WidgetsScreenFragment.args(
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
                    OnboardingEmailAndSelectionViewModel.Navigation.OpenVault -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                        }.onFailure {
                            Timber.e(it, "Error while navigation to vault")
                        }
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }

        OnboardingUsecaseScreen(
            vm = vm,
            isLoading = false,
            onBackClicked = onBackClicked,
            onContinueClicked = { selectedUsecase ->
                vm.onUsecaseContinueClicked(selectedUsecase, space = space, startingObject = startingObject)
            },
            onSkipClicked = {
                vm.onUsecaseSkipClicked(space = space, startingObject = startingObject)
            }
        )
    }

    @Composable
    private fun Auth(navController: NavHostController) {
        val component = componentManager().onboardingStartComponent
        val vm = daggerViewModel { component.get().getViewModel() }
        AuthScreen(
            isLoading = vm.isLoadingState.collectAsStateWithLifecycle().value,
            onJoinClicked = vm::onJoinClicked,
            onLoginClicked = vm::onLoginClicked,
            onPrivacyPolicyClicked = vm::onPrivacyPolicyClicked,
            onTermsOfUseClicked = vm::onTermsOfUseClicked,
            onSettingsClicked = vm::onSettingsClicked
        )
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
                        val route = buildMnemonicRoute(
                            spaceId = navigation.spaceId,
                            startingObjectId = navigation.startingObjectId
                        )
                        navController.navigate(route)
                    }

                    is OnboardingStartViewModel.AuthNavigation.ProceedWithSignIn -> {
                        navController.navigate(OnboardingNavigation.recovery)
                    }
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose { vm.cancelLoadingState() }
        }
        DisposableEffect(Unit) {
            onDispose { component.release() }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen(vm: OnboardingStartViewModel) {
        val errorState = vm.errorState.collectAsStateWithLifecycle().value
        
        val errorMessage = when (errorState) {
            OnboardingStartViewModel.ErrorState.Hidden -> null
            is OnboardingStartViewModel.ErrorState.Generic -> errorState.message
            is OnboardingStartViewModel.ErrorState.WalletSetupError -> stringResource(id = R.string.error_wallet_setup)
            OnboardingStartViewModel.ErrorState.NetworkError -> stringResource(id = R.string.onboarding_error_network_error)
            OnboardingStartViewModel.ErrorState.OfflineDevice -> stringResource(id = R.string.onboarding_error_offline_device)
            OnboardingStartViewModel.ErrorState.AccountCreatedButFailedToStartNode -> stringResource(id = R.string.onboarding_error_account_created_but_failed_to_start_node)
            OnboardingStartViewModel.ErrorState.AccountCreatedButFailedToSetName -> stringResource(id = R.string.onboarding_error_account_created_but_failed_to_set_name)
            OnboardingStartViewModel.ErrorState.FailedToStopRunningNode -> stringResource(id = R.string.onboarding_error_failed_to_stop_running_node)
            OnboardingStartViewModel.ErrorState.FailedToWriteConfig -> stringResource(id = R.string.onboarding_error_failed_to_write_config)
            OnboardingStartViewModel.ErrorState.FailedToCreateLocalRepo -> stringResource(id = R.string.onboarding_error_failed_to_create_local_repo)
            OnboardingStartViewModel.ErrorState.AccountCreationCanceled -> stringResource(id = R.string.onboarding_error_account_creation_canceled)
            OnboardingStartViewModel.ErrorState.ConfigFileNotFound -> stringResource(id = R.string.onboarding_error_config_file_not_found)
            OnboardingStartViewModel.ErrorState.ConfigFileInvalid -> stringResource(id = R.string.onboarding_error_config_file_invalid)
            OnboardingStartViewModel.ErrorState.ConfigFileNetworkIdMismatch -> stringResource(id = R.string.onboarding_error_config_file_network_id_mismatch)
        }
        
        errorMessage?.let { message ->
            BaseAlertDialog(
                dialogText = message,
                buttonText = stringResource(id = R.string.button_ok),
                onButtonClick = vm::onErrorDismissed,
                onDismissRequest = vm::onErrorDismissed
            )
        }
    }

    @Composable
    private fun AddEmail(
        navController: NavHostController,
        space: Id,
        startingObject: Id?,
        onBackClicked: () -> Unit
    ) {
        val component = componentManager().onboardingSoulCreationComponent
        val vm = daggerViewModel { component.get().getEmailAndSelectionViewModel() }

        SetEmailWrapper(
            viewModel = vm,
            startingObject = startingObject,
            space = space,
            onBackClicked = onBackClicked
        )

        LaunchedEffect(Unit) {
            vm.navigation.collect { command ->
                when (command) {
                    is OnboardingEmailAndSelectionViewModel.Navigation.GoBack -> {
                        navController.popBackStack()
                    }

                    is OnboardingEmailAndSelectionViewModel.Navigation.OpenStartingObject -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                            findNavController().navigate(
                                R.id.actionOpenSpaceFromVault,
                                WidgetsScreenFragment.args(
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
                    OnboardingEmailAndSelectionViewModel.Navigation.OpenVault -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.actionOpenVault,
                                VaultFragment.args(deepLink)
                            )
                        }.onFailure {
                            Timber.e(it, "Error while navigation to vault")
                        }
                    }
                    is OnboardingEmailAndSelectionViewModel.Navigation.NavigateToSelection -> {
                        val route = buildSelectProfessionRoute(
                            spaceId = command.spaceId,
                            startingObjectId = command.startingObjectId
                        )
                        navController.navigate(route)
                    }
                    is OnboardingEmailAndSelectionViewModel.Navigation.NavigateToUsecase -> {
                        val route = buildUsecaseRoute(
                            spaceId = command.spaceId,
                            startingObjectId = command.startingObjectId
                        )
                        navController.navigate(route)
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
    }

    private fun buildMnemonicRoute(spaceId: Id, startingObjectId: Id?): String {
        return buildString {
            append("${OnboardingNavigation.mnemonic}?$ONBOARDING_SPACE_PARAM=$spaceId")
            startingObjectId?.let { 
                append("&$ONBOARDING_STARTING_OBJECT_PARAM=$it") 
            }
        }
    }

    private fun buildSetProfileNameRoute(spaceId: String, startingObjectId: String?, profileId: String): String {
        return buildString {
            append("${OnboardingNavigation.setProfileName}?$ONBOARDING_SPACE_PARAM=$spaceId")
            startingObjectId?.let { 
                append("&$ONBOARDING_STARTING_OBJECT_PARAM=$it") 
            }
            append("&$ONBOARDING_PROFILE_PARAM=$profileId")
        }
    }

    private fun buildSetEmailRoute(spaceId: String, startingObjectId: String?): String {
        return buildString {
            append("${OnboardingNavigation.setEmail}?$ONBOARDING_SPACE_PARAM=$spaceId")
            startingObjectId?.let { 
                append("&$ONBOARDING_STARTING_OBJECT_PARAM=$it") 
            }
        }
    }

    private fun buildSelectProfessionRoute(spaceId: String, startingObjectId: String?): String {
        return buildString {
            append("${OnboardingNavigation.selection}?$ONBOARDING_SPACE_PARAM=$spaceId")
            startingObjectId?.let {
                append("&$ONBOARDING_STARTING_OBJECT_PARAM=$it")
            }
        }
    }

    private fun buildUsecaseRoute(spaceId: String, startingObjectId: String?): String {
        return buildString {
            append("${OnboardingNavigation.usecase}?$ONBOARDING_SPACE_PARAM=$spaceId")
            startingObjectId?.let { 
                append("&$ONBOARDING_STARTING_OBJECT_PARAM=$it") 
            }
        }
    }

    private fun onboardingArguments() = listOf(
        navArgument(ONBOARDING_SPACE_PARAM) { type = NavType.StringType },
        navArgument(ONBOARDING_STARTING_OBJECT_PARAM) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        },
        navArgument(ONBOARDING_PROFILE_PARAM) { 
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
    
    private fun buildOnboardingRoute(baseRoute: String): String {
        return "$baseRoute?$ONBOARDING_SPACE_PARAM={$ONBOARDING_SPACE_PARAM}" +
            "&$ONBOARDING_STARTING_OBJECT_PARAM={$ONBOARDING_STARTING_OBJECT_PARAM}" +
            "&$ONBOARDING_PROFILE_PARAM={$ONBOARDING_PROFILE_PARAM}"
    }
    
    private data class OnboardingParams(
        val spaceId: String?,
        val startingObjectId: String?,
        val profileId: String?
    )
    
    private fun Bundle?.extractOnboardingParams(): OnboardingParams {
        return OnboardingParams(
            spaceId = this?.getString(ONBOARDING_SPACE_PARAM),
            startingObjectId = this?.getString(ONBOARDING_STARTING_OBJECT_PARAM),
            profileId = this?.getString(ONBOARDING_PROFILE_PARAM)
        )
    }

    companion object {
        fun args(deepLink: String?) = bundleOf(
            ONBOARDING_DEEP_LINK_KEY to deepLink
        )
        private const val ONBOARDING_DEEP_LINK_KEY = "arg.onboarding.deep-link-key"

        const val ONBOARDING_SPACE_PARAM = "space"
        const val ONBOARDING_STARTING_OBJECT_PARAM = "startingObject"
        const val ONBOARDING_PROFILE_PARAM = "profile"
    }
}

private const val ANIMATION_LENGTH_SLIDE = 300
private const val ANIMATION_LENGTH_FADE = 700
private const val KEYBOARD_HIDE_DELAY = 300L

typealias BackButtonCallback = (() -> Unit)?