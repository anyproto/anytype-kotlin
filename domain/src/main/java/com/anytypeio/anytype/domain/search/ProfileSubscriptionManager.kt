package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscription
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSubscriptionManager @Inject constructor(
    private val scope: CoroutineScope,
    private val configStorage: ConfigStorage,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val container: StorelessSubscriptionContainer,
    private val dispatchers: AppCoroutineDispatchers
) : GlobalSubscription {

    private val state = MutableStateFlow<ObjectWrapper.Basic?>(null)

    fun onStart() {
        scope.launch {
            awaitAccountStartManager
                .state()
                .mapNotNull { configStorage.getOrNull() }
                .flatMapLatest { config ->
                    container.subscribe(
                        searchParams = StoreSearchByIdsParams(
                            subscription = GLOBAL_PROFILE_SUBSCRIPTION,
                            space = SpaceId(config.techSpace),
                            keys = listOf(
                                Relations.ID,
                                Relations.NAME,
                                Relations.ICON_EMOJI,
                                Relations.ICON_IMAGE,
                                Relations.ICON_OPTION,
                                Relations.SHARED_SPACES_LIMIT
                            ),
                            targets = listOf(config.profile)
                        )
                    ).map {
                        it.firstOrNull()
                    }
                }.collect {
                    state.value = it
                }
        }
    }

    companion object {
        const val GLOBAL_PROFILE_SUBSCRIPTION = "subscription.global.profile"
    }
}