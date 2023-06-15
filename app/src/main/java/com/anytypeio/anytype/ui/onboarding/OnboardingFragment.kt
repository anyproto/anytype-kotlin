package com.anytypeio.anytype.ui.onboarding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Right
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.ComponentManager
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.ui.onboarding.screens.AuthScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.CreateSoulAnimWrapper
import com.anytypeio.anytype.ui.onboarding.screens.CreateSoulWrapper
import com.anytypeio.anytype.ui.onboarding.screens.MnemonicPhraseScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.RecoveryScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.VoidScreenWrapper
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


class OnboardingFragment : BaseComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        val currentPage = remember { mutableStateOf(Page.AUTH) }
                        BackgroundCircle()
                        PagerIndicator(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            pageCount = Page.values().filter { it.visible }.size,
                            page = currentPage
                        )
                        Onboarding(currentPage)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun Onboarding(currentPage: MutableState<Page>) {
        val navController = rememberAnimatedNavController()
        AnimatedNavHost(navController, startDestination = OnboardingNavigation.auth) {
            composable(
                route = OnboardingNavigation.auth,
                enterTransition = { null },
                exitTransition = {
                    fadeOut(tween(150))
                }
            ) {
                currentPage.value = Page.AUTH
                Auth(navController)
            }
            composable(
                route = OnboardingNavigation.recovery,
                enterTransition = {
                    when (initialState.destination.route) {
                        OnboardingNavigation.inviteCode -> {
                            slideIntoContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }
                        else -> {
                            slideIntoContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                },
                exitTransition = {
                    slideOutOfContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                }
            ) {
                RecoveryScreenWrapper()
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
                currentPage.value = Page.VOID
                VoidScreenWrapper(ContentPaddingTop()) {
                    navController.navigate(OnboardingNavigation.mnemonic)
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
                currentPage.value = Page.MNEMONIC
                Mnemonic(navController, ContentPaddingTop())
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
                currentPage.value = Page.SOUL_CREATION
                CreateSoul(navController, ContentPaddingTop())
            }
            composable(
                route = OnboardingNavigation.createSoulAnim,
                enterTransition = {
                    fadeIn(tween(ANIMATION_LENGTH_FADE))
                }
            ) {
                currentPage.value = Page.SOUL_CREATION_ANIM
                CreateSoulAnimation(ContentPaddingTop())
                BackHandler {
                    // do nothing
                }
            }
        }
    }

    @Composable
    private fun ContentPaddingTop(): Int {
        return LocalConfiguration.current.screenHeightDp * 2 / 6
    }

    @Composable
    private fun CreateSoulAnimation(contentPaddingTop: Int) {
        val component = componentManager().onboardingSoulCreationAnimComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )

        CreateSoulAnimWrapper(
            contentPaddingTop = contentPaddingTop,
            viewModel = daggerViewModel { component.get().getViewModel() }
        ) {
            findNavController().navigate(R.id.action_openHome)
        }
    }

    @Composable
    private fun CreateSoul(navController: NavHostController, contentPaddingTop: Int) {
        val component = componentManager().onboardingSoulCreationComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )
        val viewModel = daggerViewModel { component.get().getViewModel() }
        CreateSoulWrapper(viewModel, contentPaddingTop)
        val navigationCommands =
            viewModel.navigationFlow.collectAsState(
                initial = OnboardingSoulCreationViewModel.Navigation.Idle
            )
        LaunchedEffect(key1 = navigationCommands.value) {
            when (navigationCommands.value) {
                is OnboardingSoulCreationViewModel.Navigation.OpenSoulCreationAnim -> {
                    navController.navigate(
                        route = OnboardingNavigation.createSoulAnim
                    )
                }
                else -> {}
            }
        }
    }

    @Composable
    private fun Mnemonic(navController: NavHostController, contentPaddingTop: Int) {
        val component = componentManager().onboardingMnemonicComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )
        MnemonicPhraseScreenWrapper(
            contentPaddingTop = contentPaddingTop,
            viewModel = daggerViewModel { component.get().getViewModel() },
            openSoulCreation = {
                navController.navigate(OnboardingNavigation.createSoul)
            },
            copyMnemonicToClipboard = ::copyMnemonicToClipboard
        )
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
        val component = componentManager().onboardingAuthComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )
        val viewModel = daggerViewModel { component.get().getViewModel() }
        AuthScreenWrapper(
            viewModel = viewModel,
            navigateToLogin = {
                navController.navigate(OnboardingNavigation.recovery)
            }
        )
        val navigationCommands = viewModel.navigationFlow.collectAsState(
            initial = OnboardingAuthViewModel.InviteCodeNavigation.Idle
        )
        LaunchedEffect(key1 = navigationCommands.value) {
            when (navigationCommands.value) {
                is OnboardingAuthViewModel.InviteCodeNavigation.Void -> {
                    navController.navigate(OnboardingNavigation.void)
                }
                else -> {

                }
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

    override fun injectDependencies() {}

    override fun releaseDependencies() {}

}

@Composable
fun <T> ComponentManager.Component<T>.ReleaseOn(
    viewLifecycleOwner: LifecycleOwner,
    state: Lifecycle.State
): ComponentManager.Component<T> {
    val that = this
    DisposableEffect(
        key1 = viewLifecycleOwner.lifecycle.currentState.isAtLeast(state),
        effect = {
            onDispose {
                if (viewLifecycleOwner.lifecycle.currentState == state) {
                    that.release()
                }
            }
        }
    )
    return that
}

private const val ANIMATION_LENGTH_SLIDE = 700
private const val ANIMATION_LENGTH_FADE = 700