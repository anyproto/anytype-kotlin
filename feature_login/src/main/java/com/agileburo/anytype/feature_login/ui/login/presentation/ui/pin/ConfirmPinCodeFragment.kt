package com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_utils.disposedBy
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.ConfirmPinCodeSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.common.Keys
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.ConfirmPinCodeViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.ConfirmPinCodeViewModelFactory
import kotlinx.android.synthetic.main.fragment_confirm_pin_code.*
import javax.inject.Inject

class ConfirmPinCodeFragment : PinCodeFragment() {

    @Inject
    lateinit var factory: ConfirmPinCodeViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(ConfirmPinCodeViewModel::class.java)
    }

    private val numPadAdapter by lazy {
        NumPadAdapter(
            onNumberClicked = { view -> vm.onNumPadClicked(view.number) },
            onRemoveClicked = { vm.onRemovedDigitClicked() }
        )
    }

    override fun provideDotRecycler(): RecyclerView = dotRecycler
    override fun provideNumPadRecycler(): RecyclerView = numPadRecycler
    override fun provideNumPadAdapter(): NumPadAdapter = numPadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_confirm_pin_code, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupPinCode()
        vm.pin.subscribe { state -> updateDotAdapter(state) }.disposedBy(subscriptions)
        setupNavigation()
    }

    private fun setupPinCode() {
        vm.code = arguments?.getString(Keys.PIN_CODE_KEY) ?: throw IllegalStateException("Code can't be null")
    }

    private fun setupNavigation() {
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {
        ConfirmPinCodeSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        ConfirmPinCodeSubComponent.clear()
    }
}