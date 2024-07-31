package com.anytypeio.anytype.di.feature.history

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.history.VersionHistoryVMFactory
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel
import com.anytypeio.anytype.ui.history.VersionHistoryFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Subcomponent

@Subcomponent(
    modules = [
        VersionHistoryModule::class,
        VersionHistoryModule.Declarations::class
    ]
)
@PerModal
interface VersionHistoryComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun vmParams(vmParams: VersionHistoryViewModel.VmParams): Builder
        fun build(): VersionHistoryComponent
    }

//    @Component.Factory
//    interface Factory {
//        fun create(
//            dependencies: VersionHistoryComponentDependencies,
//            @BindsInstance vmParams: VersionHistoryViewModel.VmParams
//        ): VersionHistoryComponent
//    }

    fun inject(fragment: VersionHistoryFragment)
}

@Module
object VersionHistoryModule {

    @Module
    interface Declarations {

        @PerModal
        @Binds
        fun bindViewModelFactory(
            factory: VersionHistoryVMFactory
        ): ViewModelProvider.Factory
    }
}
//
//interface VersionHistoryComponentDependencies : ComponentDependencies {
//    fun analytics(): Analytics
//    fun blockRepository(): BlockRepository
//    fun appCoroutineDispatchers(): AppCoroutineDispatchers
//    fun dateProvider(): DateProvider
//    fun localeProvider(): LocaleProvider
//    fun provideUrlBuilder(): UrlBuilder
//    fun provideStoreOfRelations(): StoreOfRelations
//    fun provideStoreOfTypes(): StoreOfObjectTypes
//    fun provideToggleStateHolder(): ToggleStateHolder
//}

