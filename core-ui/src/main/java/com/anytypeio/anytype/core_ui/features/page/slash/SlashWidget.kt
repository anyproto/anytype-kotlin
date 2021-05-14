package com.anytypeio.anytype.core_ui.features.page.slash

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.page.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import kotlinx.android.synthetic.main.widget_editor_slash.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow

class SlashWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val _clickEvents = Channel<SlashItem>()
    val clickEvents = _clickEvents.consumeAsFlow()

    private val mainAdapter by lazy {
        SlashMainAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val styleAdapter by lazy {
        SlashStyleAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val mediaAdapter by lazy {
        SlashMediaAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val objectTypesAdapter by lazy {
        SlashObjectTypesAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val relationsAdapter by lazy {
        SlashRelationsAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val otherAdapter by lazy {
        SlashOtherAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val actionsAdapter by lazy {
        SlashActionsAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val alignAdapter by lazy {
        SlashAlignmentAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val colorAdapter by lazy {
        SlashColorAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val backgroundAdapter by lazy {
        SlashColorAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val concatAdapter = ConcatAdapter(
        mainAdapter,
        styleAdapter,
        mediaAdapter,
        objectTypesAdapter,
        relationsAdapter,
        otherAdapter,
        actionsAdapter,
        alignAdapter,
        colorAdapter,
        backgroundAdapter
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_editor_slash, this)
        setup(context)
    }

    private fun setup(context: Context) {
        with(rvSlash) {
            val lm = LinearLayoutManager(context)
            layoutManager = lm
            adapter = concatAdapter
        }
    }

    fun onStateChanged(widgetState: SlashWidgetState) {
        when (widgetState) {
            is SlashWidgetState.UpdateItems -> {
                rvSlash.smoothScrollToPosition(0)

                mainAdapter.update(widgetState.mainItems)
                styleAdapter.update(widgetState.styleItems)
                mediaAdapter.update(widgetState.mediaItems)
                objectTypesAdapter.update(widgetState.objectItems)
                if (widgetState.relationItems.isEmpty()) {
                    relationsAdapter.clear()
                } else {
                    relationsAdapter.update(widgetState.relationItems)
                }
                otherAdapter.update(widgetState.otherItems)
                actionsAdapter.update(widgetState.actionsItems)
                alignAdapter.update(widgetState.alignmentItems)
                colorAdapter.update(widgetState.colorItems)
                backgroundAdapter.update(widgetState.backgroundItems)
            }
        }
    }

    fun getWidgetMinHeight() = with(context.resources) {
        getDimensionPixelSize(R.dimen.slash_widget_item_height) * MIN_VISIBLE_ITEMS +
                getDimensionPixelSize(R.dimen.mention_divider_height)
    }

    companion object {
        const val MIN_VISIBLE_ITEMS = 4
    }
}