package com.anytypeio.anytype.core_ui.widgets.toolbar.table

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetSimpleTableBinding
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SimpleTableSettingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    val binding = WidgetSimpleTableBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    var onItemClickListener: (SimpleTableWidgetItem) -> Unit = {}

    private val cellAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> onItemClickListener.invoke(item) })
    private val columnAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> onItemClickListener.invoke(item) })
    private val rowAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> onItemClickListener.invoke(item) })

    private val pagerAdapter = SimpleTableSettingAdapter(
        cellAdapter = cellAdapter,
        columnAdapter = columnAdapter,
        rowAdapter = rowAdapter
    )

    fun onStateChanged(
        cellItems: List<SimpleTableWidgetItem>,
        rowItems: List<SimpleTableWidgetItem>,
        columnItems: List<SimpleTableWidgetItem>
    ) {
        if (cellItems.isNotEmpty() && binding.tabsLayout.selectedTabPosition != TAB_CELL_POSITION) {
            binding.tabsLayout.selectTab(binding.tabsLayout.getTabAt(TAB_CELL_POSITION))
        }
        if (columnItems.isNotEmpty() && binding.tabsLayout.selectedTabPosition != TAB_COLUMN_POSITION) {
            binding.tabsLayout.selectTab(binding.tabsLayout.getTabAt(TAB_COLUMN_POSITION))
        }
        if (rowItems.isNotEmpty() && binding.tabsLayout.selectedTabPosition != TAB_ROW_POSITION) {
            binding.tabsLayout.selectTab(binding.tabsLayout.getTabAt(TAB_ROW_POSITION))
        }
        cellAdapter.update(cellItems)
        columnAdapter.update(columnItems)
        rowAdapter.update(rowItems)
    }

    fun setListener(listener: (SimpleTableWidgetItem) -> Unit = {}) {
        onItemClickListener = listener
        binding.tabsLayout.apply {
            addOnTabSelectedListener(onTabSelectedListener)
        }
    }

    init {
        binding.viewpager.adapter = pagerAdapter
        binding.viewpager.isUserInputEnabled = false
        TabLayoutMediator(binding.tabsLayout, binding.viewpager) { tab, position ->
            tab.text = when (position) {
                TAB_CELL_POSITION -> context.getString(R.string.simple_tables_widget_tab_cell)
                TAB_COLUMN_POSITION -> context.getString(R.string.simple_tables_widget_tab_column)
                TAB_ROW_POSITION -> context.getString(R.string.simple_tables_widget_tab_row)
                else -> throw IllegalStateException("Unexpected position: $position")
            }
        }.attach()
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.text) {
                context.getString(R.string.simple_tables_widget_tab_cell) -> {
                    onItemClickListener(SimpleTableWidgetItem.Tab.Cell)
                }
                context.getString(R.string.simple_tables_widget_tab_row) -> {
                    onItemClickListener(SimpleTableWidgetItem.Tab.Row)
                }
                context.getString(R.string.simple_tables_widget_tab_column) -> {
                    onItemClickListener(SimpleTableWidgetItem.Tab.Column)
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    companion object {
        const val TAB_CELL_POSITION = 0
        const val TAB_COLUMN_POSITION = 1
        const val TAB_ROW_POSITION = 2
    }
}