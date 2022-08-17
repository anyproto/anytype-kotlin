package com.anytypeio.anytype.presentation.editor.editor

import android.text.Spannable
import com.anytypeio.anytype.core_models.ThemeColor

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
        data class Underline(override val from: Int, override val to: Int) : Mark()
        data class Keyboard(override val from: Int, override val to: Int) : Mark()
        data class TextColor(
            override val from: Int,
            override val to: Int,
            val color: String
        ) : Mark() {

            fun color(): ThemeColor? = ThemeColor.values().find { it.code == color }
        }

        data class BackgroundColor(
            override val from: Int,
            override val to: Int,
            val background: String
        ) : Mark() {

            fun background(): ThemeColor? =
                ThemeColor.values().find { it.code == background }
        }

        data class Link(
            override val from: Int,
            override val to: Int,
            val param: String
        ) : Mark()

        data class Object(
            override val from: Int,
            override val to: Int,
            val param: String,
            val isArchived: Boolean
        ) : Mark()

        sealed class Mention : Mark() {
            abstract val param: String

            data class Base(
                override val from: Int,
                override val to: Int,
                override val param: String,
                val isArchived: Boolean
            ) : Mention()

            data class WithEmoji(
                override val from: Int,
                override val to: Int,
                override val param: String,
                val emoji: String,
                val isArchived: Boolean
            ) : Mention()

            data class WithImage(
                override val from: Int,
                override val to: Int,
                override val param: String,
                val image: String,
                val isArchived: Boolean
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
                abstract val isArchived: Boolean
                data class WithImage(
                    override val from: Int,
                    override val to: Int,
                    override val param: String,
                    val imageUrl: String,
                    override val isArchived: Boolean
                ) : Profile()

                data class WithInitials(
                    override val from: Int,
                    override val to: Int,
                    override val param: String,
                    val initials: Char,
                    override val isArchived: Boolean
                ) : Profile()
            }

            sealed class Task : Mention(){
                abstract val isArchived: Boolean
                data class Checked(
                    override val from: Int,
                    override val to: Int,
                    override val param: String,
                    override val isArchived: Boolean
                ) : Task()

                data class Unchecked(
                    override val from: Int,
                    override val to: Int,
                    override val param: String,
                    override val isArchived: Boolean
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
        OBJECT,
        UNDERLINE
    }

    companion object {
        const val DEFAULT_SPANNABLE_FLAG = Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        const val MENTION_SPANNABLE_FLAG = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        const val SPAN_MONOSPACE = "monospace"
        const val NON_EXISTENT_OBJECT_MENTION_NAME = "Non-existent object"
    }
}