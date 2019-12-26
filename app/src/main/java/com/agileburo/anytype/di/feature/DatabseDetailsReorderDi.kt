package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.domain.database.interactor.SwapDetails
import com.agileburo.anytype.domain.database.repo.DatabaseRepository
import com.agileburo.anytype.presentation.databaseview.modals.DetailsReorderViewModelFactory
import com.agileburo.anytype.ui.database.modals.DetailsReorderFragment
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