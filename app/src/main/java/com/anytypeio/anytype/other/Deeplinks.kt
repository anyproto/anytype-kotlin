package com.anytypeio.anytype.other

import android.net.Uri
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver

const val DEEP_LINK_PATTERN = "anytype://"

const val DEEP_LINK_INVITE_DOMAIN = "invite.any.coop"

const val DEEP_LINK_TO_OBJECT_BASE_URL = "https://object.any.coop"

/**
 * Regex pattern for matching
 */
const val DEEP_LINK_INVITE_REG_EXP = "invite.any.coop/([a-zA-Z0-9]+)#([a-zA-Z0-9]+)"

const val DEE_LINK_INVITE_CUSTOM_REG_EXP = "anytype://invite/\\?cid=([a-zA-Z0-9]+)&key=([a-zA-Z0-9]+)"

const val MAIN_PATH = "main"
const val OBJECT_PATH = "object"
const val IMPORT_PATH = "import"
const val INVITE_PATH = "invite"
const val MEMBERSHIP_PATH = "membership"

const val TYPE_PARAM = "type"
const val OBJECT_ID_PARAM = "objectId"
const val SPACE_ID_PARAM = "spaceId"
const val CONTENT_ID_PARAM = "cid"
const val INVITE_ID_PARAM = "inviteID"
const val ENCRYPTION_KEY_PARAM = "key"
const val SOURCE_PARAM = "source"
const val TYPE_VALUE_EXPERIENCE = "experience"
const val TIER_ID_PARAM = "tier"

const val IMPORT_EXPERIENCE_DEEPLINK = "$DEEP_LINK_PATTERN$MAIN_PATH/$IMPORT_PATH/?$TYPE_PARAM=$TYPE_VALUE_EXPERIENCE"

object DefaultDeepLinkResolver : DeepLinkResolver {

    private val defaultInviteRegex = Regex(DEEP_LINK_INVITE_REG_EXP)

    override fun resolve(
        deeplink: String
    ): DeepLinkResolver.Action = when {
        deeplink.contains(IMPORT_EXPERIENCE_DEEPLINK) -> {
            try {
                val type = Uri.parse(deeplink).getQueryParameter(TYPE_PARAM)
                val source = Uri.parse(deeplink).getQueryParameter(SOURCE_PARAM)
                DeepLinkResolver.Action.Import.Experience(
                    type = type.orEmpty(),
                    source = source.orEmpty()
                )
            } catch (e: Exception) {
                DeepLinkResolver.Action.Unknown
            }
        }
        deeplink.contains(INVITE_PATH) -> {
            DeepLinkResolver.Action.Invite(deeplink)
        }
        defaultInviteRegex.containsMatchIn(deeplink) -> {
            DeepLinkResolver.Action.Invite(deeplink)
        }
        deeplink.contains(OBJECT_PATH) -> {
            val uri = Uri.parse(deeplink)
            val obj = uri.getQueryParameter(OBJECT_ID_PARAM)
            val space = uri.getQueryParameter(SPACE_ID_PARAM)
            if (!obj.isNullOrEmpty() && !space.isNullOrEmpty()) {
                val cid = uri.getQueryParameter(CONTENT_ID_PARAM)
                val key = uri.getQueryParameter(ENCRYPTION_KEY_PARAM)
                DeepLinkResolver.Action.DeepLinkToObject(
                    obj = obj,
                    space = SpaceId(space),
                    invite = if (!cid.isNullOrEmpty() && !key.isNullOrEmpty()) {
                        DeepLinkResolver.Action.DeepLinkToObject.Invite(
                            cid = cid,
                            key = key
                        )
                    } else {
                        null
                    }
                )
            } else {
                DeepLinkResolver.Action.Unknown
            }
        }
        deeplink.contains(MEMBERSHIP_PATH) -> {
            val uri = Uri.parse(deeplink)
            DeepLinkResolver.Action.DeepLinkToMembership(
                tierId = uri.getQueryParameter(TIER_ID_PARAM)
            )
        }
        else -> DeepLinkResolver.Action.Unknown
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
        return "${DEEP_LINK_TO_OBJECT_BASE_URL}/$obj?${SPACE_ID_PARAM}=${space.id}&${INVITE_ID_PARAM}=$invite#$encryptionKey"
    }

    override fun isDeepLink(link: String): Boolean {
        return link.contains(defaultInviteRegex) || link.contains(DEEP_LINK_PATTERN)
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