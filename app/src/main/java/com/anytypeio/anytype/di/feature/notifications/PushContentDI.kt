package com.anytypeio.anytype.di.feature.notifications

import android.content.Context
import com.anytypeio.anytype.device.notifications.AnytypeFirebaseMessagingService
import com.anytypeio.anytype.device.notifications.DecryptionPushContentService
import com.anytypeio.anytype.device.notifications.DecryptionPushContentServiceProtocol
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.notifications.RegisterDeviceTokenUseCase
import com.anytypeio.anytype.presentation.notifications.PushKeyProvider
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Singleton
@Component(
    dependencies = [PushContentDependencies::class],
    modules = [
        PushContentModule::class,
        PushContentModule.Declarations::class],
)
interface PushContentComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: PushContentDependencies): PushContentComponent
    }

    fun inject(service: AnytypeFirebaseMessagingService)
}

@Module
object PushContentModule {

    @Singleton
    @Provides
    fun provideUseCase(
        repository: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): RegisterDeviceTokenUseCase {
        return RegisterDeviceTokenUseCase(
            repository = repository,
            dispatchers = dispatchers
        )
    }

    @Singleton
    @Provides
    fun provideDecryptionPushContentService(
        pushKeyProvider: PushKeyProvider
    ): DecryptionPushContentServiceProtocol {
        return DecryptionPushContentService(
            pushKeyProvider = pushKeyProvider
        )
    }

    @Module
    interface Declarations {

    }
}

interface PushContentDependencies : ComponentDependencies {
    fun context(): Context
    fun logger(): Logger
    fun authRepository(): AuthRepository
    fun dispatchers(): AppCoroutineDispatchers

    fun pushKeyProvider(): PushKeyProvider

    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    fun coroutineScope(): CoroutineScope
}