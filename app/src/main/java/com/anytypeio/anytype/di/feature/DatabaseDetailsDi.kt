package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository
import com.anytypeio.anytype.presentation.databaseview.modals.DetailsViewModelFactory
import com.anytypeio.anytype.ui.database.modals.DetailsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DetailsModule::class])
@PerScreen
interface DetailsSubComponent {

    @Subcomponent.Builder
    interface Builder {

        fun propertiesModule(module: DetailsModule): Builder
        fun build(): DetailsSubComponent
    }

    fun inject(fragment: DetailsFragment)
}

@Module
class DetailsModule(
    private val id: String
) {

    @Provides
    @PerScreen
    fun provideGetDatabase(databaseRepo: DatabaseRepository) = GetDatabase(databaseRepo)

    @Provides
    @PerScreen
    fun provideFactory(getDatabase: GetDatabase): DetailsViewModelFactory =
        DetailsViewModelFactory(
            id,
            getDatabase
        )
}