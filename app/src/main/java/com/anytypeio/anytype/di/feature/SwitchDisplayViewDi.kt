package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.interactor.SwitchDisplayView
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository
import com.anytypeio.anytype.presentation.databaseview.modals.SwitchDisplayViewViewModelFactory
import com.anytypeio.anytype.ui.database.modals.SwitchDisplayFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [SwitchDisplayViewModule::class])
@PerScreen
interface SwitchDisplayViewSubComponent {

    @Subcomponent.Builder
    interface Builder {

        fun switchModule(module: SwitchDisplayViewModule): Builder
        fun build(): SwitchDisplayViewSubComponent
    }

    fun inject(fragment: SwitchDisplayFragment)
}

@Module
class SwitchDisplayViewModule(
    private val id: String
) {

    @Provides
    @PerScreen
    fun provideGetDatabase(databaseRepo: DatabaseRepository) = GetDatabase(databaseRepo)

    @PerScreen
    @Provides
    fun provideSwitchDisplayView(databaseRepository: DatabaseRepository) =
        SwitchDisplayView(databaseRepository)

    @PerScreen
    @Provides
    fun provideViewModelFactory(
        switchDisplayView: SwitchDisplayView,
        getDatabase: GetDatabase
    ): SwitchDisplayViewViewModelFactory =
        SwitchDisplayViewViewModelFactory(
            id = id,
            getDatabase = getDatabase,
            switchDisplayView = switchDisplayView
        )
}