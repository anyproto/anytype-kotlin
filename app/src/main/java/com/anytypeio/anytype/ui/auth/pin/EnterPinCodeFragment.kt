package com.anytypeio.anytype.ui.auth.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.presentation.auth.pin.EnterPinCodeViewModel
import com.anytypeio.anytype.presentation.auth.pin.EnterPinCodeViewModelFactory
import kotlinx.android.synthetic.main.fragment_enter_pin_code.*
import javax.inject.Inject

class EnterPinCodeFragment : BaseFragment(R.layout.fragment_enter_pin_code) {

    @Inject
    lateinit var factory: EnterPinCodeViewModelFactory

    private val vm : EnterPinCodeViewModel by viewModels { factory }

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

    private fun setupNumPad() {
        //
    }

    override fun injectDependencies() {

    }

    override fun releaseDependencies() {

    }
}