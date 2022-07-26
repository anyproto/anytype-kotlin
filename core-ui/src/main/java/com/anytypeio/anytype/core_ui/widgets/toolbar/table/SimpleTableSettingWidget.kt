package com.anytypeio.anytype.core_ui.widgets.toolbar.table

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetSimpleTableBinding
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetState
import com.google.android.material.tabs.TabLayoutMediator

class SimpleTableSettingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    val binding = WidgetSimpleTableBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val cellAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> })
    private val columnAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> })
    private val rowAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> })

    private val pagerAdapter = SimpleTableSettingAdapter(
        cellAdapter = cellAdapter,
        columnAdapter = columnAdapter,
        rowAdapter = rowAdapter
    )

    fun onStateChanged(state: SimpleTableWidgetState) {
        when (state) {
            is SimpleTableWidgetState.UpdateItems -> {
                cellAdapter.update(state.cellItems)
                columnAdapter.update(state.columnItems)
                rowAdapter.update(state.rowItems)
            }
            SimpleTableWidgetState.Idle -> {}
        }
    }

    init {
        binding.viewpager.adapter = pagerAdapter
        binding.viewpager.isUserInputEnabled = false
        TabLayoutMediator(binding.tabsLayout, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                0 -> context.getString(R.string.simple_tables_widget_tab_cell)
                1 -> context.getString(R.string.simple_tables_widget_tab_column)
                2 -> context.getString(R.string.simple_tables_widget_tab_row)
                else -> throw IllegalStateException("Unexpected position: $position")
            }
        }.attach()
    }
}