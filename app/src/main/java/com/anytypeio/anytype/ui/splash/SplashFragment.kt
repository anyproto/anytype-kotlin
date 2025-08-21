package com.anytypeio.anytype.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.DefaultAppActionManager.Companion.ACTION_CREATE_NEW_TYPE_KEY
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentSplashBinding
import com.anytypeio.anytype.device.AnytypePushService
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import com.anytypeio.anytype.presentation.splash.SplashViewModelFactory
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.onboarding.OnboardingFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.update.MigrationFailedScreen
import com.anytypeio.anytype.ui.update.MigrationInProgressScreen
import com.anytypeio.anytype.ui.update.MigrationStartScreen
import com.anytypeio.anytype.ui.vault.VaultFragment
import javax.inject.Inject
import kotlin.collections.plusAssign
import timber.log.Timber

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashFragment : BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    @Inject
    lateinit var factory: SplashViewModelFactory
    private val vm by viewModels<SplashViewModel> { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showVersion()
        if (BuildConfig.DEBUG) {
            binding.error.setOnClickListener {
                vm.onErrorClicked()
            }
        }
    }

    override fun onStart() {
        jobs += subscribe(vm.state) {
            Timber.d("Splash state: $it")
            handleState(it)
        }
        jobs += subscribe(vm.commands) {
            Timber.d("Splash command: $it")
            handleCommand(it)
        }
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun handleState(state: SplashViewModel.State) = with(binding) {
        when (state) {
            is SplashViewModel.State.Init -> {
                error.gone()
                compose.gone()
            }

            is SplashViewModel.State.Error -> {
                error.text = state.msg
                error.visible()
                compose.gone()
            }

            is SplashViewModel.State.Loading -> {
                showCompose {
                    PulsatingCircleScreen()
                }
                error.gone()
            }

            is SplashViewModel.State.Migration -> {
                showCompose {
                    when (state) {
                        is SplashViewModel.State.Migration.AwaitingStart -> {
                            MigrationStartScreen(onStartUpdate = vm::onStartMigrationClicked)
                        }

                        is SplashViewModel.State.Migration.InProgress -> {
                            MigrationInProgressScreen(progress = state.progress)
                        }

                        is SplashViewModel.State.Migration.Failed -> {
                            MigrationFailedScreen(
                                state = state.state,
                                onRetryClicked = vm::onRetryMigrationClicked
                            )
                        }
                    }
                }
                error.gone()
            }

            is SplashViewModel.State.Success -> {
                error.gone()
                compose.gone()
                error.text = ""
            }
        }
    }

    private fun showCompose(content: @Composable () -> Unit) = with(binding) {
        compose.setContent {
            content()
        }
        compose.visible()
    }

    private fun handleCommand(command: SplashViewModel.Command) {
        val nav = findNavController()
        when (command) {
            is SplashViewModel.Command.NavigateToWidgets -> {
                nav.safeNavigateOrLog(
                    id = R.id.homeScreen,
                    args = HomeScreenFragment.args(
                        deeplink = command.deeplink,
                        space = command.space
                    ),
                    errorTag = "Widgets Screen from Splash Screen",
                )
            }

            is SplashViewModel.Command.NavigateToChat -> {
                nav.safeNavigateOrLog(
                    id = R.id.chatScreen,
                    args = ChatFragment.args(
                        space = command.space,
                        ctx = command.chat
                    ),
                    errorTag = "Chat Screen from Splash Screen",
                )
            }

            is SplashViewModel.Command.NavigateToVault -> {
                nav.safeNavigateOrLog(
                    id = R.id.actionOpenVaultFromSplash,
                    args = VaultFragment.args(deeplink = command.deeplink),
                    errorTag = "Vault Screen from Splash Screen",
                )
            }

            is SplashViewModel.Command.NavigateToObject -> {
                navigateToHomeOrChatThen(
                    nav = nav,
                    chat = command.chat,
                    space = command.space
                ) {
                    nav.safeNavigateOrLog(
                        id = R.id.objectNavigation,
                        args = EditorFragment.args(
                            ctx = command.id,
                            space = command.space
                        ),
                        errorTag = "object from splash",
                    )
                }
            }

            is SplashViewModel.Command.NavigateToObjectType -> {
                navigateToHomeOrChatThen(
                    nav = nav,
                    chat = command.chat,
                    space = command.space
                ) {
                    nav.safeNavigateOrLog(
                        id = R.id.objectTypeNavigation,
                        args = ObjectTypeFragment.args(
                            objectId = command.id,
                            space = command.space
                        ),
                        errorTag = "object type from splash",
                    )
                }
            }

            is SplashViewModel.Command.NavigateToObjectSet -> {
                navigateToHomeOrChatThen(
                    nav = nav,
                    chat = command.chat,
                    space = command.space
                ) {
                    nav.safeNavigateOrLog(
                        id = R.id.dataViewNavigation,
                        args = ObjectSetFragment.args(
                            ctx = command.id,
                            space = command.space
                        ),
                        errorTag = "set-or-collection from splash",
                    )
                }
            }

            is SplashViewModel.Command.NavigateToDateObject -> {
                navigateToHomeOrChatThen(
                    nav = nav,
                    chat = command.chat,
                    space = command.space
                ) {
                    nav.safeNavigateOrLog(
                        id = R.id.dateObjectScreen,
                        args = DateObjectFragment.args(
                            objectId = command.id,
                            space = command.space
                        ),
                        errorTag = "date object from splash",
                    )
                }
            }

            is SplashViewModel.Command.NavigateToAuthStart -> {
                val intent = activity?.intent
                var deepLink: String? = null
                if (intent != null && (intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_SEND)) {
                    val data = intent.dataString
                    deepLink = if (data != null && DefaultDeepLinkResolver.isDeepLink(data)) {
                        data
                    } else {
                        intent.extras?.getString(Intent.EXTRA_TEXT)
                    }
                }
                if (!deepLink.isNullOrEmpty()) {
                    with(requireNotNull(intent)) {
                        setAction(null)
                        setData(null)
                        putExtras(Bundle())
                    }
                }
                nav.safeNavigateOrLog(
                    id = R.id.action_splashFragment_to_authStart,
                    args = OnboardingFragment.args(deepLink),
                    errorTag = "AuthStart navigation",
                )
            }

            is SplashViewModel.Command.CheckAppStartIntent -> {
                val intent = activity?.intent
                Timber.d("Checking app start intent: $intent, action: ${intent?.action}")
                when {
                    intent != null && (intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_SEND) -> {
                        val data = intent.dataString.orNull()
                            ?: intent.extras?.getString(Intent.EXTRA_TEXT)
                        if (data != null && DefaultDeepLinkResolver.isDeepLink(data)) {
                            with(intent) {
                                setAction(null)
                                setData(null)
                                putExtras(Bundle())
                            }
                            vm.onDeepLinkLaunch(data)
                        } else {
                            val bundle = intent.extras
                            if (bundle != null) {
                                val type = bundle.getString(ACTION_CREATE_NEW_TYPE_KEY)
                                if (type != null) {
                                    vm.onIntentCreateNewObject(type = type)
                                } else {
                                    vm.onIntentActionNotFound()
                                }
                            } else {
                                vm.onIntentActionNotFound()
                            }
                        }
                    }

                    intent?.action == AnytypePushService.ACTION_OPEN_CHAT -> {
                        val chatId = intent.getStringExtra(Relations.CHAT_ID)
                        val spaceId = intent.getStringExtra(Relations.SPACE_ID)
                        if (!chatId.isNullOrEmpty() && !spaceId.isNullOrEmpty()) {
                            vm.onIntentTriggeredByChatPush(
                                space = spaceId,
                                chat = chatId
                            )
                        } else {
                            vm.onIntentActionNotFound()
                        }
                    }

                    else -> {
                        vm.onIntentActionNotFound()
                    }
                }
            }

            is SplashViewModel.Command.Toast -> {
                toast(command.message)
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

    private fun showVersion() {
        binding.version.text = getVersionText()
    }

    private fun getVersionText(): String {
        return if (BuildConfig.DEBUG)
            "${BuildConfig.VERSION_NAME}-debug"
        else
            BuildConfig.VERSION_NAME
    }

    override fun injectDependencies() {
        componentManager().splashLoginComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().splashLoginComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)
}