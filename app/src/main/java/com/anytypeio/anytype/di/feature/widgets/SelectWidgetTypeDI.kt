package com.anytypeio.anytype.di.feature.widgets

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.SelectWidgetTypeViewModel
import com.anytypeio.anytype.ui.widgets.SelectWidgetTypeFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [SelectWidgetTypeModule::class]
)
@PerModal
interface SelectWidgetTypeSubcomponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectWidgetTypeModule): Builder
        fun build(): SelectWidgetTypeSubcomponent
    }

    fun inject(fragment: SelectWidgetTypeFragment)
}

@Module
object SelectWidgetTypeModule {

    @JvmStatic
    @PerModal
    @Provides
    fun factory(
        dispatcher: Dispatcher<Payload>,
        updateWidget: UpdateWidget,
        appCoroutineDispatchers: AppCoroutineDispatchers
    ): SelectWidgetTypeViewModel.Factory = SelectWidgetTypeViewModel.Factory(
        dispatcher = dispatcher,
        updateWidget = updateWidget,
        appCoroutineDispatchers = appCoroutineDispatchers
    )
}