package com.agileburo.anytype.di.main

import com.agileburo.anytype.di.feature.AuthSubComponent
import com.agileburo.anytype.di.feature.DesktopSubComponent
import com.agileburo.anytype.di.feature.KeychainPhraseSubComponent
import com.agileburo.anytype.di.feature.ProfileSubComponent
import com.agileburo.anytype.di.feature.SplashSubComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ContextModule::class,
        DataModule::class
    ]
)
interface MainComponent {
    fun authComponentBuilder(): AuthSubComponent.Builder
    fun profileComponentBuilder(): ProfileSubComponent.Builder
    fun splashComponentBuilder(): SplashSubComponent.Builder
    fun keychainPhraseComponentBuilder(): KeychainPhraseSubComponent.Builder
    fun desktopComponentBuilder(): DesktopSubComponent.Builder
}