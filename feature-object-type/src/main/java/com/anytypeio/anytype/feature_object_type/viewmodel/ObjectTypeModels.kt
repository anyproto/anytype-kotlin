package com.anytypeio.anytype.feature_object_type.viewmodel

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState

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

sealed class UiHeaderState {
    data object Empty : UiHeaderState()
    data class Content(
        val title: String,
        val icon: ObjectIcon
    ) : UiHeaderState()
}

sealed class UiSettingsIcon{
    data object Hidden : UiSettingsIcon()
    data class Visible(val objectId: Id) : UiSettingsIcon()
}

sealed class UiSyncStatusBadgeState {
    data object Hidden : UiSyncStatusBadgeState()
    data class Visible(val status: SpaceSyncAndP2PStatusState) : UiSyncStatusBadgeState()
}

sealed class UiSyncStatusWidgetState {
    data object Hidden : UiSyncStatusWidgetState()
    data class Visible(val status: SyncStatusWidgetState) : UiSyncStatusWidgetState()
}



sealed class UiErrorState {
    data object Hidden : UiErrorState()
    data class Show(val reason: Reason) : UiErrorState()

    sealed class Reason {
        data class ErrorGettingObjects(val msg: String) : Reason()
        data class Other(val msg: String) : Reason()
    }
}