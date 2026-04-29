package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes

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

/**
 * Resolve a single-batch list of uploaded file types into the appropriate
 * [UploadSuccessSnackbar] variant for [space], looking up the bundled
 * object type's id and plural name via [storeOfObjectTypes].
 *
 * Returns [UploadSuccessSnackbar.Mixed] when the batch contains more
 * than one distinct file type, when the bundled type is missing from
 * the store (rare race during space switch), or when its plural/name
 * are blank — in those cases the host shows a plain OK snackbar with
 * no navigation target.
 *
 * Audio / PDF and any other non-Image/Video types collapse into the
 * File variant, matching iOS parity.
 */
suspend fun List<Block.Content.File.Type>.toSnackbarVariant(
    space: Id,
    storeOfObjectTypes: StoreOfObjectTypes
): UploadSuccessSnackbar {
    val distinct = distinct()
    if (distinct.size > 1) return UploadSuccessSnackbar.Mixed
    val fileType = distinct.single()
    val key = when (fileType) {
        Block.Content.File.Type.IMAGE -> ObjectTypeUniqueKeys.IMAGE
        Block.Content.File.Type.VIDEO -> ObjectTypeUniqueKeys.VIDEO
        else -> ObjectTypeUniqueKeys.FILE
    }
    val type = storeOfObjectTypes.getByKey(key) ?: return UploadSuccessSnackbar.Mixed
    val pluralName = type.pluralName?.takeIf { it.isNotBlank() }
        ?: type.name?.takeIf { it.isNotBlank() }
        ?: return UploadSuccessSnackbar.Mixed
    return when (fileType) {
        Block.Content.File.Type.IMAGE -> UploadSuccessSnackbar.Image(type.id, space, pluralName)
        Block.Content.File.Type.VIDEO -> UploadSuccessSnackbar.Video(type.id, space, pluralName)
        else -> UploadSuccessSnackbar.File(type.id, space, pluralName)
    }
}
