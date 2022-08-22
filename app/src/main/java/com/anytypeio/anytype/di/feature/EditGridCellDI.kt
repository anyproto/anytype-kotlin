package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.`object`.ReloadObject
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationDateValueViewModel
import com.anytypeio.anytype.presentation.sets.RelationTextValueViewModel
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

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
        relations: ObjectRelationProvider,
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
interface RelationDataValueSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationDateValueModule): Builder
        fun build(): RelationDataValueSubComponent
    }

    fun inject(fragment: RelationDateValueFragment)
}

@Module
object RelationDateValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideEditGridCellViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider
    ) = RelationDateValueViewModel.Factory(relations, values)
}
