package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.presentation.keychain.KeychainPhraseViewModelFactory
import com.anytypeio.anytype.ui.dashboard.DashboardMnemonicReminderDialog
import com.anytypeio.anytype.ui.profile.KeychainPhraseDialog
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(
    modules = [KeychainPhraseModule::class]
)
@PerScreen
interface KeychainPhraseSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun keychainPhraseModule(module: KeychainPhraseModule): Builder
        fun build(): KeychainPhraseSubComponent
    }

    fun inject(fragment: KeychainPhraseDialog)
    fun inject(fragment: DashboardMnemonicReminderDialog)
}

@Module
object KeychainPhraseModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideKeychainPhraseViewModelFactory(
        getMnemonic: GetMnemonic,
        analytics: Analytics
    ) = KeychainPhraseViewModelFactory(
        getMnemonic = getMnemonic,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetMnemonicUseCase(
        repository: AuthRepository
    ): GetMnemonic = GetMnemonic(
        repository = repository
    )
}