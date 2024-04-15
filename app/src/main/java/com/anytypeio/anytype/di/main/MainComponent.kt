package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.common.ComponentDependenciesKey
import com.anytypeio.anytype.di.feature.AppPreferencesDependencies
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
import com.anytypeio.anytype.di.feature.PersonalizationSettingsSubComponent
import com.anytypeio.anytype.di.feature.SplashDependencies
import com.anytypeio.anytype.di.feature.auth.DeletedAccountDependencies
import com.anytypeio.anytype.di.feature.gallery.GalleryInstallationComponentDependencies
import com.anytypeio.anytype.di.feature.home.HomeScreenDependencies
import com.anytypeio.anytype.di.feature.library.LibraryDependencies
import com.anytypeio.anytype.di.feature.multiplayer.RequestJoinSpaceDependencies
import com.anytypeio.anytype.di.feature.multiplayer.ShareSpaceDependencies
import com.anytypeio.anytype.di.feature.multiplayer.SpaceJoinRequestDependencies
import com.anytypeio.anytype.di.feature.notifications.NotificationDependencies
import com.anytypeio.anytype.di.feature.objects.SelectObjectTypeDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingStartDependencies
import com.anytypeio.anytype.di.feature.onboarding.login.OnboardingMnemonicLoginDependencies
import com.anytypeio.anytype.di.feature.onboarding.signup.OnboardingMnemonicDependencies
import com.anytypeio.anytype.di.feature.onboarding.signup.OnboardingSoulCreationDependencies
import com.anytypeio.anytype.di.feature.payments.PaymentsComponentDependencies
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromLibraryDependencies
import com.anytypeio.anytype.di.feature.relations.RelationEditDependencies
import com.anytypeio.anytype.di.feature.settings.AboutAppDependencies
import com.anytypeio.anytype.di.feature.settings.AppearanceDependencies
import com.anytypeio.anytype.di.feature.settings.FilesStorageDependencies
import com.anytypeio.anytype.di.feature.settings.LogoutWarningSubComponent
import com.anytypeio.anytype.di.feature.settings.ProfileSubComponent
import com.anytypeio.anytype.di.feature.settings.SpacesStorageDependencies
import com.anytypeio.anytype.di.feature.sharing.AddToAnytypeDependencies
import com.anytypeio.anytype.di.feature.spaces.CreateSpaceDependencies
import com.anytypeio.anytype.di.feature.spaces.SelectSpaceDependencies
import com.anytypeio.anytype.di.feature.spaces.SpaceSettingsDependencies
import com.anytypeio.anytype.di.feature.templates.TemplateBlankDependencies
import com.anytypeio.anytype.di.feature.templates.TemplateSelectDependencies
import com.anytypeio.anytype.di.feature.templates.TemplateSubComponent
import com.anytypeio.anytype.di.feature.types.CreateObjectTypeDependencies
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
        CrashReportingModule::class,
        TemplatesModule::class,
        NetworkModeModule::class,
        NotificationsModule::class,
        MembershipModule::class
    ]
)
interface MainComponent :
    AppearanceDependencies,
    LibraryDependencies,
    HomeScreenDependencies,
    CollectionDependencies,
    CreateObjectTypeDependencies,
    TypeIconPickDependencies,
    TypeEditDependencies,
    RelationCreateFromLibraryDependencies,
    RelationEditDependencies,
    SplashDependencies,
    DeletedAccountDependencies,
    MigrationErrorDependencies,
    BacklinkOrAddToObjectDependencies,
    FilesStorageDependencies,
    OnboardingDependencies,
    OnboardingStartDependencies,
    OnboardingMnemonicDependencies,
    OnboardingMnemonicLoginDependencies,
    OnboardingSoulCreationDependencies,
    AboutAppDependencies,
    TemplateBlankDependencies,
    TemplateSelectDependencies,
    SelectSpaceDependencies,
    CreateSpaceDependencies,
    SpaceSettingsDependencies,
    SelectObjectTypeDependencies,
    SpacesStorageDependencies,
    AppPreferencesDependencies,
    AddToAnytypeDependencies,
    ShareSpaceDependencies,
    SpaceJoinRequestDependencies,
    RequestJoinSpaceDependencies,
    PaymentsComponentDependencies,
    GalleryInstallationComponentDependencies,
    NotificationDependencies
{

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


    //region Settings

    fun profileComponent(): ProfileSubComponent.Builder
    fun debugSettingsBuilder(): DebugSettingsSubComponent.Builder
    fun keychainPhraseComponentBuilder(): KeychainPhraseSubComponent.Builder
    fun personalizationSettingsComponentBuilder(): PersonalizationSettingsSubComponent.Builder
    fun logoutWarningComponent(): LogoutWarningSubComponent.Builder

    //endregion
}

@Module
abstract class ComponentDependenciesModule {

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
    @ComponentDependenciesKey(CreateObjectTypeDependencies::class)
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
    @ComponentDependenciesKey(OnboardingDependencies::class)
    abstract fun provideOnboardingDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(OnboardingStartDependencies::class)
    abstract fun provideOnboardingStartDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(OnboardingMnemonicDependencies::class)
    abstract fun provideOnboardingMnemonicDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(OnboardingMnemonicLoginDependencies::class)
    abstract fun provideOnboardingMnemonicLoginDependencies(component: MainComponent): ComponentDependencies

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
    @ComponentDependenciesKey(TemplateBlankDependencies::class)
    abstract fun provideTemplateBlankDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(TemplateSelectDependencies::class)
    abstract fun provideTemplateSelectDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SelectSpaceDependencies::class)
    abstract fun provideSelectSpaceDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(CreateSpaceDependencies::class)
    abstract fun provideCreateSpaceDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SpaceSettingsDependencies::class)
    abstract fun provideSpaceSettingsDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SelectObjectTypeDependencies::class)
    abstract fun provideCreateObjectOfTypeDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SpacesStorageDependencies::class)
    abstract fun provideSpacesStorageDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(AppPreferencesDependencies::class)
    abstract fun providePreferencesDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(AddToAnytypeDependencies::class)
    abstract fun provideAddToAnytypeDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(ShareSpaceDependencies::class)
    abstract fun provideShareSpaceDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SpaceJoinRequestDependencies::class)
    abstract fun provideSpaceJoinRequestDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(PaymentsComponentDependencies::class)
    abstract fun providePaymentsComponentDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(RequestJoinSpaceDependencies::class)
    abstract fun provideRequestToJoinSpaceDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(GalleryInstallationComponentDependencies::class)
    abstract fun provideGalleryInstallationDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(NotificationDependencies::class)
    abstract fun provideNotificationDependencies(component: MainComponent): ComponentDependencies
}