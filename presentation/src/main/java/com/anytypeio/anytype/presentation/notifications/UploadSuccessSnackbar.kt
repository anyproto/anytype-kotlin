package com.anytypeio.anytype.presentation.notifications

/**
 * One-shot event emitted by a host ViewModel after an upload batch
 * completes successfully. Surfaced to the user as an app-level
 * Snackbar with an OK action (see MainActivity's Command.SnackbarWithOk).
 *
 * Variants correspond to the type of the uploaded items; [Mixed] is
 * emitted when a single batch contains more than one distinct type.
 */
sealed interface UploadSuccessSnackbar {
    data object Image : UploadSuccessSnackbar
    data object Video : UploadSuccessSnackbar
    data object File : UploadSuccessSnackbar
    data object Mixed : UploadSuccessSnackbar
}
