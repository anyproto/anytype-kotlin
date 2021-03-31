package com.anytypeio.anytype.core_ui.features.page.modal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.page.picker.AddBlockView
import com.anytypeio.anytype.presentation.page.picker.AddBlockView.Companion.VIEW_HOLDER_ADD_BLOCK_HEADER
import com.anytypeio.anytype.presentation.page.picker.AddBlockView.Companion.VIEW_HOLDER_ITEM
import com.anytypeio.anytype.presentation.page.picker.AddBlockView.Companion.VIEW_HOLDER_OBJECT_TYPES
import com.anytypeio.anytype.presentation.page.picker.AddBlockView.Companion.VIEW_HOLDER_SECTION
import com.anytypeio.anytype.presentation.page.picker.AddBlockView.Companion.VIEW_HOLDER_TURN_INTO_HEADER
import kotlinx.android.synthetic.main.item_add_block_or_turn_into_item.view.*
import kotlinx.android.synthetic.main.item_add_block_or_turn_into_object_type.view.*
import kotlinx.android.synthetic.main.item_add_block_or_turn_into_section.view.*

class AddBlockOrTurnIntoAdapter(
    private var views: List<AddBlockView> = emptyList(),
    private val onUiBlockClicked: (UiBlock) -> Unit,
    private val onObjectClicked: (AddBlockView.ObjectView) -> Unit
) : RecyclerView.Adapter<AddBlockOrTurnIntoAdapter.ViewHolder>() {

    fun update(views: List<AddBlockView>) {
        val diff = DiffUtil.calculateDiff(AddBlockDiffUtil(old = this.views, new = views))
        this.views = views
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_HOLDER_ITEM -> ViewHolder.Item(
                view = inflater.inflate(
                    R.layout.item_add_block_or_turn_into_item,
                    parent,
                    false
                )
            )
            VIEW_HOLDER_SECTION -> ViewHolder.Section(
                view = inflater.inflate(
                    R.layout.item_add_block_or_turn_into_section,
                    parent,
                    false
                )
            )
            VIEW_HOLDER_ADD_BLOCK_HEADER -> ViewHolder.AddBlockHeader(
                view = inflater.inflate(
                    R.layout.item_add_block_or_turn_into_header,
                    parent,
                    false
                )
            )
            VIEW_HOLDER_TURN_INTO_HEADER -> ViewHolder.TurnIntoHeader(
                view = inflater.inflate(
                    R.layout.item_add_block_or_turn_into_header,
                    parent,
                    false
                )
            )
            VIEW_HOLDER_OBJECT_TYPES -> ViewHolder.ObjectType(
                view = inflater.inflate(
                    R.layout.item_add_block_or_turn_into_object_type,
                    parent,
                    false
                )
            ).apply {
                itemView.setOnClickListener {
                    onObjectClicked(views[bindingAdapterPosition] as AddBlockView.ObjectView)
                }
            }
            else -> throw IllegalStateException("Unexpected type: $viewType")
        }
    }

    override fun getItemCount(): Int = views.size
    override fun getItemViewType(position: Int) = views[position].getViewType()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Section -> holder.bind(
                section = views[position] as AddBlockView.Section
            )
            is ViewHolder.Item -> holder.bind(
                item = views[position] as AddBlockView.Item,
                onUiBlockClicked = onUiBlockClicked
            )
            is ViewHolder.ObjectType -> holder.bind(
                item = views[position] as AddBlockView.ObjectView
            )
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class AddBlockHeader(view: View) : ViewHolder(view) {
            init {
                itemView.title.setText(R.string.add_new)
            }
        }

        class TurnIntoHeader(view: View) : ViewHolder(view) {
            init {
                itemView.title.setText(R.string.turn_into)
            }
        }

        class Section(view: View) : ViewHolder(view) {

            private val title = itemView.section

            fun bind(section: AddBlockView.Section) {
                when (section.category) {
                    UiBlock.Category.TEXT -> title.setText(R.string.toolbar_section_text)
                    UiBlock.Category.LIST -> title.setText(R.string.toolbar_section_list)
                    UiBlock.Category.OBJECT -> title.setText(R.string.toolbar_section_objects)
                    UiBlock.Category.RELATION -> title.setText(R.string.toolbar_section_relation)
                    UiBlock.Category.OTHER -> title.setText(R.string.toolbar_section_other)
                }
            }
        }

        class Item(view: View) : ViewHolder(view) {

            private val icon = itemView.icon
            private val title = itemView.title
            private val subtitle = itemView.subtitle

            fun bind(
                item: AddBlockView.Item,
                onUiBlockClicked: (UiBlock) -> Unit
            ) {
                when (item.type) {
                    UiBlock.TEXT -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_text)
                        title.setText(R.string.option_text_text)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_text)
                    }
                    UiBlock.HEADER_ONE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_h1)
                        title.setText(R.string.option_text_header_one)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_header_one)
                    }
                    UiBlock.HEADER_TWO -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_h2)
                        title.setText(R.string.option_text_header_two)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_header_two)
                    }
                    UiBlock.HEADER_THREE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_h3)
                        title.setText(R.string.option_text_header_three)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_header_three)
                    }
                    UiBlock.HIGHLIGHTED -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_highlighted)
                        title.setText(R.string.option_text_highlighted)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_highlighted)
                    }
                    UiBlock.CHECKBOX -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_checkbox)
                        title.setText(R.string.option_list_checkbox)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_checkbox)
                    }
                    UiBlock.BULLETED -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_bulleted)
                        title.setText(R.string.option_list_bulleted_list)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_bulleted)
                    }
                    UiBlock.NUMBERED -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_numbered)
                        title.setText(R.string.option_list_numbered_list)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_numbered)
                    }
                    UiBlock.TOGGLE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_toggle)
                        title.setText(R.string.option_list_toggle_list)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_toggle)
                    }
                    UiBlock.PAGE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_page)
                        title.setText(R.string.option_tool_page)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_page)
                    }
                    UiBlock.LINK_TO_OBJECT -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_link_to_object)
                        title.setText(R.string.option_tool_link_to_object)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_existing_page)
                    }
                    UiBlock.FILE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_file)
                        title.setText(R.string.option_media_file)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_file)
                    }
                    UiBlock.IMAGE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_picture)
                        title.setText(R.string.option_media_picture)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_image)
                    }
                    UiBlock.VIDEO -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_video)
                        title.setText(R.string.option_media_video)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_video)
                    }
                    UiBlock.BOOKMARK -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_bookmark)
                        title.setText(R.string.option_media_bookmark)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_bookmark)
                    }
                    UiBlock.LINE_DIVIDER -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_line_divider)
                        title.setText(R.string.option_other_divider)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_line_divider)
                    }
                    UiBlock.THREE_DOTS -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_three_dots_divider)
                        title.setText(R.string.option_other_dots)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_dots)
                    }
                    UiBlock.CODE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_code)
                        title.setText(R.string.option_other_code)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_code)
                    }
                    UiBlock.RELATION -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_relation)
                        title.setText(R.string.option_relation_relation)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_relation)
                    }
                }

                itemView.setOnClickListener { onUiBlockClicked(item.type) }
            }
        }

        class ObjectType(view: View) : ViewHolder(view) {
            fun bind(
                item: AddBlockView.ObjectView
            ) {
                itemView.tvTitle.text = item.name
                itemView.tvSubtitle.text = item.description
                itemView.iconWidget.setIcon(
                    emoji = item.emoji,
                    image = null,
                    name = item.name
                )
            }
        }
    }

    companion object {

        fun turnIntoAdapterData(
            excludedTypes: List<UiBlock> = emptyList(),
            excludedCategories: List<UiBlock.Category> = emptyList()
        ): List<AddBlockView> {

            val aggregated = UiBlock.values().groupBy { it.category() }

            return mutableListOf<AddBlockView>().apply {
                add(AddBlockView.TurnIntoHeader)
                aggregated.forEach { (category, types) ->
                    if (!excludedCategories.contains(category)) {
                        add(AddBlockView.Section(category = category))
                        types.forEach { type ->
                            if (!excludedTypes.contains(type)) {
                                add(AddBlockView.Item(type = type))
                            }
                        }
                    }
                }
            }
        }
    }
}

class AddBlockDiffUtil(
    private val old: List<AddBlockView>,
    private val new: List<AddBlockView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return oldItem.getViewType() == newItem.getViewType()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return false
    }

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size
}