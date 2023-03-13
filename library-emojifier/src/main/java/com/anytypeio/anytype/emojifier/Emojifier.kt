package com.anytypeio.anytype.emojifier

import com.anytypeio.anytype.emojifier.data.Emoji

object Emojifier {

    private const val EMOJI_SEPARATOR_INT = 65039
    private const val SEPARATOR = EMOJI_SEPARATOR_INT.toChar()

    /**
     * cache for [search] results.
     */
    private val cache = mutableMapOf<String, Pair<Int, Int>>()

    /**
     * @param unicode emoji unicode
     * @return uri for loading emoji as image
     */
    @Throws(IllegalStateException::class)
    fun uri(unicode: String): String {
        var result = search(unicode)

        if (result == null) {
            if (unicode.last() == SEPARATOR) {
                val sb = StringBuilder()
                unicode.forEachIndexed { index, char ->
                    if (index < unicode.length.dec()) sb.append(char)
                }
                result = search(sb.toString())
            }
        }

        checkNotNull(result) { "Could not find emoji for: $unicode" }

        val (page, index) = result

        return uri(page, index)
    }

    /**
     * @param page emoji's page (emoji category)
     * @param index emoji's index on the [page]
     * @return uri for loading emoji as image
     */
    fun uri(page: Int, index: Int): String {
        return "file:///android_asset/emoji/${page}_${index}.png"
    }

    /**
     * @param unicode emoji unicode
     * @return a pair consisting of emoji's page and emoji's index for this [unicode]
     */
    private fun search(unicode: String): Pair<Int, Int>? {
        val cached = cache[unicode]

        if (cached != null) return cached

        var result: Pair<Int, Int>? = null

        Emoji.DATA.forEachIndexed { categoryIndex, emojis ->
            val emojiIndex = emojis.indexOfFirst { emoji -> emoji == unicode }
            if (emojiIndex != -1) {
                val pair = Pair(categoryIndex, emojiIndex)
                result = pair
                cache[unicode] = pair
                return@forEachIndexed
            }
        }
        return result
    }

    object Config {
        const val EMOJI_FILE = "emoji.json"
    }

}