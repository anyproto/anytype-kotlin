package com.agileburo.anytype.feature_editor.presentation.model

import android.text.SpannableString

sealed class BlockView {

    abstract val id : String

    data class ParagraphView(
        override val id : String,
        override var text : SpannableString
    ) : BlockView(), Editable

    data class HeaderView(
        override val id : String,
        override var text : SpannableString,
        val type : HeaderType
    ) : BlockView(), Editable {
        enum class HeaderType { ONE, TWO, THREE, FOUR }
    }

    data class QuoteView(
        override val id : String,
        override var text : SpannableString
    ) : BlockView(), Editable

    data class CheckboxView(
        override val id : String,
        override var text : SpannableString,
        val isChecked : Boolean
    ) : BlockView(), Editable

    data class CodeSnippetView(
        override val id : String,
        override var text : SpannableString
    ) : BlockView(), Editable

    data class NumberListItemView(
        override val id : String,
        override var text: SpannableString,
        val number : Int
    ) : BlockView(), Editable

    data class BulletView(
        override val id : String,
        override var text: SpannableString
    ) : BlockView(), Editable

    data class LinkToPageView(
        override val id : String,
        val title : String
    ) : BlockView()

    data class BookmarkView(
        override val id : String,
        val title : String,
        val description : String,
        val url : String,
        val image : String
    ) : BlockView()

    data class DividerView(
        override val id : String
    ) : BlockView()

    data class PictureView (
        override val id : String,
        val url : String
    ) : BlockView()

    interface Editable {
        var text : SpannableString
    }
}