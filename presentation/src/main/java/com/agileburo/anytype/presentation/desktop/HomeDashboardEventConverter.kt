package com.agileburo.anytype.presentation.desktop

import com.agileburo.anytype.domain.dashboard.interactor.toHomeDashboard
import com.agileburo.anytype.domain.event.model.Event

interface HomeDashboardEventConverter {

    fun convert(event: Event): HomeDashboardStateMachine.Event?

    class DefaultConverter : HomeDashboardEventConverter {

        override fun convert(event: Event) = when (event) {
            is Event.Command.UpdateStructure -> HomeDashboardStateMachine.Event.OnStructureUpdated(
                event.children
            )
            is Event.Command.AddBlock -> HomeDashboardStateMachine.Event.OnBlocksAdded(
                event.blocks
            )
            is Event.Command.ShowBlock -> HomeDashboardStateMachine.Event.OnDashboardLoaded(
                dashboard = event.blocks.toHomeDashboard(
                    id = event.context,
                    details = event.details
                )
            )
            is Event.Command.LinkGranularChange -> {
                event.fields?.let { fields ->
                    HomeDashboardStateMachine.Event.OnLinkFieldsChanged(
                        id = event.id,
                        fields = fields
                    )
                }
            }
            is Event.Command.UpdateDetails -> {
                HomeDashboardStateMachine.Event.OnDetailsUpdated(
                    context = event.context,
                    target = event.target,
                    details = event.details
                )
            }
            else -> null
        }
    }
}