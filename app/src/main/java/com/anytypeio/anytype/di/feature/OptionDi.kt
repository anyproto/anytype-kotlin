package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.option.OptionViewModel
import com.anytypeio.anytype.presentation.relations.option.OptionViewModelFactory
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.value.OptionFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

@PerModal
@Subcomponent(
    modules = [OptionObjectModule::class]
)
interface OptionObjectComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: OptionViewModel.Params): Builder
        fun build(): OptionObjectComponent
    }

    fun inject(fragment: OptionFragment)
}

@Module
object OptionObjectModule {

    @JvmStatic
    @Provides
    @PerModal
    fun createRelationOption(
        repo: BlockRepository
    ): CreateRelationOption = CreateRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        params: OptionViewModel.Params,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        setObjectDetails: SetObjectDetails,
        dispatcher: Dispatcher<Payload>,
        spaceManager: SpaceManager,
        analytics: Analytics,
        createOption: CreateRelationOption
    ): OptionViewModelFactory = OptionViewModelFactory(
        params = params,
        values = values,
        createOption = createOption,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        spaceManager = spaceManager,
        analytics = analytics,
    )
}