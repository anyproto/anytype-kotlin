package com.anytypeio.anytype.presentation.`object`

import com.anytypeio.anytype.core_models.Hash

sealed class ObjectIcon {
    object None : ObjectIcon()
    sealed class Basic : ObjectIcon() {
        data class Image(val hash: Hash) : Basic()
        data class Emoji(val unicode: String) : Basic()
    }
    sealed class Profile : ObjectIcon() {
        data class Avatar(val name: String) : Profile()
        data class Image(val hash: Hash) : Profile()
    }
    data class Task(val isChecked: Boolean) : ObjectIcon()
}