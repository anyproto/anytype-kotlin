package com.anytypeio.anytype.ui.onboarding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.ComponentManager
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.ui.onboarding.screens.AuthScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.CreateSoulAnimWrapper
import com.anytypeio.anytype.ui.onboarding.screens.CreateSoulWrapper
import com.anytypeio.anytype.ui.onboarding.screens.InviteCodeScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.MnemonicPhraseScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.RecoveryScreenWrapper
import com.anytypeio.anytype.ui.onboarding.screens.VoidScreenWrapper
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController


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
                route = OnboardingNavigation.inviteCode,
                enterTransition = {
                    when (initialState.destination.route) {
                        OnboardingNavigation.void -> {
                            slideIntoContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                        else -> null
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
                currentPage.value = Page.INVITE_CODE
                InviteCode(navController)
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
                        OnboardingNavigation.inviteCode -> {
                            slideIntoContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }
                        else -> {
                            slideIntoContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        OnboardingNavigation.mnemonic -> {
                            slideOutOfContainer(Left, tween(ANIMATION_LENGTH_SLIDE))
                        }
                        else -> {
                            slideOutOfContainer(Right, tween(ANIMATION_LENGTH_SLIDE))
                        }
                    }
                }
            ) {
                currentPage.value = Page.VOID
                VoidScreenWrapper {
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
                exitTransition = { fadeOut(tween(ANIMATION_LENGTH_SLIDE)) }
            ) {
                currentPage.value = Page.MNEMONIC
                Mnemonic(navController)
            }
            composable(
                route = OnboardingNavigation.createSoul,
                enterTransition = { slideIntoContainer(Left, tween(ANIMATION_LENGTH_SLIDE)) },
                exitTransition = { fadeOut(tween(ANIMATION_LENGTH_FADE)) }
            ) {
                currentPage.value = Page.SOUL_CREATION
                CreateSoul(navController)
            }
            composable(
                route = OnboardingNavigation.createSoulAnim,
                enterTransition = { fadeIn(tween(ANIMATION_LENGTH_FADE)) }
            ) {
                currentPage.value = Page.SOUL_CREATION_ANIM
                CreateSoulAnimation()
                BackHandler {
                    // do nothing
                }
            }
        }
    }

    @Composable
    private fun CreateSoulAnimation() {
        val component = componentManager().onboardingSoulCreationAnimComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )

        CreateSoulAnimWrapper(
            viewModel = daggerViewModel { component.get().getViewModel() }
        ) {
            findNavController().navigate(R.id.action_openHome)
        }
    }

    @Composable
    private fun CreateSoul(navController: NavHostController) {
        val component = componentManager().onboardingSoulCreationComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )
        val viewModel = daggerViewModel { component.get().getViewModel() }
        CreateSoulWrapper(viewModel)
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
    private fun Mnemonic(navController: NavHostController) {
        val component = componentManager().onboardingMnemonicComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )
        MnemonicPhraseScreenWrapper(
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
    private fun InviteCode(navController: NavHostController) {
        val component = componentManager().onboardingInviteCodeComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )

        val viewModel: OnboardingInviteCodeViewModel = daggerViewModel {
            component.get().getViewModel()
        }

        InviteCodeScreenWrapper(viewModel = viewModel)
        val navigationCommands =
            viewModel.navigationFlow.collectAsState(
                initial = OnboardingInviteCodeViewModel.InviteCodeNavigation.Idle
            )
        LaunchedEffect(key1 = navigationCommands.value) {
            when (navigationCommands.value) {
                is OnboardingInviteCodeViewModel.InviteCodeNavigation.Void -> {
                    navController.navigate(OnboardingNavigation.void)
                }
                else -> {

                }
            }
        }
    }

    @Composable
    private fun Auth(navController: NavHostController) {
        val component = componentManager().onboardingAuthComponent.ReleaseOn(
            viewLifecycleOwner = viewLifecycleOwner,
            state = Lifecycle.State.DESTROYED
        )
        AuthScreenWrapper(
            viewModel = daggerViewModel { component.get().getViewModel() },
            navigateToInviteCode = {
                navController.navigate(OnboardingNavigation.inviteCode)
            },
            navigateToLogin = {
                navController.navigate(OnboardingNavigation.recovery)
            }
        )
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