package com.anytypeio.anytype.device

import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.notifications.RegisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class AnytypePushService : FirebaseMessagingService() {

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    @Inject
    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var registerDeviceToken: RegisterDeviceTokenUseCase

    @Inject
    lateinit var deviceTokenStore: DeviceTokenStore

    @Inject
    lateinit var awaitAccountStartManager: AwaitAccountStartManager

    init {
        Timber.d("AnytypePushService initialized")
    }

    override fun onCreate() {
        (application as AndroidApplication).componentManager.pushContentComponent.get().inject(this)
        super.onCreate()
        startTokenRegistration()
    }

    private fun startTokenRegistration() {
        scope.launch {
            observeTokenRegistration().collect {
                Timber.d("Token registration started")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New token received: $token")
        deviceTokenStore.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTokenRegistration(): Flow<Unit> {
        return awaitAccountStartManager
            .state()
            .flatMapLatest { state ->
                if (state is AwaitAccountStartManager.State.Started) {
                    deviceTokenStore.getToken()?.let { token ->
                        scope.launch(dispatchers.io) {
                            val params = RegisterDeviceTokenUseCase.Params(token = token)
                            registerDeviceToken.async(params).fold(
                                onSuccess = {
                                    Timber.d("Successfully registered token: $token")
                                },
                                onFailure = { error ->
                                    Timber.w("Failed to register token: $token, error: $error")
                                }
                            )
                        }
                    }
                }
                emptyFlow()
            }
    }
}