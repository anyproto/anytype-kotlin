package com.agileburo.anytype.emojifier

import com.agileburo.anytype.domain.emoji.Emoji
import com.agileburo.anytype.domain.emoji.Emojifier
import com.vdurmont.emoji.EmojiManager

class DefaultEmojifier : Emojifier {

    override suspend fun fromAlias(alias: String): Emoji {
        check(alias.isNotEmpty()) { "Alias cannot be empty" }
        return EmojiManager.getForAlias(alias).let { result ->
            Emoji(
                /**
                 * Fix pirate flag emoji render, after fixing
                 * in table https://github.com/vdurmont/emoji-java/blob/master/EMOJIS.md
                 * can be removed
                 */
                unicode = result.unicode.filterTextByChar(
                    value = '☠',
                    filterBy = '♾'
                ),
                alias = result.aliases.first()
            )
        }
    }

    override suspend fun fromShortName(name: String): Emoji {
        check(name.isNotEmpty()) { "Short name cannot be empty" }
        val alias = name.substring(1, name.length - 1)
        return fromAlias(alias)
    }
}

fun String.filterTextByChar(value: Char, filterBy: Char): String =
    if (contains(value)) {
        filterNot { it == filterBy }
    } else {
        this
    }