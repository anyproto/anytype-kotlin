package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.contacts.GetContact
import com.agileburo.anytype.domain.contacts.GetContacts
import com.agileburo.anytype.domain.database.interactor.AddFilter
import com.agileburo.anytype.domain.database.interactor.GetFilters
import com.agileburo.anytype.presentation.contacts.ContactsViewModelFactory
import com.agileburo.anytype.ui.contacts.ContactsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ContactsModule::class])
@PerScreen
interface ContactsSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): ContactsSubComponent
        fun contactsModule(module: ContactsModule): Builder
    }

    fun inject(fragment: ContactsFragment)
}

@Module
class ContactsModule {

    @PerScreen
    @Provides
    fun provideViewModelFactory(
        getContacts: GetContacts,
        getContact: GetContact,
        getFilters: GetFilters,
        addFilter: AddFilter
    ): ContactsViewModelFactory =
        ContactsViewModelFactory(
            getContacts = getContacts,
            getContact = getContact,
            getFilters = getFilters,
            addFilter = AddFilter()
        )

    @PerScreen
    @Provides
    fun provideGetContacts(): GetContacts = GetContacts()

    @PerScreen
    @Provides
    fun provideGetContact(): GetContact = GetContact()

    @PerScreen
    @Provides
    fun provideGetFilters(): GetFilters = GetFilters()

    @PerScreen
    @Provides
    fun provideAddFilter(): AddFilter = AddFilter()
}