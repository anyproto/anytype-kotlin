package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetMainBottomToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.core_ui.R

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

    fun setState(state: NavPanelState) {
        when(state) {
            is NavPanelState.Default -> {
                when(val left = state.leftButtonState) {
                    is NavPanelState.LeftButtonState.AddMembers -> {
                        binding.icShare.setImageResource(
                            R.drawable.ic_nav_panel_add_member
                        )
                        if (left.isActive) {
                            binding.icShare.alpha = 1f
                        } else {
                            binding.icShare.alpha = 0.5f
                        }
                    }
                    is NavPanelState.LeftButtonState.Comment -> {

                    }
                    NavPanelState.LeftButtonState.ViewMembers -> {
                        binding.icShare.setImageResource(
                            R.drawable.ic_nav_panel_add_member
                        )
                        binding.icShare.alpha = 1f
                    }
                }
                if (state.isCreateObjectButtonEnabled) {
                    binding.icAddDoc.alpha = 1f
                } else {
                    binding.icAddDoc.alpha = 0.5f
                }
            }
            NavPanelState.Init -> {
                // Do nothing
            }
        }
    }
}