package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.objects.canCreateObjectOfType
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Widget container for the grouped object types widget.
 * Subscribes to StoreOfObjectTypes and emits a single WidgetView.ObjectTypesGroup
 * containing all valid object types as rows.
 * 
 * Unlike individual type widgets, this container:
 * - Does not fetch nested objects (performance improvement)
 * - Does not have expand/collapse state (the card is always visible)
 * - Filters types based on validation criteria (TODO: add objectCount > 0 filter)
 */
class ObjectTypesGroupWidgetContainer(
    private val widget: Widget.ObjectTypesGroup,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val fieldParser: FieldParser,
    private val spaceUxType: SpaceUxType?,
    isSessionActive: Flow<Boolean>
) : WidgetContainer {

    override val view: Flow<WidgetView> = isSessionActive.map { isActive ->
        if (isActive) {
            buildView()
        } else {
            // Return empty state when session is not active
            WidgetView.ObjectTypesGroup(
                id = widget.id,
                typeRows = emptyList(),
                sectionType = widget.sectionType
            )
        }
    }

    private suspend fun buildView(): WidgetView.ObjectTypesGroup {
        val allTypes = storeOfObjectTypes.getAll()
        
        // Get system layouts based on space context
        val systemLayoutsForSpace = SupportedLayouts.getSystemLayouts(spaceUxType)
        val excludedLayouts = systemLayoutsForSpace + SupportedLayouts.dateLayouts + listOf(
            ObjectType.Layout.OBJECT_TYPE,
            ObjectType.Layout.PARTICIPANT
        )
        
        // Filter out invalid types
        val filteredObjectTypes = allTypes
            .filter { objectType ->
                objectType.isValid &&
                !excludedLayouts.contains(objectType.recommendedLayout) &&
                objectType.isArchived != true &&
                objectType.isDeleted != true &&
                objectType.uniqueKey != ObjectTypeIds.TEMPLATE
            }
        
        Timber.d("ObjectTypesGroupWidgetContainer: allTypes = ${allTypes.size}, filtered = ${filteredObjectTypes.size}")
        
        // Sort types by priority using shared extension
        val isChatSpace = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
        val sortedTypes = filteredObjectTypes.sortByTypePriority(isChatSpace)
        
        // TODO: Add filtering logic for types with objectCount > 0
        // For now, showing all types for debugging and visual check
        
        // Map types to type rows
        val typeRows = sortedTypes.map { objectType ->
            WidgetView.ObjectTypesGroup.TypeRow(
                id = objectType.id,
                icon = objectType.objectIcon(),
                name = fieldParser.getObjectName(objectType.toBasic()),
                canCreateObjects = canCreateObjectOfType(ObjectWrapper.Type(objectType.map))
            )
        }
        
        return WidgetView.ObjectTypesGroup(
            id = widget.id,
            typeRows = typeRows,
            sectionType = widget.sectionType
        )
    }
    
    /**
     * Extension to convert ObjectWrapper.Type to ObjectWrapper.Basic
     */
    private fun ObjectWrapper.Type.toBasic(): ObjectWrapper.Basic = ObjectWrapper.Basic(this.map)
}
