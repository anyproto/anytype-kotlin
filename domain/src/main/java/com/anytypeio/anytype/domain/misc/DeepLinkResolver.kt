package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface DeepLinkResolver {

    fun resolve(deeplink: String) : Action

    fun createObjectDeepLink(obj: Id, space: SpaceId) : Url

    fun createObjectDeepLinkWithInvite(
        obj: Id,
        space: SpaceId,
        invite: Id,
        encryptionKey: String
    ) : Url

    fun isDeepLink(link: String) : Boolean

    sealed class Action {
        data object Unknown : Action()
        sealed class Import : Action() {
            data class Experience(val type: String, val source: String) : Action()
        }
        data class Invite(val link: String) : Action()
        data class DeepLinkToObject(
            val obj: Id,
            val space: SpaceId,
            val invite: Invite? = null
        ) : Action() {
            data class Invite(
                val cid: String,
                val key: String
            )
        }
        data class DeepLinkToMembership(
            val tierId: String?
        ) : Action()

        data class InitiateOneToOneChat(
            val identity: Id,
            val metadataKey: String
        ) : Action()

        /**
         * Deep links triggered by OS home screen widgets.
         */
        sealed class OsWidgetDeepLink : Action() {
            /**
             * Deep link to open a specific space.
             */
            data class DeepLinkToSpace(
                val space: SpaceId
            ) : OsWidgetDeepLink()

            /**
             * Deep link to create an object using a pre-configured widget.
             */
            data class DeepLinkToCreateObject(
                val appWidgetId: Int
            ) : OsWidgetDeepLink()
        }
    }
}
