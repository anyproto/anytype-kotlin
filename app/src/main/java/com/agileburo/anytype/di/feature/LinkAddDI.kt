package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.page.CheckForUnlink
import com.agileburo.anytype.presentation.page.LinkAddViewModelFactory
import com.agileburo.anytype.ui.page.modals.LinkFragment
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

    fun inject(fragment: LinkFragment)
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