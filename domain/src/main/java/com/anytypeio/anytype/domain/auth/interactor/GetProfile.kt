package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext

/** Use case for getting currently selected user account.
 */
 class GetProfile @Inject constructor(
    private val provider: ConfigStorage,
    private val repo: BlockRepository,
    private val channel: SubscriptionEventChannel,
    private val dispatchers: AppCoroutineDispatchers
) : BaseUseCase<ObjectWrapper.Basic, GetProfile.Params>() {

    fun subscribe(subscription: Id) = channel.subscribe(subscriptions = listOf(subscription))

    suspend fun unsubscribe(subscription: Id) = withContext(dispatchers.io) {
        repo.cancelObjectSearchSubscription(
            subscriptions = listOf(subscription)
        )
    }

    fun observe(
        subscription: Id,
        keys: List<String>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Flow<ObjectWrapper.Basic> {

        return flow {
            val profile = getProfile(subscription, keys)
            emitAll(
                channel.subscribe(subscriptions = listOf(subscription)).scan(profile) { prev, payload ->
                    var result = prev
                    payload.forEach { event ->
                        result = when (event) {
                            is SubscriptionEvent.Amend -> {
                                result.amend(event.diff)
                            }
                            is SubscriptionEvent.Set -> {
                                ObjectWrapper.Basic(event.data)
                            }
                            is SubscriptionEvent.Unset -> {
                                result.unset(event.keys)
                            }
                            else -> {
                                result
                            }
                        }
                    }
                    result
                }
            )
        }.flowOn(dispatcher)
    }

    private suspend fun getProfile(
        subscription: Id,
        keys: List<String>
    ): ObjectWrapper.Basic {
        val config = provider.get()
        val result = repo.searchObjectsByIdWithSubscription(
            space = SpaceId(config.techSpace),
            subscription = subscription,
            ids = listOf(config.profile),
            keys = keys
        )
        val profile = result.results.first { obj ->
            obj.id == config.profile
        }
        return profile
    }

    @Deprecated("Should not be used. Will be changed.")
    override suspend fun run(params: Params) = safe {
        val config = provider.get()
        val result = repo.searchObjectsByIdWithSubscription(
            space = SpaceId(config.techSpace),
            subscription = params.subscription,
            ids = listOf(config.profile),
            keys = params.keys
        )
        result.results.find { obj ->
            obj.id == config.profile
        } ?: throw Exception("Profile not found")
    }

    class Params(
        val subscription: Id,
        val keys: List<String>
    )
}