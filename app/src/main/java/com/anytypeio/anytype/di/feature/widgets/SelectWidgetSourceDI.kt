package com.anytypeio.anytype.di.feature.widgets

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.SelectWidgetSourceViewModel
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.ui.widgets.SelectWidgetSourceFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [SelectWidgetSourceModule::class]
)
@PerModal
interface SelectWidgetSourceSubcomponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectWidgetSourceModule): Builder
        fun build(): SelectWidgetSourceSubcomponent
    }

    fun inject(fragment: SelectWidgetSourceFragment)
}

@Module
object SelectWidgetSourceModule {

    @JvmStatic
    @PerModal
    @Provides
    fun factory(
        urlBuilder: UrlBuilder,
        analytics: Analytics,
        searchObjects: SearchObjects,
        getObjectTypes: GetObjectTypes,
        dispatcher: Dispatcher<WidgetDispatchEvent>,
        spaceManager: SpaceManager
    ): SelectWidgetSourceViewModel.Factory = SelectWidgetSourceViewModel.Factory(
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        analytics = analytics,
        getObjectTypes = getObjectTypes,
        dispatcher = dispatcher,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideGetObjectTypesUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetObjectTypes = GetObjectTypes(repository, dispatchers)
}