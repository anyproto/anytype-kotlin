package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Generates a deep link for initiating a 1-1 chat with the current user.
 * The link format is: https://hi.any.coop/{identity}#{metaDataKey}
 */
class GenerateOneToOneChatLink @Inject constructor(
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GenerateOneToOneChatLink.Params, OneToOneLinkData>(dispatchers.io) {

    data class Params(
        val identity: Id,
        val metaDataKey: String,
        val name: String
    )

    override suspend fun doWork(params: Params): OneToOneLinkData {
        if (params.metaDataKey.isBlank()) {
            throw IllegalStateException("MetaData key not available")
        }
        if (params.identity.isBlank()) {
            throw IllegalStateException("Identity not available")
        }

        return OneToOneLinkData(
            link = "$BASE_URL${params.identity}#${params.metaDataKey}",
            name = params.name,
            identity = params.identity
        )
    }

    companion object {
        const val BASE_URL = "https://hi.any.coop/"
    }
}

/**
 * Data class containing the generated 1-1 chat link and associated profile information.
 *
 * @property link The generated deep link URL
 * @property name The current user's profile name
 * @property identity The current user's identity ID
 */
data class OneToOneLinkData(
    val link: String,
    val name: String,
    val identity: Id
)
