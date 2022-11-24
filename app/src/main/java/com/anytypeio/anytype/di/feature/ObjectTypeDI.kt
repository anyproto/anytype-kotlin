package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModelFactory
import com.anytypeio.anytype.ui.objects.types.pickers.AppDefaultObjectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.DataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.DraftObjectSelectTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.EmptyDataViewSelectSourceFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectSelectTypeFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectTypeChangeModule::class])
@PerScreen
interface ObjectTypeChangeSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectTypeChangeModule): Builder
        fun build(): ObjectTypeChangeSubComponent
    }

    fun inject(fragment: DraftObjectSelectTypeFragment)
    fun inject(fragment: ObjectSelectTypeFragment)
    fun inject(fragment: DataViewSelectSourceFragment)
    fun inject(fragment: EmptyDataViewSelectSourceFragment)
    fun inject(fragment: AppDefaultObjectTypeFragment)
}

@Module
object ObjectTypeChangeModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectTypeViewModelFactory(
        storeOfObjectTypes: StoreOfObjectTypes
    ): ObjectTypeChangeViewModelFactory {
        return ObjectTypeChangeViewModelFactory(
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchObjectsUseCase(
        repository: BlockRepository
    ): SearchObjects = SearchObjects(repository)
}