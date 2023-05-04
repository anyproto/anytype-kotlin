package com.anytypeio.anytype.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.DefaultAppActionManager.Companion.ACTION_CREATE_NEW_TYPE_KEY
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentSplashBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import com.anytypeio.anytype.presentation.splash.SplashViewModelFactory
import com.anytypeio.anytype.ui.editor.EditorFragment
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
                vm.commands.collect {
                    try {
                        observe(it)
                    } catch (e: Exception) {
                        toast(e.message ?: resources.getString(R.string.unknown_error))
                    }
                }
            }
        }
    }

    private fun observe(command: SplashViewModel.Command) {
        when (command) {
            is SplashViewModel.Command.Error -> {
                toast(command.msg)
                binding.error.visible()
            }
            SplashViewModel.Command.NavigateToDashboard -> {
                try {
                    findNavController().navigate(
                        R.id.action_splashScreen_to_homeScreen
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error while opening dashboard from splash screen")
                    toast("Error while navigating to desktop: ${e.message}")
                }
            }
            SplashViewModel.Command.NavigateToWidgets -> {
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
                findNavController().navigate(
                    R.id.action_splashScreen_to_objectScreen,
                    bundleOf(EditorFragment.ID_KEY to command.id),
                )
            }
            is SplashViewModel.Command.NavigateToObjectSet -> {
                findNavController().navigate(
                    R.id.action_splashScreen_to_objectSetScreen,
                    bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to command.id),
                )
            }
            is SplashViewModel.Command.NavigateToLogin -> {
                findNavController().navigate(
                    R.id.action_splashFragment_to_login_nav
                )
            }
            is SplashViewModel.Command.NavigateToAuthStart -> {
                findNavController().navigate(
                    R.id.action_splashFragment_to_authStart
                )
            }
            is SplashViewModel.Command.NavigateToMigration -> {
                findNavController().navigate(
                    R.id.migrationNeededScreen
                )
            }
            is SplashViewModel.Command.CheckAppStartIntent -> {
                val intent = requireActivity().intent
                if (intent != null && intent.action == Intent.ACTION_VIEW) {
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
                else {
                    vm.onIntentActionNotFound()
                }
            }
        }
    }

    private fun showVersion() {
        binding.version.text = "${BuildConfig.VERSION_NAME}-beta"
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