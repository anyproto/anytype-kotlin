package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import timber.log.Timber

interface PinObjectAsWidgetDelegate {

    suspend fun pinChat(space: SpaceId, obj: Id) : Resultat<Any>

    class Default @Inject constructor(
        private val createWidget: CreateWidget,
        private val showObject: GetObject,
        private val spaceManager: SpaceManager
    ) : PinObjectAsWidgetDelegate {

        override suspend fun pinChat(
            space: SpaceId,
            obj: Id
        ): Resultat<Any> {

            val config = spaceManager.getConfig(space)
            var target: Id?  = null

            if (config == null)
                return Resultat.Failure(Exception("Space config not found"))

            showObject.async(
                GetObject.Params(
                    target = config.widgets,
                    space = SpaceId(config.space),
                    saveAsLastOpened = false
                )
            ).onSuccess { objectView ->
                objectView.blocks.find { it.content is Block.Content.Widget }?.let {
                    target = it.id
                }
            }

            val params = CreateWidget.Params(
                ctx = config.widgets,
                source = obj,
                type = WidgetLayout.LINK,
                position = Position.TOP,
                target = target
            )
            return createWidget.async(params)
        }
    }

}