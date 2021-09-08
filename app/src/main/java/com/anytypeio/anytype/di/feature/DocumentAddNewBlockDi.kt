package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.presentation.editor.picker.DocumentAddBlockViewModelFactory
import com.anytypeio.anytype.ui.editor.modals.AddBlockFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentAddNewBlockModule::class])
@PerModal
interface DocumentAddNewBlockSubComponent{

    @Subcomponent.Builder
    interface Builder {
        fun documentAddNewBlockModule(module: DocumentAddNewBlockModule): Builder
        fun build(): DocumentAddNewBlockSubComponent
    }

    fun inject(fragment: AddBlockFragment)
}

@Module
object DocumentAddNewBlockModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        getObjectTypes: GetObjectTypes,
    ): DocumentAddBlockViewModelFactory =
        DocumentAddBlockViewModelFactory(getObjectTypes)
}