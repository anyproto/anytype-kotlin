package com.anytypeio.anytype.ui.auth.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.databinding.FragmentChoosePinCodeBinding
import com.anytypeio.anytype.presentation.auth.pin.ChoosePinCodeViewModel
import com.anytypeio.anytype.presentation.auth.pin.ChoosePinCodeViewModelFactory

class ChoosePinCodeFragment : PinCodeFragment<FragmentChoosePinCodeBinding>(R.layout.fragment_choose_pin_code) {

    //@Inject
    lateinit var factory: ChoosePinCodeViewModelFactory

    private val vm : ChoosePinCodeViewModel by viewModels { factory }

    private val numPadAdapter by lazy {
        NumPadAdapter(
            onNumberClicked = { view ->
                //vm.onNumPadClicked(view.number)
            },
            onRemoveClicked = {
                //vm.onRemovedDigitClicked()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
        binding.doItLaterButton.setOnClickListener {
            //vm.onDoItLaterClicked()
        }
    }

    override fun provideDotRecycler(): RecyclerView = binding.dotRecycler
    override fun provideNumPadRecycler(): RecyclerView = binding.numPadRecycler
    override fun provideNumPadAdapter(): NumPadAdapter = numPadAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //vm.pin.subscribe { state -> updateDotAdapter(state) }.disposedBy(subscriptions)
        setupNavigation()
    }

    private fun setupNavigation() {
        //vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChoosePinCodeBinding = FragmentChoosePinCodeBinding.inflate(
        inflater, container, false
    )
}