package com.anytypeio.anytype.core_ui.features.page.slash

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.tools.SlashHelper
import com.anytypeio.anytype.presentation.page.editor.slash.SlashCommand
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

    private val _backEvent = Channel<Boolean>()
    val backEvent = _backEvent.consumeAsFlow()


    private val mainAdapter by lazy {
        SlashMainAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) }
        )
    }

    private val styleAdapter by lazy {
        SlashStyleAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) },
            clickBack = { _backEvent.offer(true) }
        )
    }

    private val mediaAdapter by lazy {
        SlashMediaAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) },
            clickBack = { _backEvent.offer(true) }
        )
    }

    private val objectTypesAdapter by lazy {
        SlashObjectTypesAdapter(
            items = listOf(),
            clicks = { _clickEvents.offer(it) },
            clickBack = { _backEvent.offer(true) }
        )
    }

    private val relationsAdapter by lazy {
        SlashRelationsAdapter(
            items = listOf(),
            onRelationClicked = {}
        )
    }

    private val concatAdapter = ConcatAdapter(
        mainAdapter,
        styleAdapter,
        mediaAdapter,
        objectTypesAdapter,
        relationsAdapter
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

    fun onCommand(command: SlashCommand) {
        when (command) {
            is SlashCommand.ShowMainItems -> {
                mainAdapter.update(command.items)
                rvSlash.smoothScrollToPosition(0)

                styleAdapter.clear()
                mediaAdapter.clear()
                objectTypesAdapter.clear()
                relationsAdapter.clear()
            }
            is SlashCommand.ShowStyleItems -> {
                styleAdapter.update(command.items)
                rvSlash.smoothScrollToPosition(0)

                mainAdapter.clear()
                mediaAdapter.clear()
                objectTypesAdapter.clear()
                relationsAdapter.clear()
            }
            is SlashCommand.ShowMediaItems -> {
                mediaAdapter.update(command.items)
                rvSlash.smoothScrollToPosition(0)

                mainAdapter.clear()
                styleAdapter.clear()
                objectTypesAdapter.clear()
                relationsAdapter.clear()
            }
            is SlashCommand.ShowRelations -> {
                relationsAdapter.update(command.relations)
                rvSlash.smoothScrollToPosition(0)

                mainAdapter.clear()
                styleAdapter.clear()
                mediaAdapter.clear()
                objectTypesAdapter.clear()
            }
            is SlashCommand.ShowObjectTypes -> {
                objectTypesAdapter.update(command.items)
                rvSlash.smoothScrollToPosition(0)

                mainAdapter.clear()
                styleAdapter.clear()
                mediaAdapter.clear()
                relationsAdapter.clear()
            }
            SlashCommand.ShowOtherItems -> TODO()
            is SlashCommand.FilterItems -> {
                val filter = command.filter.removePrefix(SLASH_PREFIX)
                val items = SlashHelper.filterSlashItems(
                    filter = filter,
                    viewType = command.viewType
                )
                styleAdapter.update(items)
            }
        }
    }

    fun getWidgetMinHeight() = with(context.resources) {
        getDimensionPixelSize(R.dimen.mention_suggester_item_height) * MIN_VISIBLE_ITEMS +
                getDimensionPixelSize(R.dimen.mention_list_padding_bottom) +
                getDimensionPixelSize(R.dimen.mention_divider_height)
    }

    companion object {
        const val MIN_VISIBLE_ITEMS = 4
        const val SLASH_PREFIX = "/"
    }
}