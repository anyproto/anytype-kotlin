package com.anytypeio.anytype.core_models.ui

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Url

/**
 * Represents the visual icon for an object in the UI.
 * This sealed class hierarchy covers all possible icon types.
 */
sealed class ObjectIcon {

    data object None : ObjectIcon()

    sealed class Basic : ObjectIcon() {
        data class Image(
            val hash: Hash,
            val fallback: TypeIcon.Fallback = TypeIcon.Fallback.DEFAULT
        ) : Basic()

        data class Emoji(
            val unicode: String,
            val fallback: TypeIcon.Fallback = TypeIcon.Fallback.DEFAULT,
            val circleShape: Boolean = false
        ) : Basic()
    }

    sealed class Profile : ObjectIcon() {
        data class Avatar(val name: String) : Profile()
        data class Image(val hash: Hash, val name: String) : Profile()
    }

    data class Task(val isChecked: Boolean) : ObjectIcon()

    data class Bookmark(
        val image: Url,
        val fallback: TypeIcon
    ) : ObjectIcon()

    data class File(
        val mime: String?,
        val extensions: String? = null
    ) : ObjectIcon()

    data class FileDefault(
        val mime: MimeCategory
    ) : ObjectIcon()

    data object Deleted : ObjectIcon()

    data class Checkbox(val isChecked: Boolean) : ObjectIcon()

    /**
     * A type icon. [rawValue] holds the icon name of the icon set, in kebab-case.
     *
     * The UI layer maps the name to an image: `CustomIcons` for Compose, and
     * `CustomIconDrawables` for the View based screens. Do not build a resource name from
     * [rawValue] here. The resource shrinker of the release build deletes a drawable that no
     * code names statically, and a name that the app builds at run time then resolves to 0.
     */
    sealed class TypeIcon : ObjectIcon() {

        data object Deleted : TypeIcon() {
            const val DEFAULT_DELETED_ICON = "extension-puzzle"
        }

        data class Emoji(
            val unicode: String,
            val rawValue: String,
            val color: CustomIconColor,
        ) : TypeIcon()

        data class Default(
            val rawValue: String,
            val color: CustomIconColor,
        ) : TypeIcon() {

            companion object {
                const val DEFAULT_CUSTOM_ICON = "extension-puzzle"

                val DEFAULT = Default(DEFAULT_CUSTOM_ICON, CustomIconColor.DEFAULT)
                val DATE = Default("calendar", CustomIconColor.DEFAULT)
            }
        }

        //we use this icon when we can't find the emoji for object or image icon can't be loaded
        data class Fallback(val rawValue: String, val isCircleShape: Boolean = false) : TypeIcon() {

            companion object {
                const val DEFAULT_FALLBACK_ICON = "extension-puzzle"

                val DEFAULT = Fallback(DEFAULT_FALLBACK_ICON)
            }
        }
    }

    data class SimpleIcon(val rawValue: String, val color: Int) : ObjectIcon()
}