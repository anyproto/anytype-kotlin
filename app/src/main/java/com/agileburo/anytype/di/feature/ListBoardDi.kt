package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.database.interactor.GetContact
import com.agileburo.anytype.domain.database.interactor.GetContacts
import com.agileburo.anytype.domain.database.interactor.AddFilter
import com.agileburo.anytype.domain.database.interactor.GetFilters
import com.agileburo.anytype.presentation.databaseview.ListBoardViewModelFactory
import com.agileburo.anytype.ui.database.list.ListBoardFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ListBoardModule::class])
@PerScreen
interface ListBoardSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): ListBoardSubComponent
        fun contactsModule(module: ListBoardModule): Builder
    }

    fun inject(fragment: ListBoardFragment)
}

@Module
class ListBoardModule {

    @PerScreen
    @Provides
    fun provideViewModelFactory(
        getContacts: GetContacts,
        getContact: GetContact,
        getFilters: GetFilters,
        addFilter: AddFilter
    ): ListBoardViewModelFactory =
        ListBoardViewModelFactory(
            getContacts = getContacts,
            getContact = getContact,
            getFilters = getFilters,
            addFilter = AddFilter()
        )

    @PerScreen
    @Provides
    fun provideGetContacts(): GetContacts =
        GetContacts()

    @PerScreen
    @Provides
    fun provideGetContact(): GetContact =
        GetContact()

    @PerScreen
    @Provides
    fun provideGetFilters(): GetFilters = GetFilters()

    @PerScreen
    @Provides
    fun provideAddFilter(): AddFilter = AddFilter()
}