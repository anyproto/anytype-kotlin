package com.agileburo.anytype.di.main

import com.agileburo.anytype.di.feature.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ContextModule::class,
        DataModule::class,
        ImageModule::class
    ]
)
interface MainComponent {
    fun authComponentBuilder(): AuthSubComponent.Builder
    fun profileComponentBuilder(): ProfileSubComponent.Builder
    fun splashComponentBuilder(): SplashSubComponent.Builder
    fun keychainPhraseComponentBuilder(): KeychainPhraseSubComponent.Builder
    fun homeDashboardComponentBuilder(): HomeDashboardSubComponent.Builder
    fun databaseViewComponentBuilder(): DatabaseViewSubComponent.Builder
    fun contactsComponentBuilder(): ContactsSubComponent.Builder
    fun pageComponentBuilder(): PageSubComponent.Builder
}