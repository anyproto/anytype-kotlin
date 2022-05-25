package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.page.CheckForUnlink
import com.anytypeio.anytype.presentation.editor.LinkAddViewModelFactory
import com.anytypeio.anytype.ui.editor.modals.SetLinkFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [LinkModule::class])
@PerScreen
interface LinkSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun linkModule(module: LinkModule): Builder
        fun build(): LinkSubComponent
    }

    fun inject(fragment: SetLinkFragment)
}

@Module
class LinkModule {

    @PerScreen
    @Provides
    fun provideCanUnlink(): CheckForUnlink = CheckForUnlink()

    @PerScreen
    @Provides
    fun provideFactory(
        checkForUnlink: CheckForUnlink
    ): LinkAddViewModelFactory = LinkAddViewModelFactory(
        unlink = checkForUnlink
    )
}