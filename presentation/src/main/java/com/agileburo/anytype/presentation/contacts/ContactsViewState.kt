package com.agileburo.anytype.presentation.contacts

import com.agileburo.anytype.presentation.contacts.model.ContactView
import com.agileburo.anytype.presentation.filters.model.FilterView

sealed class ContactsViewState {

    sealed class ScreenState : ContactsViewState() {

        object Loading : ScreenState()
        data class Contacts(val contacts: List<ContactView>) : ScreenState()
        data class Filters(val filters: List<FilterView>) : ScreenState()
        object Error : ScreenState()
    }

    sealed class UiEffect : ContactsViewState() {
        object Init : UiEffect()
    }
}