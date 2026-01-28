package com.anytypeio.anytype.presentation.profile

import com.anytypeio.anytype.core_models.ui.ProfileIconView

/**
 * Represents the UI state for the profile QR code screen.
 *
 * Used to control the visibility and content of the QR code bottom sheet
 * that allows users to share their 1-1 chat invitation link.
 */
sealed class UiProfileQrCodeState {

    /**
     * QR code screen is hidden.
     */
    data object Hidden : UiProfileQrCodeState()

    /**
     * QR code screen is visible with the generated profile link.
     *
     * @property link The generated deep link URL for 1-1 chat initiation
     * @property name The current user's profile name
     * @property icon The current user's profile icon
     * @property globalName The user's global name for toolbar display (e.g., "username.any")
     */
    data class ProfileLink(
        val link: String,
        val name: String,
        val icon: ProfileIconView,
        val globalName: String? = null,
        val identity: String? = null,
    ) : UiProfileQrCodeState()
}
