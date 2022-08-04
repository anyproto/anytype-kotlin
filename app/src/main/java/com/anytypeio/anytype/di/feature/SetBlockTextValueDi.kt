package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.objects.block.SetBlockTextValueViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.editor.modals.SetBlockTextValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [SetBlockTextValueModule::class])
@PerDialog
interface SetBlockTextValueSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(model: SetBlockTextValueModule): Builder
        fun build(): SetBlockTextValueSubComponent
    }

    fun inject(fragment: SetBlockTextValueFragment)
}

@Module
object SetBlockTextValueModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        storage: Editor.Storage,
        dispatcher: Dispatcher<Payload>,
        paste: Paste,
        copy: Copy,
        updateText: UpdateText,
        analytics: Analytics
    ): SetBlockTextValueViewModel.Factory =
        SetBlockTextValueViewModel.Factory(
            storage = storage,
            dispatcher = dispatcher,
            paste = paste,
            copy = copy,
            updateText = updateText,
            analytics = analytics
        )
}