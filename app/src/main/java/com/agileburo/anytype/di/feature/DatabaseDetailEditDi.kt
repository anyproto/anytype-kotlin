package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.database.interactor.DeleteDetail
import com.agileburo.anytype.domain.database.interactor.DuplicateDetail
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.domain.database.interactor.HideDetail
import com.agileburo.anytype.domain.database.repo.DatabaseRepository
import com.agileburo.anytype.presentation.databaseview.modals.DetailEditViewModelFactory
import com.agileburo.anytype.ui.database.modals.DetailEditFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DetailEditModule::class])
@PerScreen
interface DetailEditSubComponent {

    @Subcomponent.Builder
    interface Builder {

        fun propertyModule(module: DetailEditModule): Builder
        fun build(): DetailEditSubComponent
    }

    fun inject(fragment: DetailEditFragment)
}

@Module
class DetailEditModule(
    private val detailId: String,
    private val databaseId: String
) {
    @Provides
    @PerScreen
    fun provideGetDatabase(databaseRepo: DatabaseRepository) = GetDatabase(databaseRepo)

    @Provides
    @PerScreen
    fun provideDeleteDetail(databaseRepo: DatabaseRepository) = DeleteDetail(databaseRepo)

    @Provides
    @PerScreen
    fun provideHideDetail(databaseRepo: DatabaseRepository) = HideDetail(databaseRepo)

    @Provides
    @PerScreen
    fun provideDuplicateDetail(databaseRepo: DatabaseRepository) = DuplicateDetail(databaseRepo)

    @Provides
    @PerScreen
    fun provideFactory(
        getDatabase: GetDatabase,
        deleteDetail: DeleteDetail,
        hideDetail: HideDetail,
        duplicateDetail: DuplicateDetail
    ): DetailEditViewModelFactory =
        DetailEditViewModelFactory(
            databaseId = databaseId,
            detailId = detailId,
            getDatabase = getDatabase,
            deleteDetail = deleteDetail,
            hideDetail = hideDetail,
            duplicateDetail = duplicateDetail
        )
}