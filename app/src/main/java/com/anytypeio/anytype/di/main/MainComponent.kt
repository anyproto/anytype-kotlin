package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.feature.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ContextModule::class,
        DataModule::class,
        EventModule::class,
        ConfigModule::class,
        DeviceModule::class,
        UtilModule::class,
        EmojiModule::class,
        ClipboardModule::class,
        AnalyticsModule::class
    ]
)
interface MainComponent {
    fun inject(app: AndroidApplication)

    fun authComponentBuilder(): AuthSubComponent.Builder
    fun profileComponentBuilder(): ProfileSubComponent.Builder
    fun splashComponentBuilder(): SplashSubComponent.Builder
    fun keychainPhraseComponentBuilder(): KeychainPhraseSubComponent.Builder
    fun homeDashboardComponentBuilder(): HomeDashboardSubComponent.Builder
    fun editorComponentBuilder(): EditorSubComponent.Builder
    fun archiveComponentBuilder(): ArchiveSubComponent.Builder
    fun linkAddComponentBuilder(): LinkSubComponent.Builder
    fun createBookmarkBuilder(): CreateBookmarkSubComponent.Builder
    fun debugSettingsBuilder(): DebugSettingsSubComponent.Builder
    fun navigationComponentBuilder(): PageNavigationSubComponent.Builder
    fun linkToObjectBuilder(): LinkToObjectSubComponent.Builder
    fun moveToBuilder(): MoveToSubComponent.Builder
    fun objectSearchComponentBuilder(): ObjectSearchSubComponent.Builder
    fun mainEntryComponentBuilder(): MainEntrySubComponent.Builder
    fun createSetComponentBuilder(): CreateSetSubComponent.Builder
    fun createObjectTypeComponentBuilder(): CreateObjectTypeSubComponent.Builder
    fun objectSetComponentBuilder(): ObjectSetSubComponent.Builder
}