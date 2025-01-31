package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetMainBottomToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DEFAULT_DISABLED_ALPHA
import com.anytypeio.anytype.core_ui.common.FULL_ALPHA
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible

class MainBottomToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetMainBottomToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        isBaselineAligned = false
        orientation = HORIZONTAL
    }

    fun searchClicks() = binding.btnSearch.clicks()
    fun addDocClicks() = binding.btnAddDoc.clicks()
    fun shareClicks() = binding.btnShare.clicks()
    fun homeClicks() = binding.btnHome.clicks()

    fun setState(state: NavPanelState) {
        when(state) {
            is NavPanelState.Default -> {
                setDefaultState(state)
            }
            NavPanelState.Init -> {
                // Do nothing
            }
        }
    }

    private fun setDefaultState(state: NavPanelState.Default) {
        setLeftButtonState(state)
        setCreateButtonState(state)
    }

    private fun setCreateButtonState(state: NavPanelState.Default) {
        if (state.isCreateObjectButtonEnabled) {
            binding.icAddDoc.alpha = FULL_ALPHA
            binding.btnAddDoc.isEnabled = true
        } else {
            binding.icAddDoc.alpha = DEFAULT_DISABLED_ALPHA
            binding.btnAddDoc.isEnabled = false
        }
    }

    private fun setLeftButtonState(state: NavPanelState.Default) {
        when (val left = state.leftButtonState) {
            is NavPanelState.LeftButtonState.AddMembers -> {
                binding.btnHome.gone()
                binding.icShare.setImageResource(
                    R.drawable.ic_nav_panel_add_member
                )
                if (left.isActive) {
                    binding.icShare.alpha = FULL_ALPHA
                } else {
                    binding.icShare.alpha = DEFAULT_DISABLED_ALPHA
                }
                binding.btnShare.visible()
            }
            is NavPanelState.LeftButtonState.Home -> {
                binding.btnShare.gone()
                binding.btnHome.visible()
            }
            is NavPanelState.LeftButtonState.Comment -> {

            }

            NavPanelState.LeftButtonState.ViewMembers -> {
                binding.icShare.setImageResource(
                    R.drawable.ic_nav_panel_add_member
                )
                binding.icShare.alpha = FULL_ALPHA
            }
        }
    }
}