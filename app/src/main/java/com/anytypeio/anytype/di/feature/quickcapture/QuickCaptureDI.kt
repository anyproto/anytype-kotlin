package com.anytypeio.anytype.di.feature.quickcapture

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.ai.TypeSuggestionEngine
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.ui.quickcapture.QuickCaptureFragment
import dagger.Component
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import com.anytypeio.anytype.di.main.ConfigModule

@Component(
    dependencies = [QuickCaptureDependencies::class]
)
@PerScreen
interface QuickCaptureComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: QuickCaptureDependencies): QuickCaptureComponent
    }

    fun inject(fragment: QuickCaptureFragment)
}

interface QuickCaptureDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun userSettingsRepository(): UserSettingsRepository
    fun spaceManager(): SpaceManager
    fun userPermissionProvider(): UserPermissionProvider
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun provideStorelessSubscriptionContainer(): StorelessSubscriptionContainer
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun spaceSyncAndP2PStatusProvider(): SpaceSyncAndP2PStatusProvider
    fun typeSuggestionEngine(): TypeSuggestionEngine
    fun participantSubscriptionContainer(): ParticipantSubscriptionContainer
    fun authRepository(): AuthRepository
    @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE)
    fun applicationCoroutineScope(): CoroutineScope
    fun logger(): Logger
}
