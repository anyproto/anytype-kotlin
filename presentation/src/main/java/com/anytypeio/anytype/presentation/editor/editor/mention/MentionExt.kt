package com.anytypeio.anytype.presentation.editor.editor.mention

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionConst.MENTION_PREFIX
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionConst.MENTION_TITLE_EMPTY
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

fun String.getMentionName(untitled: String): String = if (this.isBlank()) untitled else this

/**
 * Filter all mentions by text without symbol @
 *
 */
fun List<DefaultObjectView>.filterMentionsBy(text: String): List<DefaultObjectView> {
    val filter = text.removePrefix(MENTION_PREFIX)
    return if (filter.isNotEmpty()) this.filter {
        it.name.contains(
            filter,
            ignoreCase = true
        )
    } else this
}

object MentionConst {
    const val MENTION_PREFIX = "@"
    const val MENTION_TITLE_EMPTY = "Untitled"
}

fun Block.Content.Text.Mark.createMentionMarkup(
    obj: ObjectWrapper.Basic?,
    urlBuilder: UrlBuilder
): Markup.Mark? {

    val p = param

    if (p.isNullOrBlank()) return null

    if (obj == null) {
        return Markup.Mark.Mention.Loading(
            from = range.first,
            to = range.last,
            param = p
        )
    }

    if (obj.isDeleted == true) {
        return Markup.Mark.Mention.Deleted(
            from = range.first,
            to = range.last,
            param = p
        )
    }

    val emoji = obj.iconEmoji
    val image = obj.iconImage
    val initials = (obj.name ?: MENTION_TITLE_EMPTY).let {
        if (it.isEmpty()) MENTION_TITLE_EMPTY.first() else it.first()
    }

    return when (obj.layout) {

        ObjectType.Layout.BASIC -> createBaseMentionMark(
            from = range.first,
            to = range.last,
            param = p,
            emoji  = emoji,
            image = image,
            urlBuilder = urlBuilder
        )

        ObjectType.Layout.PROFILE -> {
            if (image.isNullOrBlank()) {
                Markup.Mark.Mention.Profile.WithInitials(
                    from = range.first,
                    to = range.last,
                    param = p,
                    initials = initials
                )
            } else {
                Markup.Mark.Mention.Profile.WithImage(
                    from = range.first,
                    to = range.last,
                    imageUrl = urlBuilder.thumbnail(image),
                    param = p
                )
            }
        }

        ObjectType.Layout.TODO -> {
            if (obj.done == true) {
                Markup.Mark.Mention.Task.Checked(
                    from = range.first,
                    to = range.last,
                    param = p
                )
            } else {
                Markup.Mark.Mention.Task.Unchecked(
                    from = range.first,
                    to = range.last,
                    param = p
                )
            }
        }

        else -> createBaseMentionMark(
            from = range.first,
            to = range.last,
            param = p,
            emoji  = emoji,
            image = image,
            urlBuilder = urlBuilder
        )
    }
}

private fun createBaseMentionMark(
    from: Int,
    to: Int,
    param: String,
    emoji: String?,
    image: String?,
    urlBuilder: UrlBuilder
): Markup.Mark.Mention {

    if (!emoji.isNullOrEmpty()) {
        return Markup.Mark.Mention.WithEmoji(
            from = from,
            to = to,
            emoji = emoji,
            param = param
        )
    }

    if (!image.isNullOrEmpty()) {
        return Markup.Mark.Mention.WithImage(
            from = from,
            to = to,
            image = urlBuilder.thumbnail(image),
            param = param
        )
    }

    return Markup.Mark.Mention.Base(
        from = from,
        to = to,
        param = param
    )
}