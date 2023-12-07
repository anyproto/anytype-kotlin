package com.anytypeio.anytype.di.feature

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.presentation.settings.PreferencesViewModel
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.DefaultCopyFileToCacheDirectory
import com.anytypeio.anytype.ui.settings.system.PreferenceFragment
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named

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
}

@Module
object AppPreferencesModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCopyFileToCache(
        context: Context
    ): CopyFileToCacheDirectory = DefaultCopyFileToCacheDirectory(context)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        copyFileToCacheDirectory: CopyFileToCacheDirectory,
        @Named("network_mode") sharedPreferences: SharedPreferences
    ): PreferencesViewModel.Factory = PreferencesViewModel.Factory(
        copyFileToCacheDirectory = copyFileToCacheDirectory,
        sharedPreferences = sharedPreferences
    )
}

interface AppPreferencesDependencies : ComponentDependencies {
    fun context(): Context
    @Named("network_mode")
    fun sharedPreferences(): SharedPreferences
}