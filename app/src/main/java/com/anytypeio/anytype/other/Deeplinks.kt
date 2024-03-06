package com.anytypeio.anytype.other

import android.net.Uri
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver

const val DEEP_LINK_PATTERN = "anytype://"

const val MAIN_PATH = "main"
const val IMPORT_PATH = "import"
const val INVITE_PATH = "invite"

const val TYPE_PARAM = "type"
const val TYPE_VALUE_EXPERIENCE = "experience"

const val IMPORT_EXPERIENCE_DEEPLINK = "$DEEP_LINK_PATTERN$MAIN_PATH/$IMPORT_PATH?$TYPE_PARAM=$TYPE_VALUE_EXPERIENCE"

object DefaultDeepLinkResolver : DeepLinkResolver {
    override fun resolve(
        deeplink: String
    ): DeepLinkResolver.Action = when {
        deeplink.contains(IMPORT_EXPERIENCE_DEEPLINK) -> {
            DeepLinkResolver.Action.Import.Experience
        }
        deeplink.contains(INVITE_PATH) -> {
            DeepLinkResolver.Action.Invite(deeplink)
        }
        else -> DeepLinkResolver.Action.Unknown
    }
}

object DefaultSpaceInviteResolver : SpaceInviteResolver {

    override fun parseContentId(link: String): Id? {
        val uri = Uri.parse(link)
        return uri.getQueryParameter(CONTENT_ID_KEY)
    }

    override fun parseFileKey(link: String): Id? {
        val uri = Uri.parse(link)
        return uri.getQueryParameter(FILE_KEY_KEY)
    }

    private const val CONTENT_ID_KEY = "cid"
    private const val FILE_KEY_KEY = "key"
}