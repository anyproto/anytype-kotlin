package com.anytypeio.anytype.feature_login.ui.login.presentation.ui.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.databinding.FragmentConfirmPinCodeBinding
import com.anytypeio.anytype.presentation.auth.pin.ConfirmPinCodeViewModel
import com.anytypeio.anytype.presentation.auth.pin.ConfirmPinCodeViewModelFactory
import com.anytypeio.anytype.ui.auth.pin.NumPadAdapter
import com.anytypeio.anytype.ui.auth.pin.PinCodeFragment
import javax.inject.Inject

class ConfirmPinCodeFragment : PinCodeFragment<FragmentConfirmPinCodeBinding>(R.layout.fragment_confirm_pin_code) {

    @Inject
    lateinit var factory: ConfirmPinCodeViewModelFactory

    private val vm : ConfirmPinCodeViewModel by viewModels { factory }

    private val numPadAdapter by lazy {
        NumPadAdapter(
            onNumberClicked = {
                //view -> vm.onNumPadClicked(view.number)
            },
            onRemoveClicked = {
                //vm.onRemovedDigitClicked()
            }
        )
    }

    override fun provideDotRecycler(): RecyclerView = binding.dotRecycler
    override fun provideNumPadRecycler(): RecyclerView = binding.numPadRecycler
    override fun provideNumPadAdapter(): NumPadAdapter = numPadAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupPinCode()
        //vm.pin.subscribe { state -> updateDotAdapter(state) }.disposedBy(subscriptions)
        setupNavigation()
    }

    private fun setupPinCode() {
        //vm.code = arguments?.getString(Keys.PIN_CODE_KEY) ?: throw IllegalStateException("Code can't be null")
    }

    private fun setupNavigation() {
        //vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {

    }

    override fun releaseDependencies() {

    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentConfirmPinCodeBinding = FragmentConfirmPinCodeBinding.inflate(
        inflater, container, false
    )
}