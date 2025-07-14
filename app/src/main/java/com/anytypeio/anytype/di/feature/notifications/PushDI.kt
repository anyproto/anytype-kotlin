package com.anytypeio.anytype.di.feature.notifications

import android.content.Context
import com.anytypeio.anytype.device.AnytypePushService
import com.anytypeio.anytype.device.DefaultPushMessageProcessor
import com.anytypeio.anytype.device.PushMessageProcessor
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.notifications.CryptoService
import com.anytypeio.anytype.presentation.notifications.CryptoServiceImpl
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentServiceImpl
import com.anytypeio.anytype.presentation.notifications.SignatureVerificationService
import com.anytypeio.anytype.presentation.notifications.SignatureVerificationServiceImpl
import com.anytypeio.anytype.domain.notifications.PushKeyProvider
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

@Singleton
@Component(
    dependencies = [PushContentDependencies::class],
    modules = [PushContentModule::class],
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

    @JvmStatic
    @Provides
    @Singleton
    fun providePushMessageProcessor(
        decryptionService: DecryptionPushContentService,
        notificationBuilder: NotificationBuilder
    ): PushMessageProcessor = DefaultPushMessageProcessor(
        decryptionService = decryptionService,
        notificationBuilder = notificationBuilder
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideCryptoService(): CryptoService = CryptoServiceImpl()

    @JvmStatic
    @Provides
    @Singleton
    fun provideSignatureVerificationService(): SignatureVerificationService = SignatureVerificationServiceImpl()

    @JvmStatic
    @Provides
    @Singleton
    fun provideDecryptionPushContentService(
        pushKeyProvider: PushKeyProvider,
        cryptoService: CryptoService,
        signatureVerificationService: SignatureVerificationService,
        json: Json
    ): DecryptionPushContentService = DecryptionPushContentServiceImpl(
        pushKeyProvider = pushKeyProvider,
        cryptoService = cryptoService,
        signatureVerificationService = signatureVerificationService,
        json = json
    )
}

interface PushContentDependencies : ComponentDependencies {
    fun deviceTokenSavingService(): DeviceTokenStoringService
    fun pushKeyProvider(): PushKeyProvider
    fun context(): Context
    @Named(DEFAULT_APP_COROUTINE_SCOPE) fun scope(): CoroutineScope
    fun dispatchers(): AppCoroutineDispatchers
    fun provider(): StringResourceProvider
    fun notificationBuilder(): NotificationBuilder
    fun json(): Json
    fun authRepository(): AuthRepository
}