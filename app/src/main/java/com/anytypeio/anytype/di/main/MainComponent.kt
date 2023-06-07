package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.common.ComponentDependenciesKey
import com.anytypeio.anytype.di.feature.AuthSubComponent
import com.anytypeio.anytype.di.feature.BacklinkOrAddToObjectDependencies
import com.anytypeio.anytype.di.feature.CreateBookmarkSubComponent
import com.anytypeio.anytype.di.feature.CreateObjectSubComponent
import com.anytypeio.anytype.di.feature.DebugSettingsSubComponent
import com.anytypeio.anytype.di.feature.EditorSubComponent
import com.anytypeio.anytype.di.feature.KeychainPhraseSubComponent
import com.anytypeio.anytype.di.feature.LinkToObjectSubComponent
import com.anytypeio.anytype.di.feature.MainEntrySubComponent
import com.anytypeio.anytype.di.feature.MoveToSubComponent
import com.anytypeio.anytype.di.feature.ObjectSearchSubComponent
import com.anytypeio.anytype.di.feature.ObjectSetSubComponent
import com.anytypeio.anytype.di.feature.ObjectTypeChangeSubComponent
import com.anytypeio.anytype.di.feature.OtherSettingsSubComponent
import com.anytypeio.anytype.di.feature.SplashDependencies
import com.anytypeio.anytype.di.feature.auth.DeletedAccountDependencies
import com.anytypeio.anytype.di.feature.home.HomeScreenDependencies
import com.anytypeio.anytype.di.feature.library.LibraryDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingAuthDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingMnemonicDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingSoulCreationAnimDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingSoulCreationDependencies
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromLibraryDependencies
import com.anytypeio.anytype.di.feature.relations.RelationEditDependencies
import com.anytypeio.anytype.di.feature.settings.AboutAppDependencies
import com.anytypeio.anytype.di.feature.settings.ProfileSubComponent
import com.anytypeio.anytype.di.feature.settings.AppearanceDependencies
import com.anytypeio.anytype.di.feature.settings.FilesStorageDependencies
import com.anytypeio.anytype.di.feature.settings.LogoutWarningSubComponent
import com.anytypeio.anytype.di.feature.settings.MainSettingsSubComponent
import com.anytypeio.anytype.di.feature.templates.TemplateSelectSubComponent
import com.anytypeio.anytype.di.feature.templates.TemplateSubComponent
import com.anytypeio.anytype.di.feature.types.TypeCreationDependencies
import com.anytypeio.anytype.di.feature.types.TypeEditDependencies
import com.anytypeio.anytype.di.feature.types.TypeIconPickDependencies
import com.anytypeio.anytype.di.feature.update.MigrationErrorDependencies
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
        LocalNetworkProviderModule::class,
        SubscriptionsModule::class,
        CrashReportingModule::class
    ]
)
interface MainComponent :
    AppearanceDependencies,
    LibraryDependencies,
    HomeScreenDependencies,
    CollectionDependencies,
    TypeCreationDependencies,
    TypeIconPickDependencies,
    TypeEditDependencies,
    RelationCreateFromLibraryDependencies,
    RelationEditDependencies,
    SplashDependencies,
    DeletedAccountDependencies,
    MigrationErrorDependencies,
    BacklinkOrAddToObjectDependencies,
    FilesStorageDependencies,
    OnboardingAuthDependencies,
    OnboardingMnemonicDependencies,
    OnboardingSoulCreationDependencies,
    AboutAppDependencies,
    OnboardingSoulCreationAnimDependencies {

    fun inject(app: AndroidApplication)

    fun editorComponentBuilder(): EditorSubComponent.Builder
    fun createBookmarkBuilder(): CreateBookmarkSubComponent.Builder
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
    //endregion

    //region Settings

    fun profileComponent(): ProfileSubComponent.Builder
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

    @Binds
    @IntoMap
    @ComponentDependenciesKey(TypeEditDependencies::class)
    abstract fun provideTypeEditDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(TypeIconPickDependencies::class)
    abstract fun provideTypeIconPickDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(RelationCreateFromLibraryDependencies::class)
    abstract fun provideRelationCreateFromLibraryDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(RelationEditDependencies::class)
    abstract fun provideRelationEditDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SplashDependencies::class)
    abstract fun provideSplashDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(DeletedAccountDependencies::class)
    abstract fun provideDeletedAccountDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(MigrationErrorDependencies::class)
    abstract fun migrationErrorDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(BacklinkOrAddToObjectDependencies::class)
    abstract fun provideBackLinkDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(FilesStorageDependencies::class)
    abstract fun provideFilesStorageDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(OnboardingAuthDependencies::class)
    abstract fun provideOnboardingAuthDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(OnboardingMnemonicDependencies::class)
    abstract fun provideOnboardingMnemonicDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(OnboardingSoulCreationDependencies::class)
    abstract fun provideOnboardingSoulCreationDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(AboutAppDependencies::class)
    abstract fun provideAboutAppDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(OnboardingSoulCreationAnimDependencies::class)
    abstract fun provideOnboardingSoulCreationAnimDependencies(component: MainComponent): ComponentDependencies

}