package com.agileburo.anytype.emojifier

import com.agileburo.anytype.emojifier.data.Emoji

object Emojifier {

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
        val (page, index) = search(unicode)
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
     * @return a pair constisting of emoji's page and emoji's index for this [unicode]
     */
    @Throws(IllegalStateException::class)
    private fun search(unicode: String): Pair<Int, Int> {
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
        return result ?: throw IllegalStateException("Result not found for: $unicode")
    }

    object Config {
        const val EMOJI_FILE = "emoji.json"
    }
}