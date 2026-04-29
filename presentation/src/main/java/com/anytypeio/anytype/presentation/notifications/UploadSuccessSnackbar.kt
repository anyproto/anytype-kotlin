package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.Id

/**
 * One-shot event emitted by a host ViewModel after an upload batch
 * completes successfully. Surfaced to the user as an app-level
 * Snackbar (see MainActivity's Command.SnackbarWithOk and
 * Command.SnackbarWithOpenType).
 *
 * Typed variants carry the resolved bundled object-type id ([typeId]),
 * the [space] they were uploaded into, and the type's [pluralName]
 * (e.g. "Images" / "Videos" / "Files") used to label the snackbar's
 * "Open <plural>" action. [Mixed] is emitted when a single batch
 * contains more than one distinct file type (no single sensible target).
 */
sealed interface UploadSuccessSnackbar {
    sealed interface Typed : UploadSuccessSnackbar {
        val typeId: Id
        val space: Id
        val pluralName: String
    }
    data class Image(
        override val typeId: Id,
        override val space: Id,
        override val pluralName: String
    ) : Typed
    data class Video(
        override val typeId: Id,
        override val space: Id,
        override val pluralName: String
    ) : Typed
    data class File(
        override val typeId: Id,
        override val space: Id,
        override val pluralName: String
    ) : Typed
    data object Mixed : UploadSuccessSnackbar
}
