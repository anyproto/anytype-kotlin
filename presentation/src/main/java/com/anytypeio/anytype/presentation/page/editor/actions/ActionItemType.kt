package com.anytypeio.anytype.presentation.page.editor.actions

sealed class ActionItemType {
    object AddBelow : ActionItemType()
    object TurnInto : ActionItemType()
    object Delete : ActionItemType()
    object Duplicate : ActionItemType()
    object Rename : ActionItemType()
    object MoveTo : ActionItemType()
    object SAM : ActionItemType()
    object Style : ActionItemType()
    object Download : ActionItemType()
    object Replace : ActionItemType()
    object AddCaption : ActionItemType()
    object Divider : ActionItemType()
    object DividerExtended : ActionItemType()
}