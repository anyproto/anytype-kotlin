package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.presentation.sets.ObjectSetCreateBookmarkRecordViewModel
import com.anytypeio.anytype.ui.sets.modals.SetObjectCreateBookmarkRecordFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectSetCreateBookmarkRecordModule::class])
@PerDialog
interface ObjectSetCreateBookmarkRecordSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSetCreateBookmarkRecordModule): Builder
        fun build(): ObjectSetCreateBookmarkRecordSubComponent
    }

    fun inject(fragment: SetObjectCreateBookmarkRecordFragment)
}

@Module
object ObjectSetCreateBookmarkRecordModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideObjectSetRecordViewModelFactory(
        createBookmarkObject: CreateBookmarkObject,
        urlValidator: UrlValidator
    ): ObjectSetCreateBookmarkRecordViewModel.Factory = ObjectSetCreateBookmarkRecordViewModel.Factory(
        createBookmarkObject = createBookmarkObject,
        urlValidator = urlValidator
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideCreateBookmarkObjectUseCase(
        repo: BlockRepository
    ) : CreateBookmarkObject = CreateBookmarkObject(
        repo = repo
    )
}