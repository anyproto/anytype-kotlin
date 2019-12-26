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
    fun databaseViewComponentBuilder(): TableBoardSubComponent.Builder
    fun contactsComponentBuilder(): ListBoardSubComponent.Builder
    fun editDatabaseComponentBuilder(): EditDatabaseSubComponent.Builder
    fun switchDisplayViewComponentBuilder(): SwitchDisplayViewSubComponent.Builder
    fun customizeDisplayViewComponentBuilder(): CustomizeDisplayViewSubComponent.Builder
    fun detailsBuilder(): DetailsSubComponent.Builder
    fun detailEditBuilder(): DetailEditSubComponent.Builder
    fun detailsReorderBuilder(): DetailsReorderSubComponent.Builder
    fun pageComponentBuilder(): PageSubComponent.Builder
}