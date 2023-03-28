package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.`object`.ReloadObject
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.DATA_VIEW_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.INTRINSIC_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationDateValueViewModel
import com.anytypeio.anytype.presentation.sets.RelationTextValueViewModel
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent(modules = [RelationTextValueModule::class])
@PerModal
interface RelationTextValueSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationTextValueModule): Builder
        fun build(): RelationTextValueSubComponent
    }

    fun inject(fragment: RelationTextValueFragment)
}

@Module
object RelationTextValueModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideRelationTextValueViewModelFactory(
        @Named(INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        reloadObject: ReloadObject,
        analytics: Analytics
    ) = RelationTextValueViewModel.Factory(
        relations = relations,
        values = values,
        reloadObject = reloadObject,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideReloadObjectUseCase(
        repo: BlockRepository
    ): ReloadObject = ReloadObject(repo)
}

@Subcomponent(modules = [RelationDateValueModule::class])
@PerModal
interface DefaultRelationDataValueSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationDateValueModule): Builder
        fun build(): DefaultRelationDataValueSubComponent
    }

    fun inject(fragment: RelationDateValueFragment)
}

@Module
object RelationDateValueModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideEditGridCellViewModelFactory(
        @Named(INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        values: ObjectValueProvider
    ) = RelationDateValueViewModel.Factory(relations, values)
}

@Subcomponent(modules = [RelationDataViewDateValueModule::class])
@PerModal
interface DataViewRelationDataValueSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationDataViewDateValueModule): Builder
        fun build(): DataViewRelationDataValueSubComponent
    }

    fun inject(fragment: RelationDateValueFragment)
}

@Module
object RelationDataViewDateValueModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideEditGridCellViewModelFactory(
        @Named(DATA_VIEW_PROVIDER_TYPE) relations: ObjectRelationProvider,
        values: ObjectValueProvider
    ) = RelationDateValueViewModel.Factory(relations, values)
}
