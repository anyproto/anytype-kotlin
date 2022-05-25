package com.anytypeio.anytype.ui.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.databinding.FragmentSetupNewAccountBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewState
import com.anytypeio.anytype.ui.base.NavigationFragment
import javax.inject.Inject

class SetupNewAccountFragment : NavigationFragment<FragmentSetupNewAccountBinding>(R.layout.fragment_setup_new_account),
    Observer<SetupNewAccountViewState> {

    @Inject
    lateinit var factory: SetupNewAccountViewModelFactory

    private val vm : SetupNewAccountViewModel by viewModels { factory }

    private val animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotation)
    }

    private lateinit var callBack : OnBackPressedCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callBack = requireActivity().onBackPressedDispatcher.addCallback(this) {}
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
        vm.state.observe(viewLifecycleOwner, this)
        binding.btnRetry.setOnClickListener { vm.onRetryClicked() }
    }

    override fun onChanged(state: SetupNewAccountViewState) {
        when (state) {
            is SetupNewAccountViewState.Loading -> {
                binding.tvError.gone()
                disableBackNavigation()
                binding.icon.startAnimation(animation)
                binding.btnRetry.invisible()
            }
            is SetupNewAccountViewState.Success -> {
                binding.tvError.gone()
                enableBackNavigation()
                animation.cancel()
            }
            is SetupNewAccountViewState.Error -> {
                enableBackNavigation()
                animation.cancel()
                binding.tvError.text = state.message
                binding.tvError.visible()
            }
            is SetupNewAccountViewState.InvalidCodeError -> {
                enableBackNavigation()
                animation.cancel()
                binding.tvError.gone()
                requireActivity().toast(state.message)
            }
            is SetupNewAccountViewState.ErrorNetwork -> {
                animation.cancel()
                binding.tvError.text = state.msg
                binding.tvError.visible()
                binding.btnRetry.visible()
            }
        }
    }

    private fun disableBackNavigation() {
        callBack.isEnabled = true
    }

    private fun enableBackNavigation() {
        callBack.isEnabled = false
    }

    override fun injectDependencies() {
        componentManager().setupNewAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupNewAccountComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetupNewAccountBinding = FragmentSetupNewAccountBinding.inflate(
        inflater, container, false
    )
}