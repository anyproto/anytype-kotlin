package com.agileburo.anytype.core_ui.features.page.modal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_LIST_BULLETED_LIST
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_LIST_CHECKBOX
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_LIST_NUMBERED_LIST
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_LIST_TOGGLE_LIST
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_OBJECTS_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_OBJECTS_CODE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_OBJECTS_FILE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_OBJECTS_IMAGE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_OBJECTS_VIDEO
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_OTHER_DOTS
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_OTHER_LINE_DIVIDER
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_PAGE_EXISTING_PAGE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_PAGE_PAGE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_TEXT_HEADER_ONE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_TEXT_HEADER_THREE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_TEXT_HEADER_TWO
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_TEXT_HIGHLIGHTED
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.OPTION_TEXT_TEXT
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.SECTION_LIST
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.SECTION_OBJECTS
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.SECTION_OTHER
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.SECTION_PAGE
import com.agileburo.anytype.core_ui.features.page.modal.AddBlockOrTurnIntoAdapter.AddBlockConfig.SECTION_TEXT
import kotlinx.android.synthetic.main.item_add_block_or_turn_into_item.view.*
import kotlinx.android.synthetic.main.item_add_block_or_turn_into_section.view.*

class AddBlockOrTurnIntoAdapter(
    private val views: List<AddBlockView> = defaultViews(),
    private val onOptionClicked: (Int) -> Unit
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
                onOptionClicked = onOptionClicked
            )
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Section(view: View) : ViewHolder(view) {

            private val title = itemView.section

            fun bind(section: AddBlockView.Section) {
                when (section.type) {
                    SECTION_TEXT -> title.setText(R.string.toolbar_section_text)
                    SECTION_LIST -> title.setText(R.string.toolbar_section_list)
                    SECTION_PAGE -> title.setText(R.string.toolbar_section_page)
                    SECTION_OBJECTS -> title.setText(R.string.toolbar_section_objects)
                    SECTION_OTHER -> title.setText(R.string.toolbar_section_other)
                }
            }
        }

        class Item(view: View) : ViewHolder(view) {

            private val icon = itemView.icon
            private val title = itemView.title
            private val subtitle = itemView.subtitle

            fun bind(
                item: AddBlockView.Item,
                onOptionClicked: (Int) -> Unit
            ) {
                when (item.type) {
                    OPTION_TEXT_TEXT -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_text)
                        title.setText(R.string.option_text_text)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_text)
                    }
                    OPTION_TEXT_HEADER_ONE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_h1)
                        title.setText(R.string.option_text_header_one)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_header_one)
                    }
                    OPTION_TEXT_HEADER_TWO -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_h2)
                        title.setText(R.string.option_text_header_two)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_header_two)
                    }
                    OPTION_TEXT_HEADER_THREE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_h3)
                        title.setText(R.string.option_text_header_three)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_header_three)
                    }
                    OPTION_TEXT_HIGHLIGHTED -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_highlighted)
                        title.setText(R.string.option_text_highlighted)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_highlighted)
                    }
                    OPTION_LIST_CHECKBOX -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_checkbox)
                        title.setText(R.string.option_list_checkbox)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_checkbox)
                    }
                    OPTION_LIST_BULLETED_LIST -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_bulleted)
                        title.setText(R.string.option_list_bulleted_list)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_bulleted)
                    }
                    OPTION_LIST_NUMBERED_LIST -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_numbered)
                        title.setText(R.string.option_list_numbered_list)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_numbered)
                    }
                    OPTION_LIST_TOGGLE_LIST -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_numbered)
                        title.setText(R.string.option_list_numbered_list)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_numbered)
                    }
                    OPTION_PAGE_PAGE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_page)
                        title.setText(R.string.option_tool_page)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_page)
                    }
                    OPTION_PAGE_EXISTING_PAGE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_existing_page)
                        title.setText(R.string.option_tool_existing_page)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_existing_page)
                    }
                    OPTION_OBJECTS_FILE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_file)
                        title.setText(R.string.option_media_file)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_file)
                    }
                    OPTION_OBJECTS_IMAGE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_picture)
                        title.setText(R.string.option_media_picture)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_image)
                    }
                    OPTION_OBJECTS_VIDEO -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_video)
                        title.setText(R.string.option_media_video)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_video)
                    }
                    OPTION_OBJECTS_BOOKMARK -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_bookmark)
                        title.setText(R.string.option_media_bookmark)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_bookmark)
                    }
                    OPTION_OBJECTS_CODE -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_code)
                        title.setText(R.string.option_media_code)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_code)
                    }
                    OPTION_OTHER_LINE_DIVIDER -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_line_divider)
                        title.setText(R.string.option_other_divider)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_line_divider)
                    }
                    OPTION_OTHER_DOTS -> {
                        icon.setBackgroundResource(R.drawable.ic_add_block_or_turn_into_three_dots_divider)
                        title.setText(R.string.option_other_dots)
                        subtitle.setText(R.string.add_block_or_turn_into_subtitle_dots)
                    }
                    else -> throw IllegalStateException("Unexpected type: ${item.type}")
                }

                itemView.setOnClickListener { onOptionClicked(item.type) }
            }
        }

    }

    sealed class AddBlockView : ViewType {
        data class Section(val type: Int) : AddBlockView() {
            override fun getViewType(): Int = VIEW_HOLDER_SECTION
        }

        data class Item(val type: Int) : AddBlockView() {
            override fun getViewType(): Int = VIEW_HOLDER_ITEM
        }
    }

    object AddBlockConfig {
        const val OPTION_TEXT_TEXT = 1
        const val OPTION_TEXT_HEADER_ONE = 2
        const val OPTION_TEXT_HEADER_TWO = 3
        const val OPTION_TEXT_HEADER_THREE = 4
        const val OPTION_TEXT_HIGHLIGHTED = 5

        const val OPTION_LIST_CHECKBOX = 6
        const val OPTION_LIST_BULLETED_LIST = 7
        const val OPTION_LIST_NUMBERED_LIST = 8
        const val OPTION_LIST_TOGGLE_LIST = 9

        const val OPTION_PAGE_PAGE = 10
        const val OPTION_PAGE_EXISTING_PAGE = 11

        const val OPTION_OBJECTS_FILE = 12
        const val OPTION_OBJECTS_IMAGE = 13
        const val OPTION_OBJECTS_VIDEO = 14
        const val OPTION_OBJECTS_BOOKMARK = 15
        const val OPTION_OBJECTS_CODE = 16

        const val OPTION_OTHER_LINE_DIVIDER = 17
        const val OPTION_OTHER_DOTS = 18

        const val SECTION_TEXT = 1
        const val SECTION_LIST = 2
        const val SECTION_PAGE = 3
        const val SECTION_OBJECTS = 4
        const val SECTION_OTHER = 5
    }

    companion object {
        const val VIEW_HOLDER_SECTION = 0
        const val VIEW_HOLDER_ITEM = 1

        fun defaultViews(): List<AddBlockView> = listOf(
            AddBlockView.Section(SECTION_TEXT),
            AddBlockView.Item(OPTION_TEXT_TEXT),
            AddBlockView.Item(OPTION_TEXT_HEADER_ONE),
            AddBlockView.Item(OPTION_TEXT_HEADER_TWO),
            AddBlockView.Item(OPTION_TEXT_HEADER_THREE),
            AddBlockView.Item(OPTION_TEXT_HIGHLIGHTED),
            AddBlockView.Section(SECTION_LIST),
            AddBlockView.Item(OPTION_LIST_CHECKBOX),
            AddBlockView.Item(OPTION_LIST_BULLETED_LIST),
            AddBlockView.Item(OPTION_LIST_NUMBERED_LIST),
            AddBlockView.Item(OPTION_LIST_TOGGLE_LIST),
            AddBlockView.Section(SECTION_PAGE),
            AddBlockView.Item(OPTION_PAGE_PAGE),
            AddBlockView.Item(OPTION_PAGE_EXISTING_PAGE),
            AddBlockView.Section(SECTION_OBJECTS),
            AddBlockView.Item(OPTION_OBJECTS_FILE),
            AddBlockView.Item(OPTION_OBJECTS_IMAGE),
            AddBlockView.Item(OPTION_OBJECTS_VIDEO),
            AddBlockView.Item(OPTION_OBJECTS_BOOKMARK),
            AddBlockView.Item(OPTION_OBJECTS_CODE),
            AddBlockView.Section(SECTION_OTHER),
            AddBlockView.Item(OPTION_OTHER_LINE_DIVIDER),
            AddBlockView.Item(OPTION_OTHER_DOTS)
        )
    }
}