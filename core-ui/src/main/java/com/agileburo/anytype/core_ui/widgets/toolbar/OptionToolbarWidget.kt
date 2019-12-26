package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.ViewType
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.drawable
import com.agileburo.anytype.core_ui.layout.SpacingItemDecoration
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.*
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionAdapter.Companion.LIST_TYPE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionAdapter.Companion.MEDIA_TYPE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionAdapter.Companion.OTHER_TYPE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionAdapter.Companion.TEXT_TYPE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionAdapter.Companion.TOOL_TYPE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_BULLETED_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_CHECKBOX
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_NUMBERED_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_LIST_TOGGLE_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_BOOKMARK
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_CODE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_FILE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_PICTURE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_MEDIA_VIDEO
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_OTHER_DIVIDER
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_ONE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_THREE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HEADER_TWO
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_HIGHLIGHTED
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TEXT_TEXT
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_CONTACT
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_DB
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_EXISTING_TOOL
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_PAGE
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_SET
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig.OPTION_TOOL_TASK
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.SectionConfig.SECTION_LIST
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.SectionConfig.SECTION_MEDIA
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.SectionConfig.SECTION_OTHER
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.SectionConfig.SECTION_TEXT
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.SectionConfig.SECTION_TOOL
import kotlinx.android.synthetic.main.item_bottom_detail_toolbar_option.view.*
import kotlinx.android.synthetic.main.widget_bottom_detail_toolbar.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * This toolbar widget provides user with different types of options
 * for converting or adding different types of blocks.
 * These options are rendered as scrollable lists.
 * @see Option
 * @see OptionAdapter
 * @see Section
 * @see SectionAdapter
 */
class OptionToolbarWidget : LinearLayout {

    private val channel = Channel<Option>()

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setup()
        setupRecycler()
        header.text = "Add block"
    }

    private fun setup() {
        LayoutInflater.from(context).inflate(R.layout.widget_bottom_detail_toolbar, this)
        orientation = VERTICAL
        setBackgroundResource(R.color.default_bottom_detail_toolbar_background_color)
    }

    private fun setupRecycler() {

        val options = listOf(
            Option.Text(type = OPTION_TEXT_TEXT),
            Option.Text(type = OPTION_TEXT_HEADER_ONE),
            Option.Text(type = OPTION_TEXT_HEADER_TWO),
            Option.Text(type = OPTION_TEXT_HEADER_THREE),
            Option.Text(type = OPTION_TEXT_HIGHLIGHTED),
            Option.List(type = OPTION_LIST_CHECKBOX),
            Option.List(type = OPTION_LIST_BULLETED_LIST),
            Option.List(type = OPTION_LIST_NUMBERED_LIST),
            Option.List(type = OPTION_LIST_TOGGLE_LIST),
            Option.Tool(type = OPTION_TOOL_TASK),
            Option.Tool(type = OPTION_TOOL_PAGE),
            Option.Tool(type = OPTION_TOOL_DB),
            Option.Tool(type = OPTION_TOOL_SET),
            Option.Tool(type = OPTION_TOOL_CONTACT),
            Option.Tool(type = OPTION_TOOL_EXISTING_TOOL),
            Option.Media(type = OPTION_MEDIA_FILE),
            Option.Media(type = OPTION_MEDIA_PICTURE),
            Option.Media(type = OPTION_MEDIA_VIDEO),
            Option.Media(type = OPTION_MEDIA_BOOKMARK),
            Option.Media(type = OPTION_MEDIA_CODE),
            Option.Other(type = OPTION_OTHER_DIVIDER)
        )

        val sections = mutableListOf(
            Section(
                selected = true,
                type = SECTION_TEXT
            ),
            Section(
                selected = false,
                type = SECTION_LIST
            ),
            Section(
                selected = false,
                type = SECTION_TOOL
            ),
            Section(
                selected = false,
                type = SECTION_MEDIA
            ),
            Section(
                selected = false,
                type = SECTION_OTHER
            )
        )


        val sectionAdapter = SectionAdapter(
            sections = sections,
            onSectionClicked = { selected ->
                scrollToOptionSection(selected, options)
                val update = sections.map { section ->
                    section.copy(selected = section.type == selected.type)
                }
                (sectionRecycler.adapter as SectionAdapter).update(update = update)
            }
        )

        sectionRecycler.apply {

            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            adapter = sectionAdapter

            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = context
                        .resources
                        .getDimension(R.dimen.default_toolbar_section_item_spacing)
                        .toInt(),
                    firstItemSpacingStart = context
                        .resources
                        .getDimension(R.dimen.default_toolbar_section_item_spacing_first)
                        .toInt(),
                    lastItemSpacingEnd = context
                        .resources
                        .getDimension(R.dimen.default_toolbar_section_item_spacing_last)
                        .toInt()
                )
            )
        }

        optionRecycler.apply {

            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            adapter = OptionAdapter(
                options = options,
                onOptionClicked = channel::sendBlocking
            )

            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = context
                        .resources
                        .getDimension(R.dimen.default_bottom_toolbar_option_item_spacing)
                        .toInt(),
                    firstItemSpacingStart = context
                        .resources
                        .getDimension(R.dimen.default_bottom_toolbar_option_item_spacing_first)
                        .toInt(),
                    lastItemSpacingEnd = context
                        .resources
                        .getDimension(R.dimen.default_bottom_toolbar_option_item_spacing_last)
                        .toInt()
                )
            )
        }
    }

    private fun scrollToOptionSection(
        section: Section,
        options: List<Option>
    ) {
        when (section.type) {
            SECTION_TEXT -> {
                val index = options.indexOfFirst { it.getViewType() == TEXT_TYPE }
                optionRecycler.smoothScrollToPosition(index)
            }
            SECTION_LIST -> {
                val index = options.indexOfFirst { it.getViewType() == LIST_TYPE }
                optionRecycler.smoothScrollToPosition(index)
            }
            SECTION_TOOL -> {
                val index = options.indexOfFirst { it.getViewType() == TOOL_TYPE }
                optionRecycler.smoothScrollToPosition(index)
            }
            SECTION_MEDIA -> {
                val index = options.indexOfFirst { it.getViewType() == MEDIA_TYPE }
                optionRecycler.smoothScrollToPosition(index)
            }
            SECTION_OTHER -> {
                val index = options.indexOfFirst { it.getViewType() == OTHER_TYPE }
                optionRecycler.smoothScrollToPosition(index)
            }
        }
    }

    /**
     * Adapter for rendering list of sections
     * @property sections mutable list of sections
     * @property onSectionClicked callback for click action
     * @see Section
     */
    class SectionAdapter(
        val sections: MutableList<Section>,
        private val onSectionClicked: (Section) -> Unit
    ) : RecyclerView.Adapter<SectionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_toolbar_section,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int = sections.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(
                section = sections[position],
                onSectionClicked = onSectionClicked
            )
        }

        fun update(update: List<Section>) {
            sections.clear()
            sections.addAll(update)
            notifyDataSetChanged()
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val container = itemView
            private val title = itemView.title

            fun bind(
                section: Section,
                onSectionClicked: (Section) -> Unit
            ) {

                itemView.setOnClickListener { onSectionClicked(section) }

                title.isSelected = section.selected

                when (section.type) {
                    SECTION_TEXT -> {
                        title.setText(R.string.toolbar_section_text)
                        setBackground(
                            selected = section.selected,
                            color = itemView.context.color(R.color.toolbar_section_text)
                        )
                    }
                    SECTION_LIST -> {
                        title.setText(R.string.toolbar_section_list)
                        setBackground(
                            selected = section.selected,
                            color = itemView.context.color(R.color.toolbar_section_list)
                        )
                    }
                    SECTION_TOOL -> {
                        title.setText(R.string.toolbar_section_tool)
                        setBackground(
                            selected = section.selected,
                            color = itemView.context.color(R.color.toolbar_section_tool)
                        )
                    }
                    SECTION_MEDIA -> {
                        title.setText(R.string.toolbar_section_media)
                        setBackground(
                            selected = section.selected,
                            color = itemView.context.color(R.color.toolbar_section_media)
                        )
                    }
                    SECTION_OTHER -> {
                        title.setText(R.string.toolbar_section_other)
                        setBackground(
                            selected = section.selected,
                            color = itemView.context.color(R.color.toolbar_section_other)
                        )
                    }
                }

            }

            private fun setBackground(
                selected: Boolean,
                color: Int
            ) {
                container.apply {
                    background = context
                        .drawable(R.drawable.rectangle_toolbar_default_chapter)
                        .also { drawable ->
                            if (selected)
                                drawable.setTint(color)
                            else
                                drawable.setTint(Color.WHITE)
                        }
                }
            }
        }
    }

    /**
     * Adapter for rendering option list.
     * @property options immutable list of options
     * @see Option
     */
    class OptionAdapter(
        private val options: List<Option>,
        private val onOptionClicked: (Option) -> Unit
    ) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val inflater = LayoutInflater.from(parent.context)

            return when (viewType) {
                TEXT_TYPE -> {
                    inflater
                        .inflate(R.layout.item_bottom_detail_toolbar_option, parent, false)
                        .let { view -> ViewHolder.TextOptionHolder(view) }
                }
                LIST_TYPE -> {
                    inflater
                        .inflate(R.layout.item_bottom_detail_toolbar_option, parent, false)
                        .let { view -> ViewHolder.ListOptionHolder(view) }
                }
                TOOL_TYPE -> {
                    inflater
                        .inflate(R.layout.item_bottom_detail_toolbar_option, parent, false)
                        .let { view -> ViewHolder.ToolOptionHolder(view) }
                }
                MEDIA_TYPE -> {
                    inflater
                        .inflate(R.layout.item_bottom_detail_toolbar_option, parent, false)
                        .let { view -> ViewHolder.MediaOptionHolder(view) }
                }
                OTHER_TYPE -> {
                    inflater
                        .inflate(R.layout.item_bottom_detail_toolbar_option, parent, false)
                        .let { view -> ViewHolder.OtherOptionHolder(view) }
                }
                else -> throw IllegalStateException("Unexpected view type: $viewType")
            }
        }

        override fun getItemViewType(position: Int): Int = options[position].getViewType()

        override fun getItemCount(): Int = options.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (holder) {
                is ViewHolder.TextOptionHolder -> {
                    holder.bind(
                        option = options[position] as Option.Text,
                        onOptionClicked = onOptionClicked
                    )
                }
                is ViewHolder.ListOptionHolder -> {
                    holder.bind(
                        option = options[position] as Option.List,
                        onOptionClicked = onOptionClicked
                    )
                }
                is ViewHolder.ToolOptionHolder -> {
                    holder.bind(
                        option = options[position] as Option.Tool,
                        onOptionClicked = onOptionClicked
                    )
                }
                is ViewHolder.MediaOptionHolder -> {
                    holder.bind(
                        option = options[position] as Option.Media,
                        onOptionClicked = onOptionClicked
                    )
                }
                is ViewHolder.OtherOptionHolder -> {
                    holder.bind(
                        option = options[position] as Option.Other,
                        onOptionClicked = onOptionClicked
                    )
                }
            }
        }

        sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun bindClick(
                option: Option,
                onOptionClicked: (Option) -> Unit
            ) {
                itemView.setOnClickListener { onOptionClicked(option) }
            }

            class TextOptionHolder(view: View) : ViewHolder(view) {

                private val pic = itemView.pic
                private val title = itemView.title

                fun bind(
                    option: Option.Text,
                    onOptionClicked: (Option) -> Unit
                ) {

                    bindClick(option, onOptionClicked)

                    when (option.type) {
                        OPTION_TEXT_TEXT -> {
                            title.setText(R.string.option_text_text)
                            pic.setImageResource(R.drawable.ic_toolbar_option_text)
                        }
                        OPTION_TEXT_HEADER_ONE -> {
                            title.setText(R.string.option_text_header_one)
                            pic.setImageResource(R.drawable.ic_toolbar_option_header_one)
                        }
                        OPTION_TEXT_HEADER_TWO -> {
                            title.setText(R.string.option_text_header_two)
                            pic.setImageResource(R.drawable.ic_toolbar_option_header_two)
                        }
                        OPTION_TEXT_HEADER_THREE -> {
                            title.setText(R.string.option_text_header_three)
                            pic.setImageResource(R.drawable.ic_toolbar_option_header_three)
                        }
                        OPTION_TEXT_HIGHLIGHTED -> {
                            title.setText(R.string.option_text_highlighted)
                            pic.setImageResource(R.drawable.ic_toolbar_option_highlight)
                        }
                        else -> throw IllegalStateException("Unexpected text option type: ${option.type}")
                    }
                }
            }

            class ListOptionHolder(view: View) : ViewHolder(view) {

                private val pic = itemView.pic
                private val title = itemView.title

                fun bind(
                    option: Option.List,
                    onOptionClicked: (Option) -> Unit
                ) {

                    bindClick(option, onOptionClicked)

                    when (option.type) {
                        OPTION_LIST_CHECKBOX -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_checkbox)
                            title.setText(R.string.option_list_checkbox)
                        }
                        OPTION_LIST_BULLETED_LIST -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_bulleted)
                            title.setText(R.string.option_list_bulleted_list)
                        }
                        OPTION_LIST_NUMBERED_LIST -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_numbered)
                            title.setText(R.string.option_list_numbered_list)
                        }
                        OPTION_LIST_TOGGLE_LIST -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_toggle)
                            title.setText(R.string.option_list_toggle_list)
                        }
                        else -> throw IllegalStateException("Unexpected list option type: ${option.type}")
                    }
                }

            }

            class ToolOptionHolder(view: View) : ViewHolder(view) {

                private val pic = itemView.pic
                private val title = itemView.title

                fun bind(
                    option: Option.Tool,
                    onOptionClicked: (Option) -> Unit
                ) {

                    bindClick(option, onOptionClicked)

                    when (option.type) {
                        OPTION_TOOL_TASK -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_task)
                            title.setText(R.string.option_tool_task)
                        }
                        OPTION_TOOL_PAGE -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_page_or_database)
                            title.setText(R.string.option_tool_page)
                        }
                        OPTION_TOOL_DB -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_page_or_database)
                            title.setText(R.string.option_tool_database)
                        }
                        OPTION_TOOL_SET -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_set)
                            title.setText(R.string.option_tool_set)
                        }
                        OPTION_TOOL_CONTACT -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_contact)
                            title.setText(R.string.option_tool_contact)
                        }
                        OPTION_TOOL_EXISTING_TOOL -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_existing_tool)
                            title.setText(R.string.option_tool_existing_tool)
                        }
                        else -> throw IllegalStateException("Unexpected tool option type: ${option.type}")
                    }
                }

            }

            class MediaOptionHolder(view: View) : ViewHolder(view) {

                private val pic = itemView.pic
                private val title = itemView.title

                fun bind(
                    option: Option.Media,
                    onOptionClicked: (Option) -> Unit
                ) {

                    bindClick(option, onOptionClicked)

                    when (option.type) {
                        OPTION_MEDIA_FILE -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_file)
                            title.setText(R.string.option_media_file)
                        }
                        OPTION_MEDIA_PICTURE -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_picture)
                            title.setText(R.string.option_media_picture)
                        }
                        OPTION_MEDIA_VIDEO -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_video)
                            title.setText(R.string.option_media_video)
                        }
                        OPTION_MEDIA_CODE -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_code)
                            title.setText(R.string.option_media_code)
                        }
                        OPTION_MEDIA_BOOKMARK -> {
                            pic.setImageResource(R.drawable.ic_toolbar_option_bookmark)
                            title.setText(R.string.option_media_bookmark)
                        }
                    }
                }

            }

            class OtherOptionHolder(view: View) : ViewHolder(view) {

                private val pic = itemView.pic
                private val title = itemView.title

                fun bind(
                    option: Option.Other,
                    onOptionClicked: (Option) -> Unit
                ) {

                    bindClick(option, onOptionClicked)

                    if (option.type == OPTION_OTHER_DIVIDER) {
                        pic.setImageResource(R.drawable.ic_toolbar_option_divider)
                        title.setText(R.string.option_other_divider)
                    }
                }

            }
        }

        companion object {
            const val TEXT_TYPE = 0
            const val LIST_TYPE = 1
            const val TOOL_TYPE = 2
            const val MEDIA_TYPE = 3
            const val OTHER_TYPE = 4
        }
    }

    /**
     * Represents options of different types options for different actions (turn into, add block, etc.).
     * Options are currently grouped by type of blocks.
     * @see OptionConfig for different types of available options.
     */
    sealed class Option : ViewType {

        /**
         * This option is related to textual blocks.
         * @property type concrete type of textual block.
         * @see OptionConfig for different textual block types.
         */
        class Text(val type: Int) : Option() {
            override fun getViewType() = TEXT_TYPE
        }

        /**
         * This option is related to list blocks.
         * @property type concrete type of list block.
         * @see OptionConfig for different list block types.
         */
        class List(val type: Int) : Option() {
            override fun getViewType() = LIST_TYPE
        }

        /**
         * This option is related to tool blocks.
         * @property type concrete type of tool block.
         * @see OptionConfig for different tool block types.
         */
        class Tool(val type: Int) : Option() {
            override fun getViewType() = TOOL_TYPE
        }

        /**
         * This option is related to media blocks.
         * @property type concrete type of media block.
         * @see OptionConfig for different media block types.
         */
        class Media(val type: Int) : Option() {
            override fun getViewType() = MEDIA_TYPE
        }

        /**
         * This option is related to ungrouped (other) block types (such as divider block).
         * @property type concrete type of ungrouped (other) block type.
         * @see OptionConfig for available ungrouped (other) types
         */
        class Other(val type: Int) : Option() {
            override fun getViewType() = OTHER_TYPE
        }
    }

    /**
     * Option list consists of group of options (text, list, tool, media, other).
     * Sections allow user to navigate these groups more easily.
     * @property type section type (relates to option group)
     * @property selected determines whether this section is selected or not.
     * Selected sections are rendered differently.
     * @see SectionConfig for different section types.
     */
    data class Section(
        val type: Int,
        val selected: Boolean
    )

    /**
     * Constants for different types of options.
     * @see Option
     */
    object OptionConfig {
        const val OPTION_TEXT_TEXT = 1
        const val OPTION_TEXT_HEADER_ONE = 2
        const val OPTION_TEXT_HEADER_TWO = 3
        const val OPTION_TEXT_HEADER_THREE = 4
        const val OPTION_TEXT_HIGHLIGHTED = 5

        const val OPTION_LIST_CHECKBOX = 6
        const val OPTION_LIST_BULLETED_LIST = 7
        const val OPTION_LIST_NUMBERED_LIST = 8
        const val OPTION_LIST_TOGGLE_LIST = 9

        const val OPTION_TOOL_TASK = 10
        const val OPTION_TOOL_PAGE = 11
        const val OPTION_TOOL_DB = 12
        const val OPTION_TOOL_SET = 13
        const val OPTION_TOOL_CONTACT = 14
        const val OPTION_TOOL_EXISTING_TOOL = 15

        const val OPTION_MEDIA_FILE = 16
        const val OPTION_MEDIA_PICTURE = 17
        const val OPTION_MEDIA_VIDEO = 18
        const val OPTION_MEDIA_BOOKMARK = 19
        const val OPTION_MEDIA_CODE = 20

        const val OPTION_OTHER_DIVIDER = 21
    }

    /**
     * Constants for different types of sections.
     * @see Section
     * @see Option
     */
    object SectionConfig {
        const val SECTION_TEXT = 0
        const val SECTION_LIST = 1
        const val SECTION_TOOL = 2
        const val SECTION_MEDIA = 3
        const val SECTION_OTHER = 4
    }

    fun optionClicks() = channel.consumeAsFlow()
}