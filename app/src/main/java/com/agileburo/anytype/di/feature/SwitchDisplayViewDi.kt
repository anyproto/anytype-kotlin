package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.domain.database.interactor.SwitchDisplayView
import com.agileburo.anytype.domain.database.repo.DatabaseRepository
import com.agileburo.anytype.presentation.databaseview.modals.SwitchDisplayViewViewModelFactory
import com.agileburo.anytype.ui.database.modals.SwitchDisplayFragment
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