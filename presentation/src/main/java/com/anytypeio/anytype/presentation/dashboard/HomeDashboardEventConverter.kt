package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import timber.log.Timber

interface HomeDashboardEventConverter {

    suspend fun convert(event: Event): HomeDashboardStateMachine.Event?

    class DefaultConverter(
        private val builder: UrlBuilder,
        private val storeOfObjectTypes: StoreOfObjectTypes
    ) : HomeDashboardEventConverter {

        override suspend fun convert(event: Event) = when (event) {
            is Event.Command.UpdateStructure -> HomeDashboardStateMachine.Event.OnStructureUpdated(
                event.children
            )
            is Event.Command.AddBlock -> HomeDashboardStateMachine.Event.OnBlocksAdded(
                blocks = event.blocks,
                details = event.details,
                builder = builder
            )
            is Event.Command.ShowObject -> when (event.type) {
                SmartBlockType.HOME -> {
                    HomeDashboardStateMachine.Event.OnShowDashboard(
                        blocks = event.blocks,
                        context = event.context,
                        details = event.details,
                        builder = builder,
                        objectTypes = storeOfObjectTypes.getAll()
                    )
                }
                else -> {
                    null
                }
            }
            is Event.Command.Details.Set -> {
                HomeDashboardStateMachine.Event.OnDetailsUpdated(
                    context = event.context,
                    target = event.target,
                    details = event.details,
                    builder = builder
                )
            }
            is Event.Command.Details.Amend -> {
                HomeDashboardStateMachine.Event.OnDetailsAmended(
                    context = event.context,
                    target = event.target,
                    slice = event.details,
                    builder = builder
                )
            }
            is Event.Command.Details.Unset -> {
                HomeDashboardStateMachine.Event.OnDetailsUnset(
                    context = event.context,
                    target = event.target,
                    keys = event.keys,
                    builder = builder
                )
            }
            else -> {
                Timber.v("Ignored event: $event")
                null
            }
        }
    }
}