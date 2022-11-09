package com.anytypeio.anytype.core_ui.widgets.toolbar.table

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetSimpleTableBinding
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem
import com.anytypeio.anytype.presentation.editor.markup.MarkupColorView
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
        onClick = { item -> onItemClickListener.invoke(item)})
    private val columnAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> onItemClickListener.invoke(item)})
    private val rowAdapter = SimpleTableWidgetAdapter(items = listOf(),
        onClick = { item -> onItemClickListener.invoke(item)})

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
        cellAdapter.update(cellItems)
        columnAdapter.update(columnItems)
        rowAdapter.update(rowItems)
    }

    fun setListener(listener: (SimpleTableWidgetItem) -> Unit = {}) {
        onItemClickListener = listener
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