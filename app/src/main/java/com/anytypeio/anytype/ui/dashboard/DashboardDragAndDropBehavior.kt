package com.anytypeio.anytype.ui.dashboard

import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior

class DashboardDragAndDropBehavior(
    onItemMoved: (Int, Int) -> Boolean,
    onItemDropped: (Int) -> Unit
) : DefaultDragAndDropBehavior(onItemMoved, onItemDropped)