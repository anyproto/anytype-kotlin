package com.anytypeio.anytype.di.feature.notifications

import android.content.Context
import com.anytypeio.anytype.device.AnytypePushService
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.notifications.RegisterDeviceTokenUseCase
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

    fun inject(service: AnytypePushService)
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

    @Module
    interface Declarations {

    }
}

interface PushContentDependencies : ComponentDependencies {
    fun context(): Context
    fun logger(): Logger
    fun authRepository(): AuthRepository
    fun dispatchers(): AppCoroutineDispatchers

    fun deviceTokenSavingService(): DeviceTokenStoringService

    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    fun coroutineScope(): CoroutineScope
}