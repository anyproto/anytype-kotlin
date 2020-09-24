package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository
import com.anytypeio.anytype.presentation.databaseview.modals.CustomizeDisplayViewModelFactory
import com.anytypeio.anytype.ui.database.modals.CustomizeDisplayFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(modules = [CustomizeDisplayViewModule::class])
@PerScreen
interface CustomizeDisplayViewSubComponent {

    @Subcomponent.Builder
    interface Builder {

        fun customizeModule(module: CustomizeDisplayViewModule): Builder
        fun build(): CustomizeDisplayViewSubComponent
    }

    fun inject(fragment: CustomizeDisplayFragment)
}

@Module
class CustomizeDisplayViewModule(
    private val id: String
) {

    @Provides
    @PerScreen
    fun provideGetDatabase(databaseRepo: DatabaseRepository) = GetDatabase(databaseRepo)

    @Provides
    @PerScreen
    fun provideFactory(getDatabase: GetDatabase): CustomizeDisplayViewModelFactory =
        CustomizeDisplayViewModelFactory(
            id,
            getDatabase
        )
}