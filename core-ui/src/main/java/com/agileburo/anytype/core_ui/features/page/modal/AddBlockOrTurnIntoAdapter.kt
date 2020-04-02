package com.agileburo.anytype.core_ui.features.page.modal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.core_ui.model.UiBlock
import kotlinx.android.synthetic.main.item_add_block_or_turn_into_item.view.*
import kotlinx.android.synthetic.main.item_add_block_or_turn_into_section.view.*

class AddBlockOrTurnIntoAdapter(
    private val views: List<AddBlockView> = default(),
    private val onUiBlockClicked: (UiBlock) -> Unit
) : RecyclerView.Adapter<AddBlockOrTurnIntoAdapter.ViewHolder>() {

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
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Section(view: View) : ViewHolder(view) {

            private val title = itemView.section

            fun bind(section: AddBlockView.Section) {
                when (section.category) {
                    UiBlock.Category.TEXT -> title.setText(R.string.toolbar_section_text)
                    UiBlock.Category.LIST -> title.setText(R.string.toolbar_section_list)
                    UiBlock.Category.PAGE -> title.setText(R.string.toolbar_section_page)
                    UiBlock.Category.OBJECT -> title.setText(R.string.toolbar_section_objects)
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
                    UiBlock.EXISTING_PAGE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_existing_page)
                        title.setText(R.string.option_tool_existing_page)
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
                    UiBlock.CODE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_code)
                        title.setText(R.string.option_media_code)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_code)
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
                }

                itemView.setOnClickListener { onUiBlockClicked(item.type) }
            }
        }

    }

    sealed class AddBlockView : ViewType {
        data class Section(val category: UiBlock.Category) : AddBlockView() {
            override fun getViewType(): Int = VIEW_HOLDER_SECTION
        }

        data class Item(val type: UiBlock) : AddBlockView() {
            override fun getViewType(): Int = VIEW_HOLDER_ITEM
        }
    }

    companion object {
        const val VIEW_HOLDER_SECTION = 0
        const val VIEW_HOLDER_ITEM = 1

        fun default(): List<AddBlockView> = listOf(
            AddBlockView.Section(category = UiBlock.Category.TEXT),
            AddBlockView.Item(type = UiBlock.TEXT),
            AddBlockView.Item(type = UiBlock.HEADER_ONE),
            AddBlockView.Item(type = UiBlock.HEADER_TWO),
            AddBlockView.Item(type = UiBlock.HEADER_THREE),
            AddBlockView.Item(type = UiBlock.HIGHLIGHTED),
            AddBlockView.Section(category = UiBlock.Category.LIST),
            AddBlockView.Item(type = UiBlock.CHECKBOX),
            AddBlockView.Item(type = UiBlock.BULLETED),
            AddBlockView.Item(type = UiBlock.NUMBERED),
            AddBlockView.Item(type = UiBlock.TOGGLE),
            AddBlockView.Section(category = UiBlock.Category.PAGE),
            AddBlockView.Item(type = UiBlock.PAGE),
            AddBlockView.Item(type = UiBlock.EXISTING_PAGE),
            AddBlockView.Section(category = UiBlock.Category.OBJECT),
            AddBlockView.Item(type = UiBlock.FILE),
            AddBlockView.Item(type = UiBlock.IMAGE),
            AddBlockView.Item(type = UiBlock.VIDEO),
            AddBlockView.Item(type = UiBlock.BOOKMARK),
            AddBlockView.Item(type = UiBlock.CODE),
            AddBlockView.Section(category = UiBlock.Category.OTHER),
            AddBlockView.Item(type = UiBlock.LINE_DIVIDER),
            AddBlockView.Item(type = UiBlock.THREE_DOTS)
        )
    }
}