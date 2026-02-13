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

const val DEEP_LINK_ONE_TO_ONE_CHAT_DOMAIN = "hi.any.coop"

const val DEEP_LINK_TO_OBJECT_BASE_URL = "https://object.any.coop"

/**
 * Regex pattern for matching
 */
const val DEEP_LINK_INVITE_REG_EXP = "invite.any.coop/([a-zA-Z0-9]+)#([a-zA-Z0-9]+)"
const val DEEP_LINK_TO_OBJECT_REG_EXP = """object\.any\.coop/([a-zA-Z0-9?=&._-]+)"""
const val DEEP_LINK_ONE_TO_ONE_CHAT_CUSTOM_REG_EXP =
    "anytype://hi/\\?id=([a-zA-Z0-9_-]+)&key=([a-zA-Z0-9_+/=-]+)"

const val DEE_LINK_INVITE_CUSTOM_REG_EXP = "anytype://invite/\\?cid=([a-zA-Z0-9]+)&key=([a-zA-Z0-9]+)"

const val MAIN_PATH = "main"
const val OBJECT_PATH = "object"
const val IMPORT_PATH = "import"
const val MEMBERSHIP_PATH = "membership"
const val OS_WIDGET_HOST = "os-widget"
const val OS_WIDGET_SPACES_LIST = "spaces-list"
const val OS_WIDGET_CREATE_OBJECT = "create-object"
const val OS_WIDGET_SPACE_SHORTCUT = "space-shortcut"
const val OS_WIDGET_OBJECT_SHORTCUT = "object-shortcut"
const val OS_WIDGET_ACTION_OPEN_SPACE = "open-space"
const val OS_WIDGET_ACTION_OPEN = "open"
const val OS_WIDGET_ACTION_CREATE = "create"

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
    private val oneToOneChatCustomRegex = Regex(DEEP_LINK_ONE_TO_ONE_CHAT_CUSTOM_REG_EXP)

    override fun resolve(deeplink: String): DeepLinkResolver.Action {
        val uri = Uri.parse(deeplink)
        return when {
            deeplink.contains(IMPORT_EXPERIENCE_DEEPLINK) -> resolveImportExperience(uri)
            uri.host == DEEP_LINK_ONE_TO_ONE_CHAT_DOMAIN -> resolveOneToOneChatLink(deeplink)
            oneToOneChatCustomRegex.containsMatchIn(deeplink) -> resolveOneToOneChatCustomLink(uri)
            defaultInviteRegex.containsMatchIn(deeplink) -> DeepLinkResolver.Action.Invite(deeplink)
            customInviteRegex.containsMatchIn(deeplink) ->  DeepLinkResolver.Action.Invite(deeplink)
            defaultLinkToObjectRegex.containsMatchIn(deeplink) -> resolveDeepLinkToObject(uri)
            // Check OS widget host BEFORE generic path checks to avoid false matches
            // (e.g., "create-object" contains "object" which would match OBJECT_PATH)
            uri.host == OS_WIDGET_HOST -> resolveOsWidgetDeepLink(uri)
            deeplink.contains(OBJECT_PATH) -> resolveObjectPath(uri)
            deeplink.contains(MEMBERSHIP_PATH) -> resolveMembershipPath(uri)
            else -> DeepLinkResolver.Action.Unknown
        }.also {
            Timber.d("Resolving deep link: $deeplink, result: $it")
        }
    }

    private fun resolveOneToOneChatLink(deeplink: String): DeepLinkResolver.Action {
        val uri = Uri.parse(deeplink)
        // Check if it's a hi.any.coop URL
        if (uri.host != DEEP_LINK_ONE_TO_ONE_CHAT_DOMAIN) return DeepLinkResolver.Action.Unknown

        val identity = uri.pathSegments.firstOrNull()
        // Use explicit URL decoding on the encoded fragment to ensure proper decoding
        // of percent-encoded base64 characters like %2B (+), %2F (/), %3D (=)
        // Use Uri.decode() instead of URLDecoder.decode() because:
        // - URLDecoder converts '+' to space (for query strings)
        // - Uri.decode() only decodes %XX sequences, preserving '+' for base64
        // This handles both iOS (literal '+') and Desktop ('%2B') QR codes
        val metadataKey = uri.encodedFragment?.let { encoded ->
            Uri.decode(encoded)
        }

        Timber.d("OneToOne deeplink: identity=$identity, encodedFragment=${uri.encodedFragment}, decodedKey=$metadataKey")

        return if (!identity.isNullOrEmpty() && !metadataKey.isNullOrEmpty()) {
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

    /**
     * Resolves anytype://os-widget/{widget-type}/{action}/{params} deep links.
     * Used by OS home screen widgets.
     */
    private fun resolveOsWidgetDeepLink(uri: Uri): DeepLinkResolver.Action {
        val widgetType = uri.pathSegments.getOrNull(0)
        val action = uri.pathSegments.getOrNull(1)
        return when (widgetType) {
            OS_WIDGET_SPACES_LIST -> resolveSpacesListWidgetAction(uri, action)
            OS_WIDGET_CREATE_OBJECT -> resolveCreateObjectWidgetAction(uri, action)
            OS_WIDGET_SPACE_SHORTCUT -> resolveSpaceShortcutWidgetAction(uri, action)
            OS_WIDGET_OBJECT_SHORTCUT -> resolveObjectShortcutWidgetAction(uri, action)
            else -> DeepLinkResolver.Action.Unknown
        }
    }

    private fun resolveSpacesListWidgetAction(
        uri: Uri,
        action: String?
    ): DeepLinkResolver.Action {
        return when (action) {
            OS_WIDGET_ACTION_OPEN_SPACE -> {
                val spaceId = uri.pathSegments.getOrNull(2)
                if (!spaceId.isNullOrEmpty()) {
                    DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToSpace(SpaceId(spaceId))
                } else {
                    DeepLinkResolver.Action.Unknown
                }
            }
            else -> DeepLinkResolver.Action.Unknown
        }
    }

    private fun resolveCreateObjectWidgetAction(
        uri: Uri,
        action: String?
    ): DeepLinkResolver.Action {
        Timber.d("resolveCreateObjectWidgetAction: uri=$uri, action=$action")
        return when (action) {
            OS_WIDGET_ACTION_CREATE -> {
                val appWidgetId = uri.pathSegments.getOrNull(2)?.toIntOrNull()
                Timber.d("resolveCreateObjectWidgetAction: parsed appWidgetId=$appWidgetId")
                if (appWidgetId != null) {
                    DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToCreateObject(appWidgetId)
                } else {
                    Timber.w("resolveCreateObjectWidgetAction: failed to parse appWidgetId from uri=$uri")
                    DeepLinkResolver.Action.Unknown
                }
            }
            else -> {
                Timber.w("resolveCreateObjectWidgetAction: unknown action=$action")
                DeepLinkResolver.Action.Unknown
            }
        }
    }

    /**
     * Resolves space shortcut widget deep links.
     * Format: anytype://os-widget/space-shortcut/open/{spaceId}
     */
    private fun resolveSpaceShortcutWidgetAction(
        uri: Uri,
        action: String?
    ): DeepLinkResolver.Action {
        return when (action) {
            OS_WIDGET_ACTION_OPEN -> {
                val spaceId = uri.pathSegments.getOrNull(2)
                if (!spaceId.isNullOrEmpty()) {
                    DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToSpace(SpaceId(spaceId))
                } else {
                    DeepLinkResolver.Action.Unknown
                }
            }
            else -> DeepLinkResolver.Action.Unknown
        }
    }

    /**
     * Resolves object shortcut widget deep links.
     * Format: anytype://os-widget/object-shortcut/open/{objectId}?spaceId={spaceId}
     */
    private fun resolveObjectShortcutWidgetAction(
        uri: Uri,
        action: String?
    ): DeepLinkResolver.Action {
        return when (action) {
            OS_WIDGET_ACTION_OPEN -> {
                val objectId = uri.pathSegments.getOrNull(2)
                val spaceId = uri.getQueryParameter(SPACE_ID_PARAM)
                if (!objectId.isNullOrEmpty() && !spaceId.isNullOrEmpty()) {
                    DeepLinkResolver.Action.DeepLinkToObject(
                        obj = objectId,
                        space = SpaceId(spaceId),
                        invite = null
                    )
                } else {
                    DeepLinkResolver.Action.Unknown
                }
            }
            else -> DeepLinkResolver.Action.Unknown
        }
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
        val uri = Uri.parse(link)
        return link.contains(defaultInviteRegex)
                || link.contains(defaultLinkToObjectRegex)
                || uri.host == DEEP_LINK_ONE_TO_ONE_CHAT_DOMAIN
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