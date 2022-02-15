package com.anytypeio.anytype.ui.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.databinding.FragmentSetupSelectedAccountBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.ui.auth.Keys
import com.anytypeio.anytype.ui.base.NavigationFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

open class SetupSelectedAccountFragment : NavigationFragment<FragmentSetupSelectedAccountBinding>(R.layout.fragment_setup_selected_account) {

    @Inject
    lateinit var factory: SetupSelectedAccountViewModelFactory

    private val rotationAnimation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotation)
    }

    private val blinkingAnimation by lazy {
        AlphaAnimation(0f, 1f).apply {
            duration = BLINKING_ANIMATION_DURATION
            repeatMode = AlphaAnimation.REVERSE
            repeatCount = AlphaAnimation.INFINITE
        }
    }

    private val errorObserver = Observer<String> {
        binding.tvError.visible()
        binding.tvError.text = it
        rotationAnimation.cancel()
        blinkingAnimation.cancel()
        binding.tvMigrationInProgress.gone()
    }

    private val vm : SetupSelectedAccountViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.selectAccount(arguments?.getString(Keys.SELECTED_ACCOUNT_ID_KEY) ?: throw IllegalStateException())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.icon.startAnimation(rotationAnimation)
        subscribe()
    }

    private fun subscribe() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
        vm.error.observe(viewLifecycleOwner, errorObserver)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.isMigrationInProgress.collect { isInProgress ->
                    if (isInProgress) {
                        binding.tvMigrationInProgress.visible()
                        binding.tvMigrationInProgress.startAnimation(blinkingAnimation)
                    }
                    else {
                        binding.tvMigrationInProgress.invisible()
                    }
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().setupSelectedAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupSelectedAccountComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetupSelectedAccountBinding = FragmentSetupSelectedAccountBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val BLINKING_ANIMATION_DURATION = 1000L
    }
}