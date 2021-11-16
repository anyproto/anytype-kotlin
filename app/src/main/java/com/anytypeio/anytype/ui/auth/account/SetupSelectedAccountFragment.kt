package com.anytypeio.anytype.ui.auth.account

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.ui.auth.Keys
import com.anytypeio.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_setup_selected_account.*
import javax.inject.Inject

open class SetupSelectedAccountFragment :
    NavigationFragment(R.layout.fragment_setup_selected_account) {

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
        tvError.visible()
        tvError.text = it
        rotationAnimation.cancel()
        blinkingAnimation.cancel()
        tvMigrationInProgress.gone()
    }

    private val vm : SetupSelectedAccountViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.selectAccount(arguments?.getString(Keys.SELECTED_ACCOUNT_ID_KEY) ?: throw IllegalStateException())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icon.startAnimation(rotationAnimation)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribe()
    }

    private fun subscribe() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
        vm.error.observe(viewLifecycleOwner, errorObserver)
        lifecycleScope.subscribe(vm.isMigrationInProgress) { isInProgress ->
            if (isInProgress) {
                tvMigrationInProgress.visible()
                tvMigrationInProgress.startAnimation(blinkingAnimation)
            }
            else {
                tvMigrationInProgress.invisible()
            }
        }
    }

    override fun injectDependencies() {
        componentManager().setupSelectedAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupSelectedAccountComponent.release()
    }

    companion object {
        const val BLINKING_ANIMATION_DURATION = 1000L
    }
}