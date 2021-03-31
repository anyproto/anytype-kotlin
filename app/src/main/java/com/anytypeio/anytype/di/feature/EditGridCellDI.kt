package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectRelationDateValueViewModel
import com.anytypeio.anytype.presentation.sets.ObjectRelationTextValueViewModel
import com.anytypeio.anytype.ui.relations.ObjectRelationDateValueFragment
import com.anytypeio.anytype.ui.relations.ObjectRelationTextValueFragment
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

    fun inject(fragment: ObjectRelationTextValueFragment)
}

@Module
object EditGridCellModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideEditGridCellViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider
    ) = ObjectRelationTextValueViewModel.Factory(relations, values)
}

@Subcomponent(modules = [EditGridCellDateModule::class])
@PerModal
interface EditGridCellDateSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: EditGridCellDateModule): Builder
        fun build(): EditGridCellDateSubComponent
    }

    fun inject(fragment: ObjectRelationDateValueFragment)
}

@Module
object EditGridCellDateModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideEditGridCellViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider
    ) = ObjectRelationDateValueViewModel.Factory(relations, values)
}
