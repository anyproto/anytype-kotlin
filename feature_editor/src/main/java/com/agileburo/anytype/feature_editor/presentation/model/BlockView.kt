package com.agileburo.anytype.feature_editor.presentation.model

import android.text.SpannableString

sealed class BlockView {

    abstract val id : String

    data class ParagraphView(
        override val id : String,
        override val indent : Int = 0,
        override var text : SpannableString
    ) : BlockView(), Editable, Indentable

    data class HeaderView(
        override val id : String,
        override var text : SpannableString,
        val type : HeaderType,
        override val indent: Int = 0
    ) : BlockView(), Editable, Indentable {
        enum class HeaderType { ONE, TWO, THREE, FOUR }
    }

    data class QuoteView(
        override val id : String,
        override var text : SpannableString,
        override val indent : Int = 0
    ) : BlockView(), Editable, Indentable

    data class CheckboxView(
        override val id : String,
        override var text : SpannableString,
        override val indent: Int = 0,
        val isChecked : Boolean
    ) : BlockView(), Editable, Indentable

    data class CodeSnippetView(
        override val id : String,
        override var text : SpannableString,
        override val indent : Int = 0
    ) : BlockView(), Editable, Indentable

    data class NumberListItemView(
        override val id : String,
        override var text: SpannableString,
        override val indent : Int = 0,
        val number : Int
    ) : BlockView(), Editable, Indentable

    data class BulletView(
        override val id : String,
        override var text: SpannableString,
        override val indent : Int = 0
    ) : BlockView(), Editable, Indentable

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
        val expanded : Boolean = false
    ) : BlockView(), Editable, Indentable

    interface Editable {
        var text : SpannableString
    }

    interface Indentable {
        val indent : Int
    }
}