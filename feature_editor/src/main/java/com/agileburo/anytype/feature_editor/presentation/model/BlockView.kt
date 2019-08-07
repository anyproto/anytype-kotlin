package com.agileburo.anytype.feature_editor.presentation.model

import android.text.SpannableString

sealed class BlockView {

    abstract val id : String
    var isSelected: Boolean = false

    data class ParagraphView(
        override val id : String,
        override val indent : Int = 0,
        override var text : SpannableString,
        override val focused: Boolean
    ) : BlockView(), Editable, Indentable, Focusable

    data class HeaderView(
        override val id : String,
        override var text : SpannableString,
        val type : HeaderType,
        override val indent: Int = 0,
        override val focused: Boolean
    ) : BlockView(), Editable, Indentable, Focusable {
        enum class HeaderType { ONE, TWO, THREE, FOUR }
    }

    data class QuoteView(
        override val id : String,
        override var text : SpannableString,
        override val indent : Int = 0,
        override val focused: Boolean
    ) : BlockView(), Editable, Indentable, Focusable

    data class CheckboxView(
        override val id : String,
        override var text : SpannableString,
        override val indent: Int = 0,
        override val focused: Boolean,
        val isChecked : Boolean
    ) : BlockView(), Editable, Indentable, Focusable

    data class CodeSnippetView(
        override val id : String,
        override var text : SpannableString,
        override val indent : Int = 0,
        override val focused: Boolean
    ) : BlockView(), Editable, Indentable, Focusable

    data class NumberListItemView(
        override val id : String,
        override var text: SpannableString,
        override val indent : Int = 0,
        override val focused: Boolean,
        val number : Int
    ) : BlockView(), Editable, Indentable, Focusable

    data class BulletView(
        override val id : String,
        override var text: SpannableString,
        override val indent : Int = 0,
        override val focused: Boolean
    ) : BlockView(), Editable, Indentable, Focusable

    data class LinkToPageView(
        override val id : String,
        val title : String,
        override val indent : Int = 0
    ) : BlockView(), Indentable

    data class BookmarkView(
        override val id : String,
        val title : String,
        val description : String,
        val url : String,
        val image : String,
        override val indent : Int = 0
    ) : BlockView(), Indentable

    data class DividerView(
        override val id : String,
        override val indent : Int = 0
        ) : BlockView(), Indentable

    data class PictureView (
        override val id : String,
        val url : String,
        override val indent : Int = 0
    ) : BlockView(), Indentable

    data class ToggleView(
        override val id : String,
        override val indent : Int = 0,
        override var text : SpannableString,
        val expanded : Boolean = false,
        override val focused: Boolean
    ) : BlockView(), Editable, Indentable, Focusable

    interface Editable {
        var text : SpannableString
    }

    interface Indentable {
        val indent : Int
    }

    interface Focusable {
        val focused : Boolean
    }

    interface Consumer
}