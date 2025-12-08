package com.anytypeio.anytype.other

import android.net.Uri
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import timber.log.Timber

const val DEEP_LINK_PATTERN = "anytype://"

const val DEEP_LINK_INVITE_DOMAIN = "invite.any.coop"

const val DEEP_LINK_TO_OBJECT_BASE_URL = "https://object.any.coop"

/**
 * Regex pattern for matching
 */
const val DEEP_LINK_INVITE_REG_EXP = "invite.any.coop/([a-zA-Z0-9]+)#([a-zA-Z0-9]+)"
const val DEEP_LINK_TO_OBJECT_REG_EXP = """object\.any\.coop/([a-zA-Z0-9?=&._-]+)"""
const val DEEP_LINK_ONE_TO_ONE_CHAT_REG_EXP = """hi\.any\.coop/([a-zA-Z0-9]+)#([a-zA-Z0-9]+)"""
const val DEEP_LINK_ONE_TO_ONE_CHAT_CUSTOM_REG_EXP =
    "anytype://hi/\\?id=([a-zA-Z0-9]+)&key=([a-zA-Z0-9]+)"

const val DEE_LINK_INVITE_CUSTOM_REG_EXP = "anytype://invite/\\?cid=([a-zA-Z0-9]+)&key=([a-zA-Z0-9]+)"

const val MAIN_PATH = "main"
const val OBJECT_PATH = "object"
const val IMPORT_PATH = "import"
const val MEMBERSHIP_PATH = "membership"

const val TYPE_PARAM = "type"
const val OBJECT_ID_PARAM = "objectId"
const val SPACE_ID_PARAM = "spaceId"
const val INVITE_ID_HTTPS_PARAM = "inviteId"
const val SOURCE_PARAM = "source"
const val TYPE_VALUE_EXPERIENCE = "experience"
const val TIER_ID_PARAM = "tier"

const val IMPORT_EXPERIENCE_DEEPLINK = "$DEEP_LINK_PATTERN$MAIN_PATH/$IMPORT_PATH/?$TYPE_PARAM=$TYPE_VALUE_EXPERIENCE"

object DefaultDeepLinkResolver : DeepLinkResolver {

    private val defaultInviteRegex = Regex(DEEP_LINK_INVITE_REG_EXP)
    private val defaultLinkToObjectRegex = Regex(DEEP_LINK_TO_OBJECT_REG_EXP)
    private val customInviteRegex = Regex(DEE_LINK_INVITE_CUSTOM_REG_EXP)
    private val oneToOneChatRegex = Regex(DEEP_LINK_ONE_TO_ONE_CHAT_REG_EXP)
    private val oneToOneChatCustomRegex = Regex(DEEP_LINK_ONE_TO_ONE_CHAT_CUSTOM_REG_EXP)

    override fun resolve(deeplink: String): DeepLinkResolver.Action {
        val uri = Uri.parse(deeplink)
        return when {
            deeplink.contains(IMPORT_EXPERIENCE_DEEPLINK) -> resolveImportExperience(uri)
            oneToOneChatRegex.containsMatchIn(deeplink) -> resolveOneToOneChatLink(deeplink)
            oneToOneChatCustomRegex.containsMatchIn(deeplink) -> resolveOneToOneChatCustomLink(uri)
            defaultInviteRegex.containsMatchIn(deeplink) -> DeepLinkResolver.Action.Invite(deeplink)
            customInviteRegex.containsMatchIn(deeplink) ->  DeepLinkResolver.Action.Invite(deeplink)
            defaultLinkToObjectRegex.containsMatchIn(deeplink) -> resolveDeepLinkToObject(uri)
            deeplink.contains(OBJECT_PATH) -> resolveObjectPath(uri)
            deeplink.contains(MEMBERSHIP_PATH) -> resolveMembershipPath(uri)
            else -> DeepLinkResolver.Action.Unknown
        }.also {
            Timber.d("Resolving deep link: $deeplink")
        }
    }

    private fun resolveOneToOneChatLink(deeplink: String): DeepLinkResolver.Action {
        val result = oneToOneChatRegex.find(deeplink)
        val identity = result?.groupValues?.getOrNull(1)
        val metadataKey = result?.groupValues?.getOrNull(2)
        return if (identity != null && metadataKey != null) {
            DeepLinkResolver.Action.InitiateOneToOneChat(
                identity = identity,
                metadataKey = metadataKey
            )
        } else {
            DeepLinkResolver.Action.Unknown
        }
    }

    private fun resolveOneToOneChatCustomLink(uri: Uri): DeepLinkResolver.Action {
        val identity = uri.getQueryParameter("id")
        val metadataKey = uri.getQueryParameter("key")
        return if (identity != null && metadataKey != null) {
            DeepLinkResolver.Action.InitiateOneToOneChat(
                identity = identity,
                metadataKey = metadataKey
            )
        } else {
            DeepLinkResolver.Action.Unknown
        }
    }

