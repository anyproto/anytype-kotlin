package com.anytypeio.anytype.core_ui.tools

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.tools.SlashTextWatcher.Companion.NO_SLASH_POSITION
import com.anytypeio.anytype.core_ui.tools.SlashTextWatcher.Companion.SLASH_CHAR
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem

object SlashHelper {

    fun isSlashCharAdded(text: CharSequence, start: Int, count: Int): Boolean =
        count == 1 && start < text.length && text[start] == SLASH_CHAR

    fun isSlashDeleted(start: Int, slashPosition: Int): Boolean =
        slashPosition != NO_SLASH_POSITION && start <= slashPosition

    /**
     * return subsequence from [startIndex] to end of sequence with limit [takeNumber]
     */
    fun getSubSequenceFromStartWithLimit(
        s: CharSequence,
        startIndex: Int,
        takeNumber: Int
    ): CharSequence = s.subSequence(startIndex = startIndex, endIndex = s.length).take(takeNumber)

    fun getSlashItems(filter: String): List<SlashItem> {
        if (filter.isEmpty()) {
            return mainList
        } else return listOf<SlashItem>()
    }

    val mainList = listOf<SlashItem>(
        SlashItem.Main.Style(
            title = R.string.slash_widget_main_style,
            icon = R.drawable.ic_slash_main_style
        ),
        SlashItem.Main.Media(
            title = R.string.slash_widget_main_media,
            icon = R.drawable.ic_slash_main_media
        ),
        SlashItem.Main.Objects(
            title = R.string.slash_widget_main_objects,
            icon = R.drawable.ic_slash_main_objects
        ),
        SlashItem.Main.Relations(
            title = R.string.slash_widget_main_relations,
            icon = R.drawable.ic_slash_main_relations
        ),
        SlashItem.Main.Other(
            title = R.string.slash_widget_main_other,
            icon = R.drawable.ic_slash_main_other
        ),
        SlashItem.Main.Actions(
            title = R.string.slash_widget_main_actions,
            icon = R.drawable.ic_slash_main_actions
        ),
        SlashItem.Main.Alignment(
            title = R.string.slash_widget_main_alignment,
            icon = R.drawable.ic_slash_main_alignment
        ),
        SlashItem.Main.Color(
            title = R.string.slash_widget_main_color,
            icon = R.drawable.ic_slash_main_color
        ),
        SlashItem.Main.Background(
            title = R.string.slash_widget_main_background,
            icon = R.drawable.ic_slash_main_rectangle
        )
    )

