package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetMentionMenuBinding
import com.anytypeio.anytype.core_ui.widgets.toolbar.adapter.MentionAdapter
import com.anytypeio.anytype.core_utils.ui.NpaLinearLayoutManager
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

class MentionToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var mentionClick: ((DefaultObjectView, String, Int) -> Unit)? = null
    private var newPageClick: ((String) -> Unit)? = null
    private val mentionAdapter by lazy {
        MentionAdapter(
            data = arrayListOf(),
            onClicked = { objectView, filter, pos ->
                mentionClick?.invoke(objectView, filter, pos)
            },
            newClicked = { name ->
                newPageClick?.invoke(name)
            }
        )
    }

    val binding = WidgetMentionMenuBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        setup(context)
    }

    fun setupClicks(
        mentionClick: (DefaultObjectView, String, Int) -> Unit,
        newPageClick: (String) -> Unit
    ) {
        this.mentionClick = mentionClick
        this.newPageClick = newPageClick
    }

    private fun setup(context: Context) {
        with(binding.recyclerView) {
            val lm = NpaLinearLayoutManager(context)
            layoutManager = lm
            adapter = mentionAdapter
        }
    }

    fun addItems(items: List<DefaultObjectView>) {
        mentionAdapter.setData(items)
    }

    fun updateFilter(filter: String) {
        mentionAdapter.updateFilter(filter)
    }

    fun clear() {
        mentionAdapter.clear()
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