package com.anytypeio.anytype.presentation.profile

sealed class ParticipantEvent{
    data object OnDismiss: ParticipantEvent()
    data class OnNameUpdate(val name: String) : ParticipantEvent()
}