package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.domain.database.repo.DatabaseRepository
import com.agileburo.anytype.presentation.databaseview.modals.CustomizeDisplayViewModelFactory
import com.agileburo.anytype.ui.database.modals.CustomizeDisplayFragment
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