package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.objects.canCreateObjectOfType
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Widget container for the grouped object types widget.
 * Subscribes to StoreOfObjectTypes and creates subscriptions for each type to check object counts.
 * Emits a single WidgetView.ObjectTypesGroup containing only types with at least one object.
 * 
 * Unlike individual type widgets, this container:
 * - Uses limit=1 subscriptions per type to efficiently check if objects exist
 * - Does not have expand/collapse state (the card is always visible)
 * - Filters types based on validation criteria and object count > 0
 */
class ObjectTypesGroupWidgetContainer(
    private val widget: Widget.ObjectTypesGroup,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storage: StorelessSubscriptionContainer,
    private val spaceId: SpaceId,
    private val fieldParser: FieldParser,
    private val spaceUxType: SpaceUxType?,
    isSessionActive: Flow<Boolean>
) : WidgetContainer {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val view: Flow<WidgetView> = isSessionActive.flatMapLatest { isActive ->
        if (isActive) {
            buildViewFlow()
        } else {
            // Return empty state when session is not active
            flowOf(
                WidgetView.ObjectTypesGroup(
                    id = widget.id,
                    typeRows = emptyList(),
                    sectionType = widget.sectionType
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow(): Flow<WidgetView.ObjectTypesGroup> {
        return storeOfObjectTypes.observe().flatMapLatest { allTypes ->
            // Create subscriptions for each type to check if they have objects
            val typeCheckFlows = allTypes.map { type ->
                checkTypeHasObjects(type)
            }
            
            if (typeCheckFlows.isEmpty()) {
                flowOf(buildView(allTypes, emptySet()))
            } else {
                combine(typeCheckFlows) { results ->
                    val typesWithObjects = results.filterNotNull().toSet()
                    buildView(allTypes, typesWithObjects)
                }
            }
        }
    }

    private fun checkTypeHasObjects(type: ObjectWrapper.Type): Flow<String?> {
        val subscriptionId = "type-check-${type.uniqueKey}-${widget.id}"
        return storage.subscribe(
            StoreSearchParams(
                space = spaceId,
                subscription = subscriptionId,
                filters = listOf(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = type.id
                    )
                ),
                sorts = emptyList(),
                keys = listOf(Relations.ID, Relations.TYPE),
                limit = 1
            )
        ).map { objects ->
            Timber.d("Check type has objects: $objects")
            if (objects.isNotEmpty()) type.uniqueKey else null
        }
    }

    private fun buildView(
        allTypes: List<ObjectWrapper.Type>,
        typesWithObjects: Set<String>
    ): WidgetView.ObjectTypesGroup {
        // Get system layouts based on space context
        val systemLayoutsForSpace = SupportedLayouts.getSystemLayouts(spaceUxType)
        val excludedLayouts = systemLayoutsForSpace + SupportedLayouts.dateLayouts + listOf(
            ObjectType.Layout.OBJECT_TYPE,
            ObjectType.Layout.PARTICIPANT
        )
        
        // Filter out invalid types and types without objects
        val filteredObjectTypes = allTypes
            .filter { objectType ->
                objectType.isValid &&
                !excludedLayouts.contains(objectType.recommendedLayout) &&
                objectType.isArchived != true &&
                objectType.isDeleted != true &&
                objectType.uniqueKey != ObjectTypeIds.TEMPLATE &&
                typesWithObjects.contains(objectType.uniqueKey) // Only show types with objects
            }
        
        Timber.d("ObjectTypesGroupWidgetContainer: allTypes = ${allTypes.size}, withObjects = ${typesWithObjects.size}, filtered = ${filteredObjectTypes.size}")
        
        // Sort types by priority using shared extension
        val isChatSpace = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
        val sortedTypes = filteredObjectTypes.sortByTypePriority(isChatSpace)
        
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
