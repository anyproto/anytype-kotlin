package com.anytypeio.anytype.feature_object_type.viewmodel

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.TemplateView

data class ObjectTypeVmParams(
    val objectId: Id,
    val spaceId: SpaceId
)

/**
 * State representing session while working with an object.
 */
sealed class ObjectViewState {
    data object Idle : ObjectViewState()
    data class Success(val obj: ObjectView) : ObjectViewState()
    data class Failure(val e: Throwable) : ObjectViewState()
}

sealed class ObjectTypeState {
    data object Empty : ObjectTypeState()
    data class Loading(val objectId: Id) : ObjectTypeState()
    data class Content(val objectId: Id, val obj: ObjectWrapper.Type) : ObjectTypeState()
}

sealed class UiSettingsIcon {
    data object Hidden : UiSettingsIcon()
    data class Visible(val objectId: Id) : UiSettingsIcon()
}

sealed class UiSyncStatusBadgeState {
    data object Hidden : UiSyncStatusBadgeState()
    data class Visible(val status: SpaceSyncAndP2PStatusState) : UiSyncStatusBadgeState()
}

sealed class UiTitleState {
    data object Hidden : UiTitleState()
    data class Title(val title: String) : UiTitleState()
}

sealed class UiIconState {
    data object Hidden : UiIconState()
    data class Icon(val icon: ObjectIcon) : UiIconState()
}

sealed class UiEditButton {
    data object Hidden : UiEditButton()
    data object Visible : UiEditButton()
}

sealed class UiSyncStatusWidgetState {
    data object Hidden : UiSyncStatusWidgetState()
    data class Visible(val status: SyncStatusWidgetState) : UiSyncStatusWidgetState()
}

sealed class UiTemplatesWidgetState {
    data object Hidden : UiTemplatesWidgetState()
    data class Visible(
        val number: Int
    ) : UiTemplatesWidgetState()
}

sealed class UiErrorState {
    data object Hidden : UiErrorState()
    data class Show(val reason: Reason) : UiErrorState()

    sealed class Reason {
        data class ErrorGettingObjects(val msg: String) : Reason()
        data class Other(val msg: String) : Reason()
    }
}

//region Mapping
fun ObjectWrapper.Basic.toTemplateView(
    objectId: Id,
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider,
): TemplateView.Template {
    val coverContainer = if (coverType != CoverType.NONE) {
        BasicObjectCoverWrapper(this)
            .getCover(urlBuilder, coverImageHashProvider)
    } else {
        null
    }
    return TemplateView.Template(
        id = id,
        name = name.orEmpty(),
        targetTypeId = TypeId(targetObjectType.orEmpty()),
        emoji = if (!iconEmoji.isNullOrBlank()) iconEmoji else null,
        image = iconImage?.takeIf { it.isNotBlank() }?.let { urlBuilder.thumbnail(it) },
        layout = layout ?: ObjectType.Layout.BASIC,
        coverColor = coverContainer?.coverColor,
        coverImage = coverContainer?.coverImage,
        coverGradient = coverContainer?.coverGradient,
        isDefault = false,
        targetTypeKey = TypeKey(objectId)
    )
}

//endregion