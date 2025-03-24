package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon.Basic
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

fun ObjectWrapper.Basic.objectIcon(
    builder: UrlBuilder,
    objType: ObjectWrapper.Type?
): ObjectIcon {

    val obj = this

    if (obj.isDeleted == true) {
        return ObjectIcon.Deleted
    }

    val objImage = obj.iconImage
    val objEmoji = obj.iconEmoji
    val objName = obj.name.orEmpty()

    val objTypeIcon = objType?.objectIcon() ?: ObjectIcon.TypeIcon.Default.DEFAULT

    return when (obj.layout) {
        ObjectType.Layout.OBJECT_TYPE -> {
            val asType = ObjectWrapper.Type(map = obj.map)
            asType.objectIcon()
        }

        ObjectType.Layout.BASIC,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION -> {
            when {
                !objImage.isNullOrBlank() -> Basic.Image(
                    hash = builder.thumbnail(objImage),
                    fallback = objType?.objectFallbackIcon()
                        ?: ObjectIcon.TypeIcon.Fallback.DEFAULT
                )

                !objEmoji.isNullOrBlank() -> Basic.Emoji(
                    unicode = objEmoji,
                    fallback = objType?.objectFallbackIcon()
                        ?: ObjectIcon.TypeIcon.Fallback.DEFAULT
                )

                else -> objTypeIcon.setTransparentColor()
            }
        }

        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.PROFILE -> {
            when {
                !objImage.isNullOrBlank() -> ObjectIcon.Profile.Image(
                    hash = builder.thumbnail(objImage),
                    name = objName
                )

                else -> ObjectIcon.Profile.Avatar(name = objName)
            }
        }

        ObjectType.Layout.TODO -> {
            ObjectIcon.Task(isChecked = obj.done == true)
        }

        ObjectType.Layout.FILE,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.AUDIO,
        ObjectType.Layout.PDF -> {
            ObjectIcon.File(
                mime = obj.fileMimeType,
                fileName = objName,
                extensions = obj.fileExt
            )
        }

        ObjectType.Layout.BOOKMARK -> {
            when {
                !objImage.isNullOrBlank() -> ObjectIcon.Bookmark(
                    image = builder.thumbnail(objImage),
                    fallback = objTypeIcon.setTransparentColor()
                )

                else -> objTypeIcon.setTransparentColor()
            }
        }

        else -> {
            objTypeIcon.setTransparentColor()
        }
    }
}

private fun ObjectIcon.TypeIcon.setTransparentColor(): ObjectIcon.TypeIcon {
    return if (this is ObjectIcon.TypeIcon.Default) {
        this.copy(color = CustomIconColor.Transparent)
    } else {
        this
    }
}

fun ObjectWrapper.Type.objectIcon(): ObjectIcon.TypeIcon {

    if (isDeleted == true) {
        return ObjectIcon.TypeIcon.Deleted
    }

    val objEmoji = iconEmoji
    val objIconName = iconName
    val objIconOption = iconOption
    return when {

        !objEmoji.isNullOrEmpty() -> {
            ObjectIcon.TypeIcon.Emoji(
                unicode = objEmoji,
                rawValue = objIconName.orEmpty(),
                color = CustomIconColor.fromIconOption(objIconOption?.toInt())
            )
        }

        !objIconName.isNullOrEmpty() -> ObjectIcon.TypeIcon.Default(
            rawValue = objIconName,
            color = CustomIconColor.fromIconOption(objIconOption?.toInt())
        )

        else -> ObjectIcon.TypeIcon.Default(
            rawValue = ObjectIcon.TypeIcon.Default.DEFAULT_CUSTOM_ICON,
            color = CustomIconColor.DEFAULT
        )
    }
}

private fun ObjectWrapper.Type.objectFallbackIcon(): ObjectIcon.TypeIcon.Fallback {

    val objIconName = iconName
    return when {

        !objIconName.isNullOrEmpty() -> ObjectIcon.TypeIcon.Fallback(
            rawValue = objIconName
        )

        else -> ObjectIcon.TypeIcon.Fallback(
            rawValue = ObjectIcon.TypeIcon.Default.DEFAULT_CUSTOM_ICON
        )
    }
}

@Deprecated("Use ObjectWrapper.Basic.icon(builder, objType) instead")
fun ObjectWrapper.Basic.objectIcon(builder: UrlBuilder): ObjectIcon {
    return ObjectIcon.None
}
