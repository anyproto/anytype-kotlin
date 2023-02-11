package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.common.ComponentDependenciesKey
import com.anytypeio.anytype.di.feature.ArchiveSubComponent
import com.anytypeio.anytype.di.feature.AuthSubComponent
import com.anytypeio.anytype.di.feature.CreateBookmarkSubComponent
import com.anytypeio.anytype.di.feature.CreateObjectSubComponent
import com.anytypeio.anytype.di.feature.DebugSettingsSubComponent
import com.anytypeio.anytype.di.feature.EditorSubComponent
import com.anytypeio.anytype.di.feature.HomeDashboardSubComponent
import com.anytypeio.anytype.di.feature.KeychainPhraseSubComponent
import com.anytypeio.anytype.di.feature.LinkToObjectSubComponent
import com.anytypeio.anytype.di.feature.MainEntrySubComponent
import com.anytypeio.anytype.di.feature.MoveToSubComponent
import com.anytypeio.anytype.di.feature.ObjectSearchSubComponent
import com.anytypeio.anytype.di.feature.ObjectSetSubComponent
import com.anytypeio.anytype.di.feature.ObjectTypeChangeSubComponent
import com.anytypeio.anytype.di.feature.OtherSettingsSubComponent
import com.anytypeio.anytype.di.feature.PageNavigationSubComponent
import com.anytypeio.anytype.di.feature.SplashSubComponent
import com.anytypeio.anytype.di.feature.auth.DeletedAccountSubcomponent
import com.anytypeio.anytype.di.feature.home.HomeScreenDependencies
import com.anytypeio.anytype.di.feature.library.LibraryDependencies
import com.anytypeio.anytype.di.feature.settings.AboutAppSubComponent
import com.anytypeio.anytype.di.feature.settings.AccountAndDataSubComponent
import com.anytypeio.anytype.di.feature.settings.AppearanceDependencies
import com.anytypeio.anytype.di.feature.settings.LogoutWarningSubComponent
import com.anytypeio.anytype.di.feature.settings.MainSettingsSubComponent
import com.anytypeio.anytype.di.feature.templates.TemplateSelectSubComponent
import com.anytypeio.anytype.di.feature.templates.TemplateSubComponent
import com.anytypeio.anytype.di.feature.types.TypeCreationDependencies
import com.anytypeio.anytype.di.feature.wallpaper.WallpaperSelectSubComponent
import com.anytypeio.anytype.ui.widgets.collection.CollectionDependencies
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ComponentDependenciesModule::class,
        ContextModule::class,
        DataModule::class,
        EventModule::class,
        ConfigModule::class,
        WorkspaceModule::class,
        DeviceModule::class,
        UtilModule::class,
        EmojiModule::class,
        ClipboardModule::class,
        AnalyticsModule::class,
        LocalNetworkAddressModule::class,
        SubscriptionsModule::class
    ]
)
interface MainComponent :
    AppearanceDependencies,
    LibraryDependencies,
    HomeScreenDependencies,
    CollectionDependencies,
    TypeCreationDependencies {
    fun inject(app: AndroidApplication)

    fun splashComponentBuilder(): SplashSubComponent.Builder
    fun homeDashboardComponentBuilder(): HomeDashboardSubComponent.Builder
    fun editorComponentBuilder(): EditorSubComponent.Builder
    fun archiveComponentBuilder(): ArchiveSubComponent.Builder
    fun createBookmarkBuilder(): CreateBookmarkSubComponent.Builder
    fun navigationComponentBuilder(): PageNavigationSubComponent.Builder
    fun linkToObjectBuilder(): LinkToObjectSubComponent.Builder
    fun moveToBuilder(): MoveToSubComponent.Builder
    fun objectSearchComponentBuilder(): ObjectSearchSubComponent.Builder
    fun mainEntryComponentBuilder(): MainEntrySubComponent.Builder
    fun objectSetComponentBuilder(): ObjectSetSubComponent.Builder
    fun objectTypeChangeComponent(): ObjectTypeChangeSubComponent.Builder
    fun wallpaperSelectComponent(): WallpaperSelectSubComponent.Builder
    fun createObjectComponent(): CreateObjectSubComponent.Builder
    fun templateComponentFactory(): TemplateSubComponent.Factory
    fun templateSelectComponentFactory(): TemplateSelectSubComponent.Factory

    //region Auth

    fun authComponentBuilder(): AuthSubComponent.Builder
    fun deletedAccountBuilder(): DeletedAccountSubcomponent.Builder

    //endregion

    //region Settings

    fun aboutAppComponent(): AboutAppSubComponent.Builder
    fun accountAndDataComponent(): AccountAndDataSubComponent.Builder
    fun debugSettingsBuilder(): DebugSettingsSubComponent.Builder
    fun keychainPhraseComponentBuilder(): KeychainPhraseSubComponent.Builder
    fun otherSettingsComponentBuilder(): OtherSettingsSubComponent.Builder
    fun logoutWarningComponent(): LogoutWarningSubComponent.Builder
    fun mainSettingsComponent(): MainSettingsSubComponent.Builder

    //endregion
}

@Module
private abstract class ComponentDependenciesModule private constructor() {

    @Binds
    @IntoMap
    @ComponentDependenciesKey(AppearanceDependencies::class)
    abstract fun provideAppearanceDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(LibraryDependencies::class)
    abstract fun provideLibraryDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(HomeScreenDependencies::class)
    abstract fun provideHomeScreenDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(CollectionDependencies::class)
    abstract fun provideHomeWidgetDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(TypeCreationDependencies::class)
    abstract fun provideTypeCreationDependencies(component: MainComponent): ComponentDependencies

}