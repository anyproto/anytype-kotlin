package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.database.interactor.CreateDisplayView
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository
import com.anytypeio.anytype.presentation.databaseview.modals.AddDisplayViewModelFactory
import com.anytypeio.anytype.presentation.databaseview.TableBoardViewModelFactory
import com.anytypeio.anytype.ui.database.modals.AddDisplayFragment
import com.anytypeio.anytype.ui.database.table.DatabaseViewFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(modules = [TableBoardModule::class])
@PerScreen
interface TableBoardSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun databaseViewModule(module: TableBoardModule): Builder
        fun build(): TableBoardSubComponent
    }

    fun inject(fragment: DatabaseViewFragment)
    fun inject(fragment: AddDisplayFragment)
}

@Module
class TableBoardModule {

    @Provides
    @PerScreen
    fun provideCreateDisplayView(databaseRepo: DatabaseRepository) = CreateDisplayView(databaseRepo)

    @Provides
    @PerScreen
    fun provideGetDatabase(databaseRepo: DatabaseRepository) = GetDatabase(databaseRepo)

    @Provides
    @PerScreen
    fun provideDatabaseViewFactory(getDatabase: GetDatabase) = TableBoardViewModelFactory(getDatabase)

    @Provides
    @PerScreen
    fun provideAddDisplayViewFactory(createDisplayView: CreateDisplayView) =
        AddDisplayViewModelFactory(
            createDisplayView
        )
}