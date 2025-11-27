package com.anytypeio.anytype.presentation.profile

sealed class ParticipantEvent{
    data object OnDismiss: ParticipantEvent()
    data object OnCardClicked: ParticipantEvent()
    data object OnConnectClicked: ParticipantEvent()
}