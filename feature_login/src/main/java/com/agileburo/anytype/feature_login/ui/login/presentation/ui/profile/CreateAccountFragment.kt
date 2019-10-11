package com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.core_utils.di.CoreComponentProvider
import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.CreateProfileSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.CreateAccountViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.CreateAccountViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_create_profile.*
import javax.inject.Inject

class CreateAccountFragment : BaseFragment() {

    @Inject
    lateinit var factory: CreateAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(CreateAccountViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_profile, container, false)

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
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {
        (activity as? CoreComponentProvider)?.let { provider ->
            CreateProfileSubComponent
                .get(provider.provideCoreComponent())
                .inject(this)
        }
    }

    override fun releaseDependencies() {
        CreateProfileSubComponent.clear()
    }
}