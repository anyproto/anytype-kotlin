package com.anytypeio.anytype.other

import android.net.Uri
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver

const val DEEP_LINK_PATTERN = "anytype://"

/**
 * Regex pattern for matching
 */
const val DEEP_LINK_INVITE_REG_EXP = "invite.any.coop/([a-zA-Z0-9]+)#([a-zA-Z0-9]+)"

const val DEE_LINK_INVITE_CUSTOM_REG_EXP = "anytype://invite/\\?cid=([a-zA-Z0-9]+)&key=([a-zA-Z0-9]+)"

const val MAIN_PATH = "main"
const val OBJECT_PATH = "object"
const val IMPORT_PATH = "import"
const val INVITE_PATH = "invite"

const val TYPE_PARAM = "type"
const val OBJECT_ID_PARAM = "objectId"
const val SPACE_ID_PARAM = "spaceId"
const val SOURCE_PARAM = "source"
const val TYPE_VALUE_EXPERIENCE = "experience"

const val IMPORT_EXPERIENCE_DEEPLINK = "$DEEP_LINK_PATTERN$MAIN_PATH/$IMPORT_PATH/?$TYPE_PARAM=$TYPE_VALUE_EXPERIENCE"

object DefaultDeepLinkResolver : DeepLinkResolver {

    private val regex = Regex(DEEP_LINK_INVITE_REG_EXP)

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
        regex.containsMatchIn(deeplink) -> {
            DeepLinkResolver.Action.Invite(deeplink)
        }
        deeplink.contains(OBJECT_PATH) -> {
            val uri = Uri.parse(deeplink)
            val obj = uri.getQueryParameter(OBJECT_ID_PARAM)
            val space = uri.getQueryParameter(SPACE_ID_PARAM)
            if (!obj.isNullOrEmpty() && !space.isNullOrEmpty()) {
                DeepLinkResolver.Action.DeepLinkToObject(
                    obj = obj,
                    space = SpaceId(space)
                )
            } else {
                DeepLinkResolver.Action.Unknown
            }
        }
        else -> DeepLinkResolver.Action.Unknown
    }

    override fun createObjectDeepLink(obj: Id, space: SpaceId): Url {
        return "${DEEP_LINK_PATTERN}${OBJECT_PATH}?${OBJECT_ID_PARAM}=$obj&${SPACE_ID_PARAM}=${space.id}"
    }

    override fun isDeepLink(link: String): Boolean {
        return link.contains(regex) || link.contains(DEEP_LINK_PATTERN)
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

    private const val CONTENT_INDEX = 1
    private const val KEY_INDEX = 2
    private const val CONTENT_ID_KEY = "cid"
    private const val FILE_KEY_KEY = "key"
}