package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.presentation.`object`.ObjectMenuViewModel
import com.anytypeio.anytype.ui.page.sheets.ObjectMenuFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(modules = [ObjectMenuModule::class])
@PerDialog
interface ObjectMenuComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectMenuModule): Builder
        fun build(): ObjectMenuComponent
    }

    fun inject(fragment: ObjectMenuFragment)
}

@Module
object ObjectMenuModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        archiveDocument: ArchiveDocument
    ): ObjectMenuViewModel.Factory = ObjectMenuViewModel.Factory(
        archiveDocument = archiveDocument
    )
}