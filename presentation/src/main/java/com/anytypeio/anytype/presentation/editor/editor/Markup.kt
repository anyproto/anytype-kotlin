package com.anytypeio.anytype.presentation.editor.editor

import android.text.Spannable

/**
 * Classes implementing this interface should support markup rendering.
 */
interface Markup {

    /**
     * A text body that this markup should be applied to.
     */
    val body: String

    /**
     * List of marks associated with the text body.
     */
    var marks: List<Mark>

    sealed class Mark {

        /**
         * @property from character index where this markup starts (inclusive)
         * @property to character index where this markup ends (inclusive)
         **/

        abstract val from: Int
        abstract val to: Int

        data class Bold(override val from: Int, override val to: Int) : Mark()
        data class Italic(override val from: Int, override val to: Int) : Mark()
        data class Strikethrough(override val from: Int, override val to: Int) : Mark()
        data class Keyboard(override val from: Int, override val to: Int) : Mark()
        data class TextColor(
            override val from: Int,
            override val to: Int,
            val color: String
        ) : Mark() {

            fun color(): Int? =
                ThemeColor.values().find { it.title == color }?.text
        }

        data class BackgroundColor(
            override val from: Int,
            override val to: Int,
            val background: String
        ) : Mark() {

            fun background(): Int? =
                ThemeColor.values().find { it.title == background }?.background
        }

        data class Link(
            override val from: Int,
            override val to: Int,
            val param: String
        ) : Mark()

        data class Object(
            override val from: Int,
            override val to: Int,
            val param: String
        ) : Mark()

        sealed class Mention : Mark() {
            abstract val param: String

            data class Base(
                override val from: Int,
                override val to: Int,
                override val param: String
            ) : Mention()

            data class WithEmoji(
                override val from: Int,
                override val to: Int,
                override val param: String,
                val emoji: String
            ) : Mention()

            data class WithImage(
                override val from: Int,
                override val to: Int,
                override val param: String,
                val image: String
            ) : Mention()

            data class Loading(
                override val from: Int,
                override val to: Int,
                override val param: String
            ) : Mention()

            data class Deleted(
                override val from: Int,
                override val to: Int,
                override val param: String
            ) : Mention()

            sealed class Profile : Mention() {
                data class WithImage(
                    override val from: Int,
                    override val to: Int,
                    override val param: String,
                    val imageUrl: String
                ) : Profile()

                data class WithInitials(
                    override val from: Int,
                    override val to: Int,
                    override val param: String,
                    val initials: Char
                ) : Profile()
            }

            sealed class Task : Mention(){
                data class Checked(
                    override val from: Int,
                    override val to: Int,
                    override val param: String
                ) : Task()

                data class Unchecked(
                    override val from: Int,
                    override val to: Int,
                    override val param: String
                ) : Task()
            }
        }
    }

    /**
     * Markup types.
     */
    enum class Type {
        ITALIC,
        BOLD,
        STRIKETHROUGH,
        TEXT_COLOR,
        BACKGROUND_COLOR,
        LINK,
        KEYBOARD,
        MENTION,
        OBJECT
    }

    companion object {
        const val DEFAULT_SPANNABLE_FLAG = Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        const val MENTION_SPANNABLE_FLAG = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        const val SPAN_MONOSPACE = "monospace"
        const val NON_EXISTENT_OBJECT_MENTION_NAME = "Non-existent object"
    }
}