package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.common.ComponentDependenciesKey
import com.anytypeio.anytype.di.feature.*
import com.anytypeio.anytype.di.feature.auth.DeletedAccountSubcomponent
import com.anytypeio.anytype.di.feature.settings.AboutAppSubComponent
import com.anytypeio.anytype.di.feature.settings.AccountAndDataSubComponent
import com.anytypeio.anytype.di.feature.settings.AppearanceDependencies
import com.anytypeio.anytype.di.feature.settings.LogoutWarningSubComponent
import com.anytypeio.anytype.di.feature.settings.MainSettingsSubComponent
import com.anytypeio.anytype.di.feature.templates.TemplateSelectSubComponent
import com.anytypeio.anytype.di.feature.templates.TemplateSubComponent
import com.anytypeio.anytype.di.feature.wallpaper.WallpaperSelectSubComponent
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
        DeviceModule::class,
        UtilModule::class,
        EmojiModule::class,
        ClipboardModule::class,
        AnalyticsModule::class,
        LocalNetworkAddressModule::class
    ]
)
interface MainComponent : AppearanceDependencies {
    fun inject(app: AndroidApplication)

    fun splashComponentBuilder(): SplashSubComponent.Builder
    fun homeDashboardComponentBuilder(): HomeDashboardSubComponent.Builder
    fun editorComponentBuilder(): EditorSubComponent.Builder
    fun archiveComponentBuilder(): ArchiveSubComponent.Builder
    fun linkAddComponentBuilder(): LinkSubComponent.Builder
    fun createBookmarkBuilder(): CreateBookmarkSubComponent.Builder
    fun navigationComponentBuilder(): PageNavigationSubComponent.Builder
    fun linkToObjectBuilder(): LinkToObjectSubComponent.Builder
    fun linkToObjectOrWebBuilder(): LinkToObjectOrWebSubComponent.Builder
    fun moveToBuilder(): MoveToSubComponent.Builder
    fun objectSearchComponentBuilder(): ObjectSearchSubComponent.Builder
    fun mainEntryComponentBuilder(): MainEntrySubComponent.Builder
    fun createSetComponentBuilder(): CreateSetSubComponent.Builder
    fun createObjectTypeComponentBuilder(): CreateObjectTypeSubComponent.Builder
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
}