package com.anytypeio.anytype.di.feature.history

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.presentation.history.VersionHistoryVMFactory
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel
import com.anytypeio.anytype.ui.history.VersionHistoryFragment
import dagger.Binds
import dagger.BindsInstance
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

