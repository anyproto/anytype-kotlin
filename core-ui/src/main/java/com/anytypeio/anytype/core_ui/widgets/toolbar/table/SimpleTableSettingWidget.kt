package com.anytypeio.anytype.core_ui.widgets.toolbar.table

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetSimpleTableBinding
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem
import com.google.android.material.tabs.TabLayout

class SimpleTableSettingWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    val binding = WidgetSimpleTableBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    var onItemClickListener: (SimpleTableWidgetItem) -> Unit = {}

    private val itemsAdapter = SimpleTableWidgetAdapter(
        onClick = { item -> onItemClickListener.invoke(item) }
    )

    init {
        binding.recyclerItems.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = itemsAdapter
        }
        binding.tabsLayout.apply {
            addTab(
                newTab().setText(context.getString(R.string.simple_tables_widget_tab_cell)),
                true
            )
            addTab(newTab().setText(context.getString(R.string.simple_tables_widget_tab_column)))
            addTab(newTab().setText(context.getString(R.string.simple_tables_widget_tab_row)))
        }
    }

    fun onStateChanged(
        items: List<SimpleTableWidgetItem>,
        tab: BlockView.Table.Tab
    ) {
        itemsAdapter.submitList(items)
        when (tab) {
            BlockView.Table.Tab.CELL -> {
                if (binding.tabsLayout.selectedTabPosition != TAB_CELL_POSITION) {
                    binding.tabsLayout.selectTab(binding.tabsLayout.getTabAt(TAB_CELL_POSITION))
                }
            }
            BlockView.Table.Tab.COLUMN -> {
                if (binding.tabsLayout.selectedTabPosition != TAB_COLUMN_POSITION) {
                    binding.tabsLayout.selectTab(binding.tabsLayout.getTabAt(TAB_COLUMN_POSITION))
                }
            }
            BlockView.Table.Tab.ROW -> {
                if (binding.tabsLayout.selectedTabPosition != TAB_ROW_POSITION) {
                    binding.tabsLayout.selectTab(binding.tabsLayout.getTabAt(TAB_ROW_POSITION))
                }
            }
        }
    }

    fun setListener(listener: (SimpleTableWidgetItem) -> Unit = {}) {
        onItemClickListener = listener
        binding.tabsLayout.apply {
            addOnTabSelectedListener(onTabSelectedListener)
        }
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                TAB_CELL_POSITION -> {
                    onItemClickListener(SimpleTableWidgetItem.Tab.Cell)
                }
                TAB_ROW_POSITION -> {
                    onItemClickListener(SimpleTableWidgetItem.Tab.Row)
                }
                TAB_COLUMN_POSITION -> {
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