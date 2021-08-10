package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.layout.SetObjectLayout
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.layout.ObjectLayoutViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.editor.layout.ObjectLayoutFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectLayoutModule::class])
@PerDialog
interface ObjectLayoutSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectLayoutModule): Builder
        fun build(): ObjectLayoutSubComponent
    }

    fun inject(fragment: ObjectLayoutFragment)
}

@Module
object ObjectLayoutModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        dispatcher: Dispatcher<Payload>,
        setObjectLayout: SetObjectLayout,
        storage: Editor.Storage
    ): ObjectLayoutViewModel.Factory = ObjectLayoutViewModel.Factory(
        dispatcher = dispatcher,
        setObjectLayout = setObjectLayout,
        storage = storage
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideSetObjectLayoutUseCase(
        repo: BlockRepository
    ): SetObjectLayout = SetObjectLayout(
        repo = repo
    )
}