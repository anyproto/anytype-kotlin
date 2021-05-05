package com.anytypeio.anytype.presentation.page.editor.slash

import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.presentation.relations.RelationListViewModel

sealed class SlashCommand {
    data class FilterItems(val filter: String, val viewType: Int) : SlashCommand()
    data class ShowMainItems(val items: List<SlashItem>) : SlashCommand()
    data class ShowStyleItems(val items: List<SlashItem>) : SlashCommand()
    data class ShowMediaItems(val items: List<SlashItem>): SlashCommand()
    data class ShowOtherItems(val items: List<SlashItem>) : SlashCommand()
    data class ShowRelations(val relations: List<RelationListViewModel.Model>): SlashCommand()
    data class ShowObjectTypes(val items: List<SlashItem>): SlashCommand()
}

sealed class SlashItem {

    //region SUB HEADER
    sealed class Subheader: SlashItem(){
        object Style: Subheader()
        object StyleWithBack: Subheader()
        object Media: Subheader()
        object MediaWithBack: Subheader()
        object ObjectType: Subheader()
        object Other: Subheader()
        object OtherWithBack: Subheader()
    }
    //endregion

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
        val description: String?
    ) : SlashItem()
    //endregion

    //region RELATION
    sealed class SlashRelation {
        object New: SlashRelation()
        data class Items(val relations: List<Relation>): SlashRelation()
    }
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
    }
    //endregion

    //region ALIGNMENT
    sealed class Alignment : SlashItem(){
        object Left: Alignment()
        object Center: Alignment()
        object Right: Alignment()
    }
    //endregion
}