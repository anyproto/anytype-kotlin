package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.ObjectTypeMenuItem

class ObjectTypePopupMenu(
    context: Context,
    anchor: View,
    onChangeTypeClicked: () -> Unit,
    onOpenSetClicked: (Id) -> Unit,
    onCreateSetClicked: (Id) -> Unit,
    items: List<ObjectTypeMenuItem>
) : PopupMenu(context, anchor, Gravity.BOTTOM, 0, R.style.DefaultPopupMenuStyle) {
    init {
        val res = context.resources
        items.forEachIndexed { index, objectTypeMenuItem ->
            when (objectTypeMenuItem) {
                is ObjectTypeMenuItem.ChangeType -> {
                    menu.add(
                        0,
                        R.id.menuChangeType,
                        index,
                        res.getString(R.string.menu_type_change)
                    ).setOnMenuItemClickListener {
                        onChangeTypeClicked()
                        true
                    }
                }
                is ObjectTypeMenuItem.CreateSet -> {
                    menu.add(
                        0,
                        R.id.menuCreateSet,
                        index,
                        res.getString(R.string.menu_type_create_set, objectTypeMenuItem.typeName)
                    ).setOnMenuItemClickListener {
                        onCreateSetClicked(objectTypeMenuItem.type)
                        true
                    }
                }
                is ObjectTypeMenuItem.OpenSet -> {
                    menu.add(
                        0,
                        R.id.menuCreateSet,
                        index,
                        res.getString(R.string.menu_type_open_set, objectTypeMenuItem.typeName)
                    ).setOnMenuItemClickListener {
                        onOpenSetClicked(objectTypeMenuItem.set)
                        true
                    }
                }
            }
        }
    }
}