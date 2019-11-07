package com.agileburo.anytype.presentation.auth.model

import com.agileburo.anytype.core_utils.ui.ViewType

sealed class SelectAccountView : ViewType {

    data class AccountView(
        val id: String,
        val name: String,
        val image: ByteArray? = null
    ) : SelectAccountView(), ViewType {

        override fun getViewType(): Int = PROFILE

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AccountView

            if (id != other.id) return false
            if (name != other.name) return false
            if (image != null) {
                if (other.image == null) return false
                if (!image.contentEquals(other.image)) return false
            } else if (other.image != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + (image?.contentHashCode() ?: 0)
            return result
        }
    }

    companion object {
        const val PROFILE = 0
        const val ADD_NEW_PROFILE = 1
    }

}