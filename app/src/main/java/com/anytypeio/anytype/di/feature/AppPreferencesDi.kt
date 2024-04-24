package com.anytypeio.anytype.di.feature

import android.content.Context
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.networkmode.SetNetworkMode
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.settings.PreferencesViewModel
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.NetworkModeCopyFileToCacheDirectory
import com.anytypeio.anytype.ui.onboarding.OnboardingNetworkSetupDialog
import com.anytypeio.anytype.ui.settings.system.PreferenceFragment
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [AppPreferencesDependencies::class],
    modules = [AppPreferencesModule::class]
)
@PerScreen
interface AppPreferencesComponent {

    @Component.Factory
    interface Factory {
        fun create(dependency: AppPreferencesDependencies): AppPreferencesComponent
    }

    fun inject(fragment: PreferenceFragment)
    fun inject(fragment: OnboardingNetworkSetupDialog)
}

@Module
object AppPreferencesModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCopyFileToCache(
        context: Context
    ): CopyFileToCacheDirectory = NetworkModeCopyFileToCacheDirectory(context)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        copyFileToCacheDirectory: CopyFileToCacheDirectory,
        getNetworkMode: GetNetworkMode,
        setNetworkMode: SetNetworkMode,
        analytics: Analytics
    ): PreferencesViewModel.Factory = PreferencesViewModel.Factory(
        copyFileToCacheDirectory = copyFileToCacheDirectory,
        getNetworkMode = getNetworkMode,
        setNetworkMode = setNetworkMode,
        analytics = analytics
    )
}

interface AppPreferencesDependencies : ComponentDependencies {
    fun context(): Context
    fun dispatchers(): AppCoroutineDispatchers
    fun authRepository(): AuthRepository
    fun analytics(): Analytics
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
}