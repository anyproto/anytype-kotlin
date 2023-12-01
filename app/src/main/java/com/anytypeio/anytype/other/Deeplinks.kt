package com.anytypeio.anytype.other

import com.anytypeio.anytype.domain.misc.DeepLinkResolver

const val DEEP_LINK_PATTERN = "anytype://"

const val MAIN_PATH = "main"
const val IMPORT_PATH = "import"

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
        else -> DeepLinkResolver.Action.Unknown
    }
}