package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon.Basic
import com.anytypeio.anytype.core_models.SupportedLayouts

fun ObjectWrapper.Basic.objectIcon(builder: UrlBuilder): ObjectIcon {

    if (isDeleted == true) {
        return ObjectIcon.Deleted
    }

    val objectIcon = layout?.icon(
        image = iconImage,
        emoji = iconEmoji,
        builder = builder,
        name = name.orEmpty()
    )

    if (objectIcon != null) {
        return objectIcon
    }

    if (SupportedLayouts.fileLayouts.contains(layout)) {
        return fileIcon(
            mime = fileMimeType,
            name = name,
            extensions = fileExt
        )
    }

    if (layout == ObjectType.Layout.TODO) {
        return taskIcon(isChecked = done == true)
    }

    return layout.emptyType()
}

fun ObjectType.Layout?.emptyType(): ObjectIcon.Empty {
    if (this == null) {
        return ObjectIcon.Empty.Page
    }
    return when (this) {
        ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> ObjectIcon.Empty.List
        ObjectType.Layout.OBJECT_TYPE -> ObjectIcon.Empty.ObjectType
        ObjectType.Layout.BOOKMARK -> ObjectIcon.Empty.Bookmark
        ObjectType.Layout.CHAT, ObjectType.Layout.CHAT_DERIVED -> ObjectIcon.Empty.Discussion
        ObjectType.Layout.DATE -> ObjectIcon.Empty.Date
        else -> ObjectIcon.Empty.Page
    }
}

fun ObjectType.Layout.icon(
    image: String?,
    emoji: String?,
    name: String,
    builder: UrlBuilder
): ObjectIcon? {
    return when (this) {

        ObjectType.Layout.BASIC,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.OBJECT_TYPE -> basicIcon(
            image = image,
            emoji = emoji,
            builder = builder,
            layout = this
        )

        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT -> profileIcon(
            image = image,
            name = name,
            builder = builder
        )

        ObjectType.Layout.BOOKMARK -> bookmarkIcon(
            iconImage = image,
            builder = builder
        )

        ObjectType.Layout.DATE -> emptyType()

        else -> null
    }
}

private fun basicIcon(
    image: String?,
    emoji: String?,
    builder: UrlBuilder,
    layout: ObjectType.Layout
): ObjectIcon? {
    return when {
        !image.isNullOrBlank() -> Basic.Image(
            hash = builder.thumbnail(image),
            emptyState = layout.emptyType()
        )

        !emoji.isNullOrBlank() -> Basic.Emoji(
            unicode = emoji,
            emptyState = layout.emptyType()
        )

        else -> null
    }
}

private fun profileIcon(image: String?, name: String, builder: UrlBuilder): ObjectIcon? {
    return when {
        !image.isNullOrBlank() -> ObjectIcon.Profile.Image(hash = builder.thumbnail(image))
        else -> ObjectIcon.Profile.Avatar(name = name)
    }
}

private fun bookmarkIcon(iconImage: String?, builder: UrlBuilder): ObjectIcon? {
    return when {
        !iconImage.isNullOrBlank() -> ObjectIcon.Bookmark(image = builder.thumbnail(iconImage))
        else -> null
    }
}

private fun fileIcon(
    mime: String?,
    name: String?,
    extensions: String?
): ObjectIcon {
    return ObjectIcon.File(
        mime = mime,
        fileName = name,
        extensions = extensions
    )
}

private fun taskIcon(isChecked: Boolean): ObjectIcon {
    return ObjectIcon.Task(isChecked = isChecked)
}
