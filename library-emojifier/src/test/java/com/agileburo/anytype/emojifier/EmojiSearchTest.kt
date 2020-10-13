package com.agileburo.anytype.emojifier

import com.anytypeio.anytype.emojifier.Emojifier
import org.junit.Test

class EmojiSearchTest {

    @Test
    fun `should find uri for all emojis`() {

        val emojis = listOf(
            "⛰️", "\uD83C\uDFD8️",
            "⛰️", "⚔️", "\uD83C\uDFDB️",
            "▶️", "\uD83D\uDDE3️", "⛩️",
            "\uD83D\uDC87\u200D♂️", "⚙️",
            "☺️", "\uD83D\uDDC4️", "\uD83D\uDDD2️",
            "\uD83D\uDD78️", "\uD83D\uDE47\u200D♀️",
            "\uD83C\uDF21️", "\uD83D\uDDB2️",
            "✴️", "⌨️", "\uD83E\uDDD1\uD83C\uDFFD\u200D✈️",
            "\uD83D\uDC69\uD83C\uDFFC\u200D⚕️", "☂️"
        )

        emojis.forEach { unicode ->
            Emojifier.uri(unicode)
        }
    }
}