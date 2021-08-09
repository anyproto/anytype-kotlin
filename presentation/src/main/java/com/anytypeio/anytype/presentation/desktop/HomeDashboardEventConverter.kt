package com.anytypeio.anytype.presentation.desktop

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import timber.log.Timber

interface HomeDashboardEventConverter {

    fun convert(event: Event): HomeDashboardStateMachine.Event?

    class DefaultConverter(
        private val builder: UrlBuilder,
        private val getFlavourConfig: GetFlavourConfig,
        private val objectTypesProvider: ObjectTypesProvider
    ) : HomeDashboardEventConverter {

        override fun convert(event: Event) = when (event) {
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
                        objectTypes = objectTypesProvider.get(),
                        isDataViewEnabled = getFlavourConfig.isDataViewEnabled()
                    )
                }
                SmartBlockType.PROFILE_PAGE -> {
                    HomeDashboardStateMachine.Event.OnShowProfile(
                        blocks = event.blocks,
                        context = event.context,
                        details = event.details,
                        builder = builder
                    )
                }
                else -> {
                    null
                }
            }
            is Event.Command.LinkGranularChange -> {
                event.fields?.let { fields ->
                    HomeDashboardStateMachine.Event.OnLinkFieldsChanged(
                        id = event.id,
                        fields = fields,
                        builder = builder
                    )
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
                Timber.d("Ignored event: $event")
                null
            }
        }
    }
}