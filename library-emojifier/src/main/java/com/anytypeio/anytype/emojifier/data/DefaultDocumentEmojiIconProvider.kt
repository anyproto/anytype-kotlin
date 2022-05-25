package com.anytypeio.anytype.emojifier.data

import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

class DefaultDocumentEmojiIconProvider : DocumentEmojiIconProvider {

    companion object {
        val DOCUMENT_SET = listOf(
            "🌳", "⛳", "🧬", "🎈", "🎓",
            "💡", "🎒", "🚀", "🤖", "📚",
            "🍎", "🏡", "🤝", "😍", "☕",
            "🔥", "💥", "✍", "⏳", "📌",
            "🖍", "🦉", "📮", "📄", "🖌",
            "🗳", "⏰", "🔑", "🎉", "🗃",
            "🔖", "🧠", "👁", "🎗", "🎲",
            "🏙", "🚲", "⚙", "🔶", "🌍",
            "🏕", "🎡", "🌵", "🚗", "🚂",
            "🖼", "⭐", "🥁", "🛴", "🛫",
            "🏔", "🏗", "🛠", "🔍", "🕹",
            "🛋", "🎁", "🧮", "🏜", "🌋",
            "🎇", "🍏", "💫", "🌿", "🦊",
            "🍁", "🐎", "🍋", "🏍", "⛵"
        )
    }

    override fun random(): String = DOCUMENT_SET.random()
}