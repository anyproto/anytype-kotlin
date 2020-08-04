package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.Mention
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.MentionAdapter
import kotlinx.android.synthetic.main.widget_mention_menu.view.*

class MentionToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var mentionClick: ((Mention, String) -> Unit)? = null
    private var newPageClick: (() -> Unit)? = null

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_mention_menu, this)
        setup(context)
    }

    fun setupClicks(mentionClick: (Mention, String) -> Unit, newPageClick: () -> Unit) {
        this.mentionClick = mentionClick
        this.newPageClick = newPageClick
    }

    private fun setup(context: Context) {
        with(recyclerView) {
            val lm = LinearLayoutManager(context)
            layoutManager = lm
            adapter = MentionAdapter(
                data = arrayListOf(),
                clicked = { mention, filter ->
                    mentionClick?.invoke(mention, filter)
                },
                newClicked = {
                    newPageClick?.invoke()
                }
            )
        }
    }

    fun addItems(items: List<Mention>) {
        (recyclerView.adapter as? MentionAdapter)?.setData(items)
    }

    fun updateFilter(filter: String) {
        (recyclerView.adapter as? MentionAdapter)?.updateFilter(filter)
    }

    fun getMentionSuggesterWidgetMinHeight() = with(context.resources) {
        getDimensionPixelSize(R.dimen.mention_suggester_item_height) * MIN_VISIBLE_ITEMS +
                getDimensionPixelSize(R.dimen.mention_list_padding_top) +
                getDimensionPixelSize(R.dimen.mention_list_padding_bottom) +
                getDimensionPixelSize(R.dimen.mention_divider_height)
    }

    companion object {
        const val MIN_VISIBLE_ITEMS = 4
    }
}