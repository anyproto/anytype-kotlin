package com.anytypeio.anytype.di.feature.types

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.presentation.types.TypeEditViewModel
import com.anytypeio.anytype.presentation.types.TypeIcon
import com.anytypeio.anytype.presentation.types.TypeId
import com.anytypeio.anytype.presentation.types.TypeName
import com.anytypeio.anytype.ui.types.edit.TypeEditFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [TypeEditDependencies::class],
    modules = [
        TypeEditModule::class,
        TypeEditModule.Declarations::class
    ]
)
@PerScreen
interface TypeEditComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun withName(@TypeName name: String): Builder

        @BindsInstance
        fun withId(@TypeId id: String): Builder

        @BindsInstance
        fun withIcon(@TypeIcon icon: String): Builder

        fun withDependencies(dependencies: TypeEditDependencies): Builder

        fun build(): TypeEditComponent
    }

    fun inject(fragment: TypeEditFragment)
}

@Module
object TypeEditModule {

    @Provides
    @PerScreen
    @JvmStatic
    fun provideEmojiProvider(): EmojiProvider = Emoji

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: TypeEditViewModel.Factory): ViewModelProvider.Factory

    }

}

interface TypeEditDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun urlBuilder(): UrlBuilder
    fun analytics(): Analytics
}