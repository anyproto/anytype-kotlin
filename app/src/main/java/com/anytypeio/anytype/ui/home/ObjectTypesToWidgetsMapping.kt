package com.anytypeio.anytype.ui.home

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.home.SystemTypeView
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetView

/**
 * Maps SystemTypeView objects to WidgetView types for reusing existing widget card components.
 * This allows system types to be rendered with the same visual components as regular widgets.
 */
class ObjectTypesToWidgetsMapping {

    /**
     * Converts SystemTypeView to WidgetView.Tree for tree-style rendering.
     */
    fun SystemTypeView.toTreeWidgetView(): WidgetView.Tree {
        return WidgetView.Tree(
            id = id,
            isLoading = false,
            name = WidgetView.Name.Default(name),
            source = toWidgetSource(),
            elements = emptyList(), // System types don't have tree elements
            isExpanded = false,
            isEditable = false
        )
    }

    /**
     * Converts SystemTypeView to WidgetView.ListOfObjects for list-style rendering.
     * @param isCompact whether to use compact list layout
     */
    fun SystemTypeView.toListWidgetView(isCompact: Boolean): WidgetView.ListOfObjects {
        return WidgetView.ListOfObjects(
            id = id,
            isLoading = false,
            source = toWidgetSource(),
            type = WidgetView.ListOfObjects.Type.Favorites, // Default type for system types
            elements = emptyList(), // System types don't have list elements
            isExpanded = true,
            isCompact = isCompact
        )
    }

    /**
     * Converts SystemTypeView to WidgetView.SetOfObjects for view-style rendering.
     */
    fun SystemTypeView.toSetOfObjectsWidgetView(): WidgetView.SetOfObjects {
        return WidgetView.SetOfObjects(
            id = id,
            isLoading = false,
            source = toWidgetSource(),
            tabs = emptyList(), // System types don't have tabs
            elements = emptyList(), // System types don't have elements
            isExpanded = true,
            name = WidgetView.Name.Default(name)
        )
    }

    /**
     * Converts SystemTypeView to WidgetView.Link for link-style rendering.
     */
    fun SystemTypeView.toLinkWidgetView(): WidgetView.Link {
        return WidgetView.Link(
            id = id,
            isLoading = false,
            name = WidgetView.Name.Default(name),
            source = toWidgetSource()
        )
    }

    /**
     * Creates a Widget.Source for the system type.
     * This provides the necessary source information for widget rendering.
     */
    private fun SystemTypeView.toWidgetSource(): Widget.Source {
        return Widget.Source.Default(
            obj = ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to id,
                    Relations.NAME to name,
                    Relations.TYPE to listOf(id), // System type references itself
                    Relations.LAYOUT to 0.0 // Basic layout
                )
            )
        )
    }

    companion object {
        /**
         * Singleton instance for accessing the mapping functions.
         */
        val instance = ObjectTypesToWidgetsMapping()
    }
}