package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.interactor.SwapDetails
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository
import com.anytypeio.anytype.presentation.databaseview.modals.DetailsReorderViewModelFactory
import com.anytypeio.anytype.ui.database.modals.DetailsReorderFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DetailsReorderModule::class])
@PerScreen
interface DetailsReorderSubComponent {

    @Subcomponent.Builder
    interface Builder {

        fun module(module: DetailsReorderModule): Builder
        fun build(): DetailsReorderSubComponent
    }

    fun inject(fragment: DetailsReorderFragment)
}

@Module
class DetailsReorderModule(
    private val id: String
) {

    @Provides
    @PerScreen
    fun swapDetails(databaseRepo: DatabaseRepository) = SwapDetails(databaseRepo)

    @Provides
    @PerScreen
    fun provideGetDatabase(databaseRepo: DatabaseRepository) = GetDatabase(databaseRepo)

    @Provides
    @PerScreen
    fun provideFactory(
        getDatabase: GetDatabase,
        swapDetails: SwapDetails
    ): DetailsReorderViewModelFactory =
        DetailsReorderViewModelFactory(
            id,
            getDatabase,
            swapDetails
        )
}