package com.anytypeio.anytype.core_ui.features.page.slash

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.tools.SlashHelper
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import kotlinx.android.synthetic.main.widget_editor_plus.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow

class SlashWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val channel = Channel<SlashItem>()
    val events = channel.consumeAsFlow()

    private val slashAdapter by lazy {
        SlashWidgetAdapter(items = listOf()) { channel.offer(it) }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_editor_plus, this)
        setup(context)
    }

    private fun setup(context: Context) {
        with(recyclerView) {
            val lm = LinearLayoutManager(context)
            layoutManager = lm
            adapter = slashAdapter
        }
    }

    fun filter(filter: String) {
        val items = SlashHelper.getSlashItems(filter.removePrefix("/"))
        slashAdapter.update(items)
    }

    fun getWidgetMinHeight() = with(context.resources) {
        getDimensionPixelSize(R.dimen.mention_suggester_item_height) * MIN_VISIBLE_ITEMS +
                getDimensionPixelSize(R.dimen.mention_list_padding_bottom) +
                getDimensionPixelSize(R.dimen.mention_divider_height)
    }

    companion object {
        const val MIN_VISIBLE_ITEMS = 4
    }
}