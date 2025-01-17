package com.anytypeio.anytype.presentation.profile

sealed class ParticipantEvent{
    data object OnDismiss: ParticipantEvent()
    data object OnButtonClick: ParticipantEvent()
    data class OnNameUpdate(val name: String) : ParticipantEvent()
}