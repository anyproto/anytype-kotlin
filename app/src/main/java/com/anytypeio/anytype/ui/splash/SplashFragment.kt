package com.anytypeio.anytype.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.DefaultAppActionManager.Companion.ACTION_CREATE_NEW_TYPE_KEY
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.databinding.FragmentSplashBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import com.anytypeio.anytype.presentation.splash.SplashViewModelFactory
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.onboarding.OnboardingFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import javax.inject.Inject
import kotlinx.coroutines.launch
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.commands.collect {
                        try {
                            observe(it)
                        } catch (e: Exception) {
                            toast(e.message ?: resources.getString(R.string.unknown_error))
                        }
                    }
                }
                launch {
                    vm.state.collect { state ->
                        when(state) {
                            is ViewState.Error -> {
                                binding.error.text = state.error
                                binding.error.visible()
                            }
                            else -> {
                                binding.error.gone()
                                binding.error.text = ""
                            }
                        }
                    }
                }
            }
        }
        if (BuildConfig.DEBUG) {
            binding.error.setOnClickListener {
                vm.onErrorClicked()
            }
        }
    }

    private fun observe(command: SplashViewModel.Command) {
        when (command) {
            is SplashViewModel.Command.NavigateToDashboard -> {
                try {
                    findNavController().navigate(
                        R.id.action_splashScreen_to_vaultScreen,
                        args = HomeScreenFragment.args(command.deeplink)
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error while opening dashboard from splash screen")
                    toast("Error while navigating to desktop: ${e.message}")
                }
            }
            is SplashViewModel.Command.NavigateToWidgets -> {
                try {
                    findNavController().navigate(
                        R.id.action_splashScreen_to_widgets
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error while opening widgets from splash screen")
                    toast("Error while navigating to widgets: ${e.message}")
                }
            }
            is SplashViewModel.Command.NavigateToObject -> {
                findNavController().navigate(R.id.action_splashScreen_to_widgets)
                findNavController().navigate(
                    R.id.objectNavigation,
                    EditorFragment.args(
                        ctx = command.id,
                        space = command.space
                    )
                )
            }
            is SplashViewModel.Command.NavigateToObjectSet -> {
                findNavController().navigate(R.id.action_splashScreen_to_widgets)
                findNavController().navigate(
                    R.id.dataViewNavigation,
                    ObjectSetFragment.args(
                        ctx = command.id,
                        space = command.space
                    )
                )
            }
            is SplashViewModel.Command.NavigateToAuthStart -> {
                val intent = activity?.intent
                val deepLink: String?
                if (intent != null && intent.action == Intent.ACTION_VIEW) {
                    val data = intent.dataString
                    deepLink = if (data != null && DefaultDeepLinkResolver.isDeepLink(data)) {
                        data
                    } else {
                        null
                    }
                } else {
                    deepLink = null
                }
                if (!deepLink.isNullOrEmpty()) {
                    // Clearing intent to only handle it once:
                    with(requireNotNull(intent)) {
                        setAction(null)
                        setData(null)
                        putExtras(Bundle())
                    }
                }
                Timber.d("Deep link is empty: ${deepLink.isNullOrEmpty()}")
                findNavController().navigate(
                    R.id.action_splashFragment_to_authStart,
                    args = OnboardingFragment.args(deepLink)
                )
            }
            is SplashViewModel.Command.NavigateToMigration -> {
                findNavController().navigate(
                    R.id.migrationNeededScreen
                )
            }
            is SplashViewModel.Command.CheckAppStartIntent -> {
                val intent = activity?.intent
                Timber.d("Timber intent: $intent")
                if (intent != null && intent.action == Intent.ACTION_VIEW) {
                    val data = intent.dataString
                    if (data != null && DefaultDeepLinkResolver.isDeepLink(data)) {
                        // Clearing intent to only handle it once:
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
                else {
                    vm.onIntentActionNotFound()
                }
            }
        }
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