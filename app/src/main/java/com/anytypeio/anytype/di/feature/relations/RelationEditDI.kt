package com.anytypeio.anytype.di.feature.relations

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.presentation.relations.RelationEditViewModel
import com.anytypeio.anytype.presentation.types.TypeIcon
import com.anytypeio.anytype.presentation.types.TypeId
import com.anytypeio.anytype.presentation.types.TypeName
import com.anytypeio.anytype.ui.relations.RelationEditFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [RelationEditDependencies::class],
    modules = [
        RelationEditModule::class,
        RelationEditModule.Declarations::class
    ]
)
@PerScreen
interface RelationEditComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun withName(@TypeName name: String): Builder

        @BindsInstance
        fun withId(@TypeId id: String): Builder

        @BindsInstance
        fun withIcon(@TypeIcon icon: Int): Builder

        fun withDependencies(dependencies: RelationEditDependencies): Builder

        fun build(): RelationEditComponent
    }

    fun inject(fragment: RelationEditFragment)
}

@Module
object RelationEditModule {

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: RelationEditViewModel.Factory): ViewModelProvider.Factory

    }

}

interface RelationEditDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
}