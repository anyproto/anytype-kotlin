package com.agileburo.anytype.ui.auth.pin

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ui.BaseFragment
import com.agileburo.anytype.core_utils.ui.SpacingItemDecoration
import com.agileburo.anytype.presentation.auth.pin.PinCodeState

abstract class PinCodeFragment(
    @LayoutRes private val layout: Int
) : BaseFragment(layout) {

    private val dotAdapter by lazy { DotAdapter() }

    abstract fun provideDotRecycler(): RecyclerView
    abstract fun provideNumPadRecycler(): RecyclerView
    abstract fun provideNumPadAdapter(): NumPadAdapter

    fun setupRecyclers() {
        provideDotRecycler().apply {
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

        provideNumPadRecycler().apply {
            adapter = provideNumPadAdapter()
            addItemDecoration(
                SpacingItemDecoration(
                    spacingTop = context.dimen(R.dimen.num_pad_vertical_spacing).toInt()
                )
            )
        }
    }

    fun updateDotAdapter(state: PinCodeState) {
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
    }
}