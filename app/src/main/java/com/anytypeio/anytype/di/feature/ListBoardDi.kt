package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.database.interactor.GetContact
import com.anytypeio.anytype.domain.database.interactor.GetContacts
import com.anytypeio.anytype.domain.database.interactor.AddFilter
import com.anytypeio.anytype.domain.database.interactor.GetFilters
import com.anytypeio.anytype.presentation.databaseview.ListBoardViewModelFactory
import com.anytypeio.anytype.ui.database.list.ListBoardFragment
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