package com.anytypeio.anytype.core_models.misc

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts

/**
 * @param [attachmentTarget] optional target, to which the object will be attached
 */
fun ObjectWrapper.Basic.navigation(
    effect: OpenObjectNavigation.SideEffect = OpenObjectNavigation.SideEffect.None,
    openBookmarkAsObject: Boolean = false,
): OpenObjectNavigation {
    if (!isValid) return OpenObjectNavigation.NonValidObject
    return when (layout) {
        ObjectType.Layout.BOOKMARK -> {
            if (openBookmarkAsObject) {
                OpenObjectNavigation.OpenEditor(
                    target = id,
                    space = requireNotNull(spaceId),
                    effect = effect
                )
            } else {
                val url = getValue<String>(Relations.SOURCE)
                if (url.isNullOrEmpty()) {
                    OpenObjectNavigation.OpenEditor(
                        target = id,
                        space = requireNotNull(spaceId),
                        effect = effect
                    )
                } else {
                    OpenObjectNavigation.OpenBookmarkUrl(url)
                }
            }
        }

        ObjectType.Layout.BASIC,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.TODO -> {
            OpenObjectNavigation.OpenEditor(
                target = id,
                space = requireNotNull(spaceId),
                effect = effect
            )
        }

        in SupportedLayouts.fileLayouts -> {
            OpenObjectNavigation.OpenEditor(
                target = id,
                space = requireNotNull(spaceId),
                effect = effect
            )
        }

        ObjectType.Layout.PROFILE -> {
            val identityLink = getValue<Id>(Relations.IDENTITY_PROFILE_LINK)
            if (identityLink.isNullOrEmpty()) {
                OpenObjectNavigation.OpenEditor(
                    target = id,
                    space = requireNotNull(spaceId),
                    effect = effect
                )
            } else {
                OpenObjectNavigation.OpenEditor(
                    target = identityLink,
                    space = requireNotNull(spaceId),
                    effect = effect
                )
            }
        }

        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION -> {
            OpenObjectNavigation.OpenDataView(
                target = id,
                space = requireNotNull(spaceId),
                effect = effect
            )
        }

        ObjectType.Layout.CHAT,
        ObjectType.Layout.CHAT_DERIVED -> {
            OpenObjectNavigation.OpenChat(
                target = id,
                space = requireNotNull(spaceId)
            )
        }

        ObjectType.Layout.DATE -> {
            OpenObjectNavigation.OpenDateObject(
                target = id,
                space = requireNotNull(spaceId)
            )
        }

        ObjectType.Layout.PARTICIPANT -> {
            OpenObjectNavigation.OpenParticipant(
                target = id,
                space = requireNotNull(spaceId)
            )
        }

        ObjectType.Layout.OBJECT_TYPE -> {
            OpenObjectNavigation.OpenType(
                target = id,
                space = requireNotNull(spaceId)
            )
        }

        else -> {
            OpenObjectNavigation.UnexpectedLayoutError(layout)
        }
    }
}