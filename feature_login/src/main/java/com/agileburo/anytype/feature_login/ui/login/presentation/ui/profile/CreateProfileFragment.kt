package com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.core_utils.disposedBy
import com.agileburo.anytype.core_utils.hideKeyboard
import com.agileburo.anytype.core_utils.toast
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.CreateProfileSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.ViewState
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.CreateProfileViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.CreateProfileViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_create_profile.*
import javax.inject.Inject

class CreateProfileFragment : BaseFragment() {

    @Inject
    lateinit var factory: CreateProfileViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(CreateProfileViewModel::class.java)
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


        vm.state.observe(this, Observer { state ->
            when (state) {
                is ViewState.Loading -> {
                    requireActivity().toast("Загрузка")
                }
                is ViewState.Error -> {
                    requireActivity().toast("Ошибка")
                }
                is ViewState.Success -> {
                    requireActivity().toast("Успех")
                }
            }
        })
    }

    private fun setupNavigation() {
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {
        CreateProfileSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        CreateProfileSubComponent.clear()
    }
}