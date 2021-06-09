package com.anytypeio.anytype.presentation.page.editor.slash

import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.ObjectType.Layout as ObjectTypeLayout
import com.anytypeio.anytype.presentation.relations.RelationListViewModel

sealed class SlashWidgetState {
    data class UpdateItems(
        val mainItems: List<SlashItem>,
        val styleItems: List<SlashItem>,
        val mediaItems: List<SlashItem>,
        val objectItems: List<SlashItem>,
        val relationItems: List<RelationListViewModel.Model>,
        val otherItems: List<SlashItem>,
        val actionsItems: List<SlashItem>,
        val alignmentItems: List<SlashItem>,
        val colorItems: List<SlashItem>,
        val backgroundItems: List<SlashItem>
    ) : SlashWidgetState() {
        companion object {
            fun empty() = UpdateItems(
                mainItems = emptyList(),
                styleItems = emptyList(),
                mediaItems = emptyList(),
                objectItems = emptyList(),
                relationItems = emptyList(),
                otherItems = emptyList(),
                actionsItems = emptyList(),
                alignmentItems = emptyList(),
                colorItems = emptyList(),
                backgroundItems = emptyList()
            )
        }
    }
}

sealed class SlashItem {

    //region SUB HEADER
    sealed class Subheader: SlashItem(){
        object Style: Subheader()
        object StyleWithBack: Subheader()
        object Media: Subheader()
        object MediaWithBack: Subheader()
        object ObjectType: Subheader()
        object ObjectTypeWithBlack: Subheader()
        object Other: Subheader()
        object OtherWithBack: Subheader()
        object Actions: Subheader()
        object ActionsWithBack: Subheader()
        object Alignment: Subheader()
        object AlignmentWithBack: Subheader()
        object Color: Subheader()
        object ColorWithBack: Subheader()
        object Background: Subheader()
        object BackgroundWithBack: Subheader()
    }
    //endregion

    object Back: SlashItem()

    //region MAIN
    sealed class Main : SlashItem() {
        object Style: Main()
        object Media: Main()
        object Objects: Main()
        object Relations: Main()
        object Other: Main()
        object Actions: Main()
        object Alignment: Main()
        object Color: Main()
        object Background: Main()
    }
    //endregion

    //region STYLE
    sealed class Style : SlashItem() {

        sealed class Type : Style() {
            object Text: Type()
            object Title: Type()
            object Heading: Type()
            object Subheading: Type()
            object Highlighted: Type()
            object Callout: Type()
            object Checkbox: Type()
            object Numbered: Type()
            object Toggle: Type()
            object Bulleted: Type()
        }

        sealed class Markup : Style() {
            object Bold: Markup()
            object Italic : Markup()
            object Breakthrough: Markup()
            object Code: Markup()
        }
    }
    //endregion

    //region MEDIA
    sealed class Media : SlashItem() {
        object File: Media()
        object Picture: Media()
        object Video: Media()
        object Bookmark: Media()
        object Code: Media()
    }
    //endregion

    //region OBJECT TYPE
    data class ObjectType(
        val url: Url,
        val name: String,
        val emoji: String,
        val description: String?,
        val layout: ObjectTypeLayout
    ) : SlashItem()
    //endregion

    //region RELATION
    data class Relation(val relation: RelationListViewModel.Model.Item) : SlashItem()
    //endregion

    //region OTHER
    sealed class Other : SlashItem() {
        object Line: Other()
        object Dots: Other()
    }
    //endregion

    //region ACTIONS
    sealed class Actions : SlashItem() {
        object Delete: Actions()
        object Duplicate: Actions()
        object Copy: Actions()
        object Paste: Actions()
        object Move: Actions()
        object MoveTo: Actions()
        object CleanStyle: Actions()
        object LinkTo: Actions()
    }
    //endregion

    //region ALIGNMENT
    sealed class Alignment : SlashItem() {
        object Left : Alignment()
        object Center : Alignment()
        object Right : Alignment()
    }
    //endregion

    //region TEXT COLOR & BACKGROUND
    sealed class Color: SlashItem() {
        data class Text(val code: String, val isSelected: Boolean) : Color()
        data class Background(val code: String, val isSelected: Boolean) : Color()
    }
    //endregion
}