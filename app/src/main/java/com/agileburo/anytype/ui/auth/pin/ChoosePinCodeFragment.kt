package com.agileburo.anytype.ui.auth.pin

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.auth.pin.ChoosePinCodeViewModel
import com.agileburo.anytype.presentation.auth.pin.ChoosePinCodeViewModelFactory
import kotlinx.android.synthetic.main.fragment_choose_pin_code.*

class ChoosePinCodeFragment : PinCodeFragment(R.layout.fragment_choose_pin_code) {

    //@Inject
    lateinit var factory: ChoosePinCodeViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this)
            .get(ChoosePinCodeViewModel::class.java)
    }

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
        doItLaterButton.setOnClickListener {
            //vm.onDoItLaterClicked()
        }
    }

    override fun provideDotRecycler(): RecyclerView = dotRecycler
    override fun provideNumPadRecycler(): RecyclerView = numPadRecycler
    override fun provideNumPadAdapter(): NumPadAdapter = numPadAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //vm.pin.subscribe { state -> updateDotAdapter(state) }.disposedBy(subscriptions)
        setupNavigation()
    }

    private fun setupNavigation() {
        //vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {

    }

    override fun releaseDependencies() {

    }
}