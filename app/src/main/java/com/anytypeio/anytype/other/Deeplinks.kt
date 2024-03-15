package com.anytypeio.anytype.other

import android.net.Uri
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import timber.log.Timber

const val DEEP_LINK_PATTERN = "anytype://"

const val MAIN_PATH = "main"
const val IMPORT_PATH = "import"
const val INVITE_PATH = "invite"

const val TYPE_PARAM = "type"
const val SOURCE_PARAM = "source"
const val TYPE_VALUE_EXPERIENCE = "experience"

const val IMPORT_EXPERIENCE_DEEPLINK =
    "$DEEP_LINK_PATTERN$MAIN_PATH/$IMPORT_PATH/?$TYPE_PARAM=$TYPE_VALUE_EXPERIENCE"

object DefaultDeepLinkResolver : DeepLinkResolver {
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
                Timber.e(e, "Error while resolving deeplink: $deeplink")
                DeepLinkResolver.Action.Unknown
            }
        }

        deeplink.contains(INVITE_PATH) -> {
            DeepLinkResolver.Action.Invite(deeplink)
        }

        else -> DeepLinkResolver.Action.Unknown
    }.also { Timber.d("Resolved deeplink: $deeplink to action: $it") }
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