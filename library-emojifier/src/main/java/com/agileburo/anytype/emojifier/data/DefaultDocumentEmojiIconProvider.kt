package com.agileburo.anytype.emojifier.data

import com.agileburo.anytype.domain.icon.DocumentEmojiIconProvider

class DefaultDocumentEmojiIconProvider : DocumentEmojiIconProvider {

    companion object {
        val DOCUMENT_SET = listOf(
            "🌳", "⛳", "🧬", "🎈", "🎓",
            "💡", "🎒", "🚀", "🤖", "📚",
            "🍎", "🏡", "🤝", "😍", "☕",
            "🔥", "💥", "✍", "⏳", "📌",
            "🚩", "🦉", "📮", "📄", "🖌",
            "🗳", "⏰", "🔑", "🎉", "🗃",
            "🔖", "🧠", "👁", "🎗", "🎲",
            "🧩", "🚲", "⚙", "🔶", "🌍",
            "🏕", "🧳", "🌵", "🚗", "🚂",
            "🖼", "⭐", "🥁", "🚠", "🛫",
            "🏔", "🏗", "🛠", "🔍", "🕹"
        )
    }

    override fun random(): String = DOCUMENT_SET.random()
}