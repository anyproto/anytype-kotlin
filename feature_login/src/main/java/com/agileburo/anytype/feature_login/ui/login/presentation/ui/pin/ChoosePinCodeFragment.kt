package com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_utils.di.CoreComponentProvider
import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.ChoosePinCodeSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.ChoosePinCodeViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.ChoosePinCodeViewModelFactory
import kotlinx.android.synthetic.main.fragment_choose_pin_code.*
import javax.inject.Inject

class ChoosePinCodeFragment : PinCodeFragment() {

    @Inject
    lateinit var factory: ChoosePinCodeViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this)
            .get(ChoosePinCodeViewModel::class.java)
    }

    private val numPadAdapter by lazy {
        NumPadAdapter(
            onNumberClicked = { view -> vm.onNumPadClicked(view.number) },
            onRemoveClicked = { vm.onRemovedDigitClicked() }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_choose_pin_code, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
        doItLaterButton.setOnClickListener { vm.onDoItLaterClicked() }
    }

    override fun provideDotRecycler(): RecyclerView = dotRecycler
    override fun provideNumPadRecycler(): RecyclerView = numPadRecycler
    override fun provideNumPadAdapter(): NumPadAdapter = numPadAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.pin.subscribe { state -> updateDotAdapter(state) }.disposedBy(subscriptions)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {
        (activity as? CoreComponentProvider)?.let { provider ->
            ChoosePinCodeSubComponent
                .get(provider.provideCoreComponent())
                .inject(this)
        }
    }

    override fun releaseDependencies() {
        ChoosePinCodeSubComponent.clear()
    }
}