    private fun resolveImportExperience(uri: Uri): DeepLinkResolver.Action {
        return try {
            val type = uri.getQueryParameter(TYPE_PARAM).orEmpty()
            val source = uri.getQueryParameter(SOURCE_PARAM).orEmpty()
            DeepLinkResolver.Action.Import.Experience(type, source)
        } catch (e: Exception) {
            DeepLinkResolver.Action.Unknown
        }
    }

    private fun resolveDeepLinkToObject(uri: Uri): DeepLinkResolver.Action {
        val obj = uri.pathSegments.getOrNull(0) ?: return DeepLinkResolver.Action.Unknown
        val space = uri.getQueryParameter(SPACE_ID_PARAM)?.takeIf { it.isNotEmpty() }
            ?: return DeepLinkResolver.Action.Unknown // Ensure spaceId is required

        return DeepLinkResolver.Action.DeepLinkToObject(
            obj = obj,
            space = SpaceId(space),
            invite = parseInvite(uri)
        )
    }

    private fun resolveObjectPath(uri: Uri): DeepLinkResolver.Action {
        val obj = uri.getQueryParameter(OBJECT_ID_PARAM)?.takeIf { it.isNotEmpty() }
        val space = uri.getQueryParameter(SPACE_ID_PARAM)?.takeIf { it.isNotEmpty() }
            ?: return DeepLinkResolver.Action.Unknown // Ensure spaceId is required

        val key = uri.getQueryParameter(DefaultSpaceInviteResolver.FILE_KEY_KEY)
        val cid = uri.getQueryParameter(DefaultSpaceInviteResolver.CONTENT_ID_KEY)

        return if (obj != null) {
            DeepLinkResolver.Action.DeepLinkToObject(
                obj = obj,
                space = SpaceId(space),
                invite = if (!key.isNullOrEmpty() && !cid.isNullOrEmpty()) {
                    DeepLinkResolver.Action.DeepLinkToObject.Invite(
                        key = key,
                        cid = cid
                    )
                } else {
                    null
                }
            )
        } else {
            DeepLinkResolver.Action.Unknown
        }
    }

    private fun resolveMembershipPath(uri: Uri): DeepLinkResolver.Action {
        return DeepLinkResolver.Action.DeepLinkToMembership(
            tierId = uri.getQueryParameter(TIER_ID_PARAM)
        )
    }

    private fun parseInvite(uri: Uri): DeepLinkResolver.Action.DeepLinkToObject.Invite? {
        val inviteId = uri.getQueryParameter(INVITE_ID_HTTPS_PARAM)?.takeIf { it.isNotEmpty() }
        val encryption = uri.fragment?.takeIf { it.isNotEmpty() }
        return if (inviteId != null && encryption != null) {
            DeepLinkResolver.Action.DeepLinkToObject.Invite(
                key = encryption,
                cid = inviteId
            )
        } else {
            null
        }
    }

    override fun createObjectDeepLink(obj: Id, space: SpaceId): Url {
        return "$DEEP_LINK_TO_OBJECT_BASE_URL/$obj?$SPACE_ID_PARAM=${space.id}"
    }

    override fun createObjectDeepLinkWithInvite(
        obj: Id,
        space: SpaceId,
        invite: Id,
        encryptionKey: String
    ): Url {
        return "${DEEP_LINK_TO_OBJECT_BASE_URL}/$obj?${SPACE_ID_PARAM}=${space.id}&${INVITE_ID_HTTPS_PARAM}=$invite#$encryptionKey"
    }

    override fun isDeepLink(link: String): Boolean {
        return link.contains(defaultInviteRegex)
                || link.contains(defaultLinkToObjectRegex)
                || link.contains(oneToOneChatRegex)
                || link.contains(oneToOneChatCustomRegex)
                || link.contains(DEEP_LINK_PATTERN)
    }
}

object DefaultSpaceInviteResolver : SpaceInviteResolver {

    private val customRegex = Regex(DEE_LINK_INVITE_CUSTOM_REG_EXP)
    private val defaultRegex = Regex(DEEP_LINK_INVITE_REG_EXP)

    override fun parseContentId(link: String): Id? {
        return if (link.matches(customRegex)) {
            val uri = Uri.parse(link)
            uri.getQueryParameter(CONTENT_ID_KEY)
        } else {
            val result = defaultRegex.find(link)
            result?.groupValues?.getOrNull(CONTENT_INDEX)
        }
    }

    override fun parseFileKey(link: String): Id? {
        return if (link.matches(customRegex)) {
            val uri = Uri.parse(link)
            uri.getQueryParameter(FILE_KEY_KEY)
        } else {
            val result = defaultRegex.find(link)
            result?.groupValues?.getOrNull(KEY_INDEX)
        }
    }

    override fun createInviteLink(contentId: String, encryptionKey: String) : String {
        return "https://$DEEP_LINK_INVITE_DOMAIN/$contentId#$encryptionKey"
    }


    private const val CONTENT_INDEX = 1
    private const val KEY_INDEX = 2
    const val CONTENT_ID_KEY = "cid"
    const val FILE_KEY_KEY = "key"
}