    val styleList = listOf<SlashItem>(
        SlashItem.Style.Type.Text(
            title = R.string.slash_widget_style_text,
            subtitle = R.string.slash_widget_style_text_subtitle,
            icon = R.drawable.ic_slash_style_text,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Title(
            title = R.string.slash_widget_style_title,
            subtitle = R.string.slash_widget_style_title_subtitle,
            icon = R.drawable.ic_slash_style_title,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Heading(
            title = R.string.slash_widget_style_heading,
            subtitle = R.string.slash_widget_style_heading_subtitle,
            icon = R.drawable.ic_slash_style_heading,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Subheading(
            title = R.string.slash_widget_style_subheading,
            subtitle = R.string.slash_widget_style_subheading_subtitle,
            icon = R.drawable.ic_slash_style_subheading,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Highlighted(
            title = R.string.slash_widget_style_highlighted,
            subtitle = R.string.slash_widget_style_highlighted_subtitle,
            icon = R.drawable.ic_slash_style_highlighted,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Callout(
            title = R.string.slash_widget_style_callout,
            subtitle = R.string.slash_widget_style_callout_subtitle,
            icon = R.drawable.ic_slash_style_callout,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Checkbox(
            title = R.string.slash_widget_style_checkbox,
            subtitle = R.string.slash_widget_style_checkbox_subtitle,
            icon = R.drawable.ic_slash_style_checkbox,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Bulleted(
            title = R.string.slash_widget_style_bulleted,
            subtitle = R.string.slash_widget_style_bulleted_subtitle,
            icon = R.drawable.ic_slash_style_bulleted,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Numbered(
            title = R.string.slash_widget_style_numbered,
            subtitle = R.string.slash_widget_style_numbered_subtitle,
            icon = R.drawable.ic_slash_style_numbered,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Type.Toggle(
            title = R.string.slash_widget_style_toggle,
            subtitle = R.string.slash_widget_style_toggle_subtitle,
            icon = R.drawable.ic_slash_style_toggle,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Markup.Bold(
            title = R.string.slash_widget_style_bold,
            icon = R.drawable.ic_slash_style_bold,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Markup.Italic(
            title = R.string.slash_widget_style_italic,
            icon = R.drawable.ic_slash_style_italic,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Markup.Breakthrough(
            title = R.string.slash_widget_style_breakthrough,
            icon = R.drawable.ic_slash_style_breakthrough,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Markup.Code(
            title = R.string.slash_widget_style_code,
            icon = R.drawable.ic_slash_style_code,
            category = SlashItem.CATEGORY_STYLE
        ),
        SlashItem.Style.Markup.Link(
            title = R.string.slash_widget_style_link,
            icon = R.drawable.ic_slash_style_link,
            category = SlashItem.CATEGORY_STYLE
        )
    )

    val mediaList = listOf<SlashItem>(
        SlashItem.Media.File(
            title = R.string.slash_widget_media_file,
            subtitle = R.string.slash_widget_media_file_subtitle,
            icon = R.drawable.ic_slash_media_file,
            category = SlashItem.CATEGORY_MEDIA
        ),
        SlashItem.Media.Picture(
            title = R.string.slash_widget_media_picture,
            subtitle = R.string.slash_widget_media_picture_subtitle,
            icon = R.drawable.ic_slash_media_picture,
            category = SlashItem.CATEGORY_MEDIA
        ),
        SlashItem.Media.Video(
            title = R.string.slash_widget_media_video,
            subtitle = R.string.slash_widget_media_video_subtitle,
            icon = R.drawable.ic_slash_media_video,
            category = SlashItem.CATEGORY_MEDIA
        ),
        SlashItem.Media.Bookmark(
            title = R.string.slash_widget_media_bookmark,
            subtitle = R.string.slash_widget_media_bookmark_subtitle,
            icon = R.drawable.ic_slash_media_bookmark,
            category = SlashItem.CATEGORY_MEDIA
        ),
        SlashItem.Media.Code(
            title = R.string.slash_widget_media_code,
            subtitle = R.string.slash_widget_media_code_subtitle,
            icon = R.drawable.ic_slash_media_code,
            category = SlashItem.CATEGORY_MEDIA
        )
    )

    val otherList = listOf<SlashItem>(
        SlashItem.Other.Line(
            title = R.string.slash_widget_other_line,
            icon = R.drawable.ic_slash_other_line,
            category = SlashItem.CATEGORY_OTHER
        ),
        SlashItem.Other.Dots(
            title = R.string.slash_widget_other_dots,
            icon = R.drawable.ic_slash_other_dots,
            category = SlashItem.CATEGORY_OTHER
        )
    )

    val actionsList = listOf<SlashItem>(
        SlashItem.Actions.Delete(
            title = R.string.slash_widget_actions_delete,
            icon = R.drawable.ic_slash_actions_delete,
            category = SlashItem.CATEGORY_ACTIONS
        ),
        SlashItem.Actions.Duplicate(
            title = R.string.slash_widget_actions_duplicate,
            icon = R.drawable.ic_slash_actions_duplicate,
            category = SlashItem.CATEGORY_ACTIONS
        ),
        SlashItem.Actions.Copy(
            title = R.string.slash_widget_actions_copy,
            icon = R.drawable.ic_slash_actions_copy,
            category = SlashItem.CATEGORY_ACTIONS
        ),
        SlashItem.Actions.Paste(
            title = R.string.slash_widget_actions_paste,
            icon = R.drawable.ic_slash_actions_paste,
            category = SlashItem.CATEGORY_ACTIONS
        ),
        SlashItem.Actions.Move(
            title = R.string.slash_widget_actions_move,
            icon = R.drawable.ic_slash_actions_move,
            category = SlashItem.CATEGORY_ACTIONS
        ),
        SlashItem.Actions.MoveTo(
            title = R.string.slash_widget_actions_moveto,
            icon = R.drawable.ic_slash_actions_move_to,
            category = SlashItem.CATEGORY_ACTIONS
        ),
        SlashItem.Actions.CleanStyle(
            title = R.string.slash_widget_actions_clean_style,
            icon = R.drawable.ic_slash_actions_clean_style,
            category = SlashItem.CATEGORY_ACTIONS
        )
    )

    val alignmentList = listOf<SlashItem>(
        SlashItem.Alignment.Left(
            title = R.string.slash_widget_align_left,
            icon = R.drawable.ic_slash_align_left,
            category = SlashItem.CATEGORY_ALIGNMENT
        ),
        SlashItem.Alignment.Center(
            title = R.string.slash_widget_align_center,
            icon = R.drawable.ic_slash_align_center,
            category = SlashItem.CATEGORY_ALIGNMENT
        ),
        SlashItem.Alignment.Right(
            title = R.string.slash_widget_align_right,
            icon = R.drawable.ic_slash_align_right,
            category = SlashItem.CATEGORY_ALIGNMENT
        )
    )
}