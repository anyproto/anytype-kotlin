package com.anytypeio.anytype.feature_date.ui.models

import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsItem
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem

sealed class DateEvent {

    sealed class TopToolbar : DateEvent() {
        data class OnSyncStatusClick(val status: SpaceSyncAndP2PStatusState) : TopToolbar()
        data class OnCalendarClick(val timestampInSeconds: TimestampInSeconds) : TopToolbar()
    }

    sealed class Header : DateEvent() {
        data object OnNextClick : Header()
        data object OnPreviousClick : Header()
        data class OnHeaderClick(val timeInMillis: TimeInMillis): Header()
    }

    sealed class FieldsSheet : DateEvent() {
        data object OnSheetDismiss : FieldsSheet()
        data class OnFieldClick(val item: UiFieldsItem) : FieldsSheet()
        data class OnSearchQueryChanged(val query: String) : FieldsSheet()
    }

    sealed class Calendar : DateEvent() {
        data object OnCalendarDismiss : Calendar()
        data class OnCalendarDateSelected(val timeInMillis: Long?) : Calendar()
        data object OnTodayClick : Calendar()
        data object OnTomorrowClick : Calendar()
    }

    sealed class NavigationWidget : DateEvent() {
        data object OnGlobalSearchClick : NavigationWidget()
        data object OnAddDocClick : NavigationWidget()
        data object OnAddDocLongClick : NavigationWidget()
        data object OnBackClick : NavigationWidget()
        data object OnBackLongClick : NavigationWidget()
        data object OnHomeClick : NavigationWidget()
    }

    sealed class ObjectsList : DateEvent() {
        data class OnObjectClicked(val item: UiObjectsListItem) : ObjectsList()
        data class OnObjectMoveToBin(val item: UiObjectsListItem.Item) : ObjectsList()
        data object OnLoadMore : ObjectsList()
    }

    sealed class FieldsList : DateEvent() {
        data class OnFieldClick(val item: UiFieldsItem) : FieldsList()
        data object OnScrolledToItemDismiss : FieldsList()
    }

    sealed class SyncStatusWidget : DateEvent() {
        data object OnSyncStatusDismiss : SyncStatusWidget()
    }

    sealed class Snackbar : DateEvent() {
        data object OnSnackbarDismiss : Snackbar()
        data class UndoMoveToBin(val objectId: String) : Snackbar()
    }
}