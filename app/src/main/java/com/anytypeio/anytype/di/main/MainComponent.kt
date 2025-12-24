package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.common.ComponentDependenciesKey
import com.anytypeio.anytype.di.feature.AllContentDependencies
import com.anytypeio.anytype.di.feature.AppPreferencesDependencies
import com.anytypeio.anytype.di.feature.BacklinkOrAddToObjectDependencies
import com.anytypeio.anytype.di.feature.CreateBookmarkSubComponent
import com.anytypeio.anytype.di.feature.CreateObjectSubComponent
import com.anytypeio.anytype.di.feature.CreateObjectTypeDependencies
import com.anytypeio.anytype.di.feature.DateObjectDependencies
import com.anytypeio.anytype.di.feature.DebugSettingsSubComponent
import com.anytypeio.anytype.di.feature.EditTypePropertiesDependencies
import com.anytypeio.anytype.di.feature.EditorSubComponent
import com.anytypeio.anytype.di.feature.KeychainPhraseSubComponent
import com.anytypeio.anytype.di.feature.LinkToObjectDependencies
import com.anytypeio.anytype.di.feature.MainEntrySubComponent
import com.anytypeio.anytype.di.feature.MediaDependencies
import com.anytypeio.anytype.di.feature.MoveToDependencies
import com.anytypeio.anytype.di.feature.MySitesDependencies
import com.anytypeio.anytype.di.feature.ObjectSetSubComponent
import com.anytypeio.anytype.di.feature.ObjectTypeChangeSubComponent
import com.anytypeio.anytype.di.feature.ObjectTypeDependencies
import com.anytypeio.anytype.di.feature.PersonalizationSettingsSubComponent
import com.anytypeio.anytype.di.feature.PublishToWebDependencies
import com.anytypeio.anytype.di.feature.SpacePropertiesDependencies
import com.anytypeio.anytype.di.feature.SpaceTypesDependencies
import com.anytypeio.anytype.di.feature.SplashDependencies
import com.anytypeio.anytype.di.feature.auth.DeletedAccountDependencies
import com.anytypeio.anytype.di.feature.chats.ChatComponentDependencies
import com.anytypeio.anytype.di.feature.chats.ChatReactionDependencies
import com.anytypeio.anytype.di.feature.chats.SelectChatIconDependencies
import com.anytypeio.anytype.di.feature.chats.SelectChatReactionDependencies
import com.anytypeio.anytype.di.feature.gallery.GalleryInstallationComponentDependencies
import com.anytypeio.anytype.di.feature.home.HomeScreenDependencies
import com.anytypeio.anytype.di.feature.membership.MembershipComponentDependencies
import com.anytypeio.anytype.di.feature.membership.MembershipUpdateComponentDependencies
import com.anytypeio.anytype.di.feature.multiplayer.RequestJoinSpaceDependencies
import com.anytypeio.anytype.di.feature.multiplayer.ShareSpaceDependencies
import com.anytypeio.anytype.di.feature.multiplayer.SpaceJoinRequestDependencies
import com.anytypeio.anytype.di.feature.notifications.NotificationDependencies
import com.anytypeio.anytype.di.feature.notifications.PushContentDependencies
import com.anytypeio.anytype.di.feature.objects.SelectObjectTypeDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingDependencies
import com.anytypeio.anytype.di.feature.onboarding.OnboardingStartDependencies
import com.anytypeio.anytype.di.feature.onboarding.login.OnboardingMnemonicLoginDependencies
import com.anytypeio.anytype.di.feature.onboarding.signup.OnboardingMnemonicDependencies
import com.anytypeio.anytype.di.feature.onboarding.signup.OnboardingSoulCreationDependencies
import com.anytypeio.anytype.di.feature.participant.ParticipantComponentDependencies
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromLibraryDependencies
import com.anytypeio.anytype.di.feature.search.GlobalSearchDependencies
import com.anytypeio.anytype.di.feature.settings.AboutAppDependencies
import com.anytypeio.anytype.di.feature.settings.AppearanceDependencies
import com.anytypeio.anytype.di.feature.settings.DebugDependencies
import com.anytypeio.anytype.di.feature.settings.FilesStorageDependencies
import com.anytypeio.anytype.di.feature.settings.LogoutWarningSubComponent
import com.anytypeio.anytype.di.feature.settings.ProfileSubComponent
import com.anytypeio.anytype.di.feature.settings.SpacesStorageDependencies
import com.anytypeio.anytype.di.feature.sharing.SharingDependencies
import com.anytypeio.anytype.di.feature.spaces.CreateSpaceDependencies
import com.anytypeio.anytype.di.feature.spaces.SpaceListDependencies
import com.anytypeio.anytype.di.feature.spaces.SpaceSettingsDependencies
import com.anytypeio.anytype.di.feature.templates.TemplateBlankDependencies
import com.anytypeio.anytype.di.feature.templates.TemplateSelectDependencies
import com.anytypeio.anytype.di.feature.vault.VaultComponentDependencies
import com.anytypeio.anytype.di.feature.widgets.CreateChatObjectDependencies
import com.anytypeio.anytype.di.feature.widgets.SelectWidgetSourceDependencies
import com.anytypeio.anytype.di.feature.widgets.SelectWidgetTypeDependencies
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
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
        MembershipModule::class,
        DispatcherModule::class
    ]
)
interface MainComponent :
    AppearanceDependencies,
    HomeScreenDependencies,
    CollectionDependencies,
    RelationCreateFromLibraryDependencies,
    SplashDependencies,
    DeletedAccountDependencies,
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
    CreateSpaceDependencies,
    SpaceListDependencies,
    SpaceSettingsDependencies,
    SelectObjectTypeDependencies,
    SpacesStorageDependencies,
    AppPreferencesDependencies,
    ShareSpaceDependencies,
    SpaceJoinRequestDependencies,
    RequestJoinSpaceDependencies,
    MembershipComponentDependencies,
    GalleryInstallationComponentDependencies,
    NotificationDependencies,
    GlobalSearchDependencies,
    MembershipUpdateComponentDependencies,
    VaultComponentDependencies,
    AllContentDependencies,
    ChatComponentDependencies,
    SelectWidgetSourceDependencies,
    SelectWidgetTypeDependencies,
    LinkToObjectDependencies,
    MoveToDependencies,
    DateObjectDependencies,
    ObjectTypeDependencies,
    SelectChatReactionDependencies,
    SelectChatIconDependencies,
    ChatReactionDependencies,
    ParticipantComponentDependencies,
    EditTypePropertiesDependencies,
    DebugDependencies,
    CreateObjectTypeDependencies,
    SpaceTypesDependencies,
    SpacePropertiesDependencies,
    PushContentDependencies,
    PublishToWebDependencies,
    MySitesDependencies,
    MediaDependencies,
    CreateChatObjectDependencies,
    SharingDependencies
{

    fun inject(app: AndroidApplication)

    fun editorComponentBuilder(): EditorSubComponent.Builder
    fun createBookmarkBuilder(): CreateBookmarkSubComponent.Builder
    fun mainEntryComponentBuilder(): MainEntrySubComponent.Builder
    fun objectSetComponentBuilder(): ObjectSetSubComponent.Builder
    fun objectTypeChangeComponent(): ObjectTypeChangeSubComponent.Builder
    fun createObjectComponent(): CreateObjectSubComponent.Builder

    //region Settings

    fun profileComponent(): ProfileSubComponent.Builder
    fun debugSettingsBuilder(): DebugSettingsSubComponent.Builder
    fun keychainPhraseComponentBuilder(): KeychainPhraseSubComponent.Builder
    fun personalizationSettingsComponentBuilder(): PersonalizationSettingsSubComponent.Builder
    fun logoutWarningComponent(): LogoutWarningSubComponent.Builder

    //endregion

    fun chatPreviewContainer(): ChatPreviewContainer
    fun chatsDetailsSubscriptionContainer(): ChatsDetailsSubscriptionContainer
}

@Module
abstract class ComponentDependenciesModule {

    @Binds
    @IntoMap
    @ComponentDependenciesKey(AppearanceDependencies::class)
    abstract fun provideAppearanceDependencies(component: MainComponent): ComponentDependencies

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
    @ComponentDependenciesKey(RelationCreateFromLibraryDependencies::class)
    abstract fun provideRelationCreateFromLibraryDependencies(component: MainComponent): ComponentDependencies

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
    @ComponentDependenciesKey(SpaceListDependencies::class)
    abstract fun provideSpaceListDependencies(component: MainComponent): ComponentDependencies

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
    @ComponentDependenciesKey(ShareSpaceDependencies::class)
    abstract fun provideShareSpaceDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SpaceJoinRequestDependencies::class)
    abstract fun provideSpaceJoinRequestDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(MembershipComponentDependencies::class)
    abstract fun provideMembershipComponentDependencies(component: MainComponent): ComponentDependencies

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

    @Binds
    @IntoMap
    @ComponentDependenciesKey(GlobalSearchDependencies::class)
    abstract fun provideGlobalSearchDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(MembershipUpdateComponentDependencies::class)
    abstract fun provideMembershipUpdateComponentDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(ChatComponentDependencies::class)
    abstract fun provideDiscussionComponentDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(VaultComponentDependencies::class)
    abstract fun provideVaultComponentDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(AllContentDependencies::class)
    abstract fun provideAllContentDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SelectWidgetSourceDependencies::class)
    abstract fun provideSelectWidgetSourceDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SelectWidgetTypeDependencies::class)
    abstract fun provideSelectWidgetTypeDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(LinkToObjectDependencies::class)
    abstract fun provideLinkToObjectDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(MoveToDependencies::class)
    abstract fun provideMoveToDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(DateObjectDependencies::class)
    abstract fun provideDateObjectDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(ObjectTypeDependencies::class)
    abstract fun provideObjectTypeDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SelectChatReactionDependencies::class)
    abstract fun provideChatReactionPickerDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SelectChatIconDependencies::class)
    abstract fun provideSelectChatIconDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(ChatReactionDependencies::class)
    abstract fun provideChatReactionDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(ParticipantComponentDependencies::class)
    abstract fun provideParticipantComponentDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(DebugDependencies::class)
    abstract fun provideDebugDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(EditTypePropertiesDependencies::class)
    abstract fun provideEditTypePropertiesDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(CreateObjectTypeDependencies::class)
    abstract fun provideCreateObjectTypeDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SpaceTypesDependencies::class)
    abstract fun provideSpaceTypesDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SpacePropertiesDependencies::class)
    abstract fun provideSpacePropertiesDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(PushContentDependencies::class)
    abstract fun providePushContentDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(PublishToWebDependencies::class)
    abstract fun providePublishToWebDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(MySitesDependencies::class)
    abstract fun mySitesDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(MediaDependencies::class)
    abstract fun mediaDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(CreateChatObjectDependencies::class)
    abstract fun createChatObjectDependencies(component: MainComponent): ComponentDependencies

    @Binds
    @IntoMap
    @ComponentDependenciesKey(SharingDependencies::class)
    abstract fun sharingDependencies(component: MainComponent): ComponentDependencies
}