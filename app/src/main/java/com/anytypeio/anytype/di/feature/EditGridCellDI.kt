package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationDateValueViewModel
import com.anytypeio.anytype.presentation.sets.RelationTextValueViewModel
import com.anytypeio.anytype.ui.relations.RelationDateValueFragment
import com.anytypeio.anytype.ui.relations.RelationTextValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [EditGridCellModule::class])
@PerModal
interface EditGridCellSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: EditGridCellModule): Builder
        fun build(): EditGridCellSubComponent
    }

    fun inject(fragment: RelationTextValueFragment)
}

@Module
object EditGridCellModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideEditGridCellViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider
    ) = RelationTextValueViewModel.Factory(relations, values)
}

@Subcomponent(modules = [EditGridCellDateModule::class])
@PerModal
interface EditGridCellDateSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: EditGridCellDateModule): Builder
        fun build(): EditGridCellDateSubComponent
    }

    fun inject(fragment: RelationDateValueFragment)
}

@Module
object EditGridCellDateModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideEditGridCellViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider
    ) = RelationDateValueViewModel.Factory(relations, values)
}
