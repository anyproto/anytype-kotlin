package com.agileburo.anytype.di.common

import com.agileburo.anytype.di.feature.*
import com.agileburo.anytype.di.main.MainComponent

class ComponentManager(private val main: MainComponent) {

    val mainComponent = main

    private val authComponent = Component {
        main.authComponentBuilder().authModule(AuthModule()).build()
    }

    val startLoginComponent = Component {
        authComponent
            .get()
            .startLoginComponentBuilder()
            .startLoginModule(StartLoginModule())
            .build()
    }

    val createAccountComponent = Component {
        authComponent
            .get()
            .createAccountComponentBuilder()
            .createAccountModule(CreateAccountModule())
            .build()
    }

    val setupNewAccountComponent = Component {
        authComponent
            .get()
            .setupNewAccountComponentBuilder()
            .setupNewAccountModule(SetupNewAccountModule())
            .build()
    }

    val setupSelectedAccountComponent = Component {
        authComponent
            .get()
            .setupSelectedAccountComponentBuilder()
            .setupSelectedAccountModule(SetupSelectedAccountModule())
            .build()
    }

    val selectAccountComponent = Component {
        authComponent
            .get()
            .selectAccountComponentBuilder()
            .selectAccountModule(SelectAccountModule())
            .build()
    }

    val keychainLoginComponent = Component {
        authComponent
            .get()
            .keychainLoginComponentBuilder()
            .keychainLoginModule(KeychainLoginModule())
            .build()
    }

    val profileComponent = Component {
        main
            .profileComponentBuilder()
            .profileModule(ProfileModule())
            .build()
    }

    val splashLoginComponent = Component {
        main
            .splashComponentBuilder()
            .module(SplashModule())
            .build()
    }

    val keychainPhraseComponent = Component {
        main
            .keychainPhraseComponentBuilder()
            .keychainPhraseModule(KeychainPhraseModule())
            .build()
    }

    val desktopComponent = Component {
        main
            .homeDashboardComponentBuilder()
            .homeDashboardModule(HomeDashboardModule())
            .build()
    }

    val databaseViewComponent = Component {
        main
            .databaseViewComponentBuilder()
            .databaseViewModule(TableBoardModule())
            .build()
    }

    val pageComponent = Component {
        main
            .pageComponentBuilder()
            .pageModule(PageModule())
            .build()
    }

    val contactsComponent = Component {
        main
            .contactsComponentBuilder()
            .contactsModule(ListBoardModule())
            .build()
    }

    val linkAddComponent = Component {
        main
            .linkAddComponentBuilder()
            .linkModule(LinkModule())
            .build()
    }

    val pageIconPickerSubComponent = Component {
        main
            .pageIconPickerBuilder()
            .pageIconPickerModule(PageIconPickerModule())
            .build()
    }

    val createBookmarkSubComponent = Component {
        main
            .createBookmarkBuilder()
            .createBookmarkModule(CreateBookmarkModule())
            .build()
    }

    class Component<T>(private val builder: () -> T) {

        private var instance: T? = null

        fun get() = instance ?: builder().also { instance = it }

        fun new() = builder().also { instance = it }

        fun release() {
            instance = null
        }
    }
}