package com.anytypeio.anytype.presentation.editor.editor.actions

sealed class ActionItemType {
    object AddBelow : ActionItemType()
    object TurnIntoPage : ActionItemType()
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
    object Preview : ActionItemType()
    object Copy : ActionItemType()
    object Paste : ActionItemType()

    companion object {
        val default = listOf(
                AddBelow,
                Delete,
                Copy,
                Paste,
                Style,
                Duplicate,
                SAM,
                MoveTo
        )
    }
}