package com.anytypeio.anytype.presentation.desktop

import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.misc.UrlBuilder

interface HomeDashboardEventConverter {

    fun convert(event: Event): HomeDashboardStateMachine.Event?

    class DefaultConverter(private val builder: UrlBuilder) : HomeDashboardEventConverter {

        override fun convert(event: Event) = when (event) {
            is Event.Command.UpdateStructure -> HomeDashboardStateMachine.Event.OnStructureUpdated(
                event.children
            )
            is Event.Command.AddBlock -> HomeDashboardStateMachine.Event.OnBlocksAdded(
                blocks = event.blocks,
                details = event.details,
                builder = builder
            )
            is Event.Command.ShowBlock -> HomeDashboardStateMachine.Event.OnShowBlocks(
                blocks = event.blocks,
                context = event.context,
                details = event.details,
                builder = builder
            )
            is Event.Command.LinkGranularChange -> {
                event.fields?.let { fields ->
                    HomeDashboardStateMachine.Event.OnLinkFieldsChanged(
                        id = event.id,
                        fields = fields,
                        builder = builder
                    )
                }
            }
            is Event.Command.UpdateDetails -> {
                HomeDashboardStateMachine.Event.OnDetailsUpdated(
                    context = event.context,
                    target = event.target,
                    details = event.details,
                    builder = builder
                )
            }
            else -> null
        }
    }
}