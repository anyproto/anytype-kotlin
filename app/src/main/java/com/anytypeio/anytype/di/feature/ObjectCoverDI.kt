package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.presentation.`object`.ObjectCoverPickerViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.page.cover.DocCoverSliderFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectCoverPickerModule::class])
@PerModal
interface ObjectCoverPickerComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectCoverPickerModule): Builder
        fun build(): ObjectCoverPickerComponent
    }

    fun inject(fragment: DocCoverSliderFragment)
}

@Module
object ObjectCoverPickerModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        removeDocCover: RemoveDocCover,
        setDocCoverImage: SetDocCoverImage,
        dispatcher: Dispatcher<Payload>
    ): ObjectCoverPickerViewModel.Factory = ObjectCoverPickerViewModel.Factory(
        setDocCoverImage = setDocCoverImage,
        removeDocCover = removeDocCover,
        dispatcher = dispatcher
    )
}