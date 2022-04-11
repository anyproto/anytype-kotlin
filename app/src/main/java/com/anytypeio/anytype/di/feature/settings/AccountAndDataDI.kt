package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.ui.settings.AccountAndDataFragment
import com.anytypeio.anytype.ui_settings.account.AccountAndDataViewModel
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AccountAndDataModule::class])
@PerScreen
interface AccountAndDataSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AccountAndDataModule): Builder
        fun build(): AccountAndDataSubComponent
    }

    fun inject(fragment: AccountAndDataFragment)
}

@Module
object AccountAndDataModule {
    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        clearFileCache: ClearFileCache,
        deleteAccount: DeleteAccount,
        analytics: Analytics
    ): AccountAndDataViewModel.Factory = AccountAndDataViewModel.Factory(
        clearFileCache = clearFileCache,
        deleteAccount = deleteAccount,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun clearFileCache(repo: BlockRepository): ClearFileCache = ClearFileCache(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun logout(repo: AuthRepository): Logout = Logout(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun deleteAccount(repo: AuthRepository): DeleteAccount = DeleteAccount(repo)
}