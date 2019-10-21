package com.agileburo.anytype.ui.auth.account

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.account.CreateAccountViewModel
import com.agileburo.anytype.presentation.auth.account.CreateAccountViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_create_account.*
import javax.inject.Inject

class CreateAccountFragment : NavigationFragment(R.layout.fragment_create_account) {

    @Inject
    lateinit var factory: CreateAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(CreateAccountViewModel::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createProfileButton.setOnClickListener {
            vm.onCreateProfileClicked(nameInputField.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard(activity?.currentFocus)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(this, navObserver)
    }

    override fun injectDependencies() {
        componentManager().createAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createAccountComponent.release()
    }
}