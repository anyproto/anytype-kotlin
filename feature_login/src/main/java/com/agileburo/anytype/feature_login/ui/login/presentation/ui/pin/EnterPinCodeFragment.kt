package com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.agileburo.anytype.core_utils.dimen
import com.agileburo.anytype.core_utils.disposedBy
import com.agileburo.anytype.core_utils.setOnClickListeners
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.EnterPinCodeSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.EnterPinCodeViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin.EnterPinCodeViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.SpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_choose_pin_code.dotRecycler
import kotlinx.android.synthetic.main.fragment_enter_pin_code.*
import javax.inject.Inject

class EnterPinCodeFragment : BaseFragment() {

    @Inject
    lateinit var factory: EnterPinCodeViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(EnterPinCodeViewModel::class.java)
    }

    private val dotAdapter by lazy {
        DotAdapter(
            dots = mutableListOf(
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                )
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_enter_pin_code, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNumPad()

        dotRecycler.apply {
            layoutManager = GridLayoutManager(
                requireContext(), 1, GridLayoutManager.HORIZONTAL, false
            )
            adapter = dotAdapter
            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = context.dimen(R.dimen.dot_spacing).toInt()
                )
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        vm.pin.subscribe { state ->
            val update = dotAdapter.dots.mapIndexed { index, dot ->
                if (index < state.digits.size)
                    dot.copy(active = true)
                else
                    dot.copy(active = false)
            }

            dotAdapter.dots.apply {
                clear()
                addAll(update)
            }

            dotAdapter.notifyDataSetChanged()

        }.disposedBy(subscriptions)
    }

    private fun setupNumPad() {
        pad.setOnClickListeners(View.OnClickListener { view ->
            vm.onNumPadClicked((view as TextView).text.toString())
        })
        removeButton.setOnClickListener { vm.onRemovedDigitClicked() }
    }

    override fun injectDependencies() {
        EnterPinCodeSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        EnterPinCodeSubComponent.clear()
    }
}