package com.anytypeio.anytype.feature_create_object.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.notifications.UploadSuccessSnackbar
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for managing the create object screen state.
 * Handles fetching object types from the store and filtering them based on search query.
 * Types are sorted according to user's custom widget order (orderId), with fallback to
 * space-specific default order, then alphabetical.
 *
 * Also owns upload plumbing for the popup's media rows (Photos / Camera / Files):
 * when the host surface exposes `showMediaSection`, selected URIs are uploaded as
 * standalone file/image/video objects in the current space via [uploadFiles], and
 * success events are emitted on [uploadSnackbar] for the host fragment to display.
 * Centralising upload here means each host screen only needs the shared VM and does
 * not have to add `UploadFile` / `FileSharer` to its own VM.
 */
class NewCreateObjectViewModel @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val uploadFile: UploadFile,
    private val fileSharer: FileSharer,
    private val vmParams: VmParams
) : ViewModel() {

    private val _state = MutableStateFlow(
        NewCreateObjectState(
            showMediaSection = vmParams.showMediaSection,
            showAttachExisting = vmParams.showAttachObject
        )
    )
    val state: StateFlow<NewCreateObjectState> = _state.asStateFlow()

    private val _uploadSnackbar = MutableSharedFlow<UploadSuccessSnackbar>(
        replay = 0,
        extraBufferCapacity = 8
    )
    val uploadSnackbar: SharedFlow<UploadSuccessSnackbar> = _uploadSnackbar.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observeObjectTypes()
    }

    /**
     * Observes object types and space type, combining them to produce a sorted list.
     * The sort order respects user's custom widget ordering via orderId field.
     */
    private fun observeObjectTypes() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                combine(
                    storeOfObjectTypes.observe(),
                    spaceViewContainer.observe(
                        space = vmParams.spaceId,
                        keys = listOf(Relations.SPACE_TYPE, Relations.SPACE_UX_TYPE),
                        mapper = { it.isOneToOneSpace }
                    )
                ) { allTypes, isOneToOneSpace ->
                    // Allow-list of layouts creatable via the "New Object" flow —
                    // matches iOS `supportedForCreationBase` and excludes file/media
                    // layouts (FILE, IMAGE, VIDEO, AUDIO, PDF) by construction since
                    // those types exist only via upload, not empty creation.
                    val allowedLayouts = SupportedLayouts.getCreateObjectLayouts(isOneToOneSpace)

                    val filteredTypes = allTypes.filter { type ->
                        type.isValid &&
                        type.isDeleted != true &&
                        type.isArchived != true &&
                        type.uniqueKey != ObjectTypeIds.TEMPLATE &&
                        allowedLayouts.contains(type.recommendedLayout)
                    }

                    // Sort using user's custom widget order, then map to UI model.
                    // CHAT-type spaces no longer exist, so the isChatSpace sort flag
                    // collapses to isOneToOneSpace — the only remaining special case.
                    filteredTypes
                        .sortByTypePriority(isChatSpace = isOneToOneSpace)
                        .map { type ->
                            ObjectTypeItem(
                                typeKey = type.uniqueKey,
                                name = type.name.orEmpty(),
                                icon = type.objectIcon()
                            )
                        }
                }.collect { types ->
                    _state.update {
                        it.copy(
                            objectTypes = types,
                            filteredObjectTypes = applySearchFilter(types, it.searchQuery),
                            isLoading = false
                        )
                    }
                    Timber.d("Loaded ${types.size} object types with custom sort order")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load object types")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    /**
     * Resets transient UI state (search query, filtered list, error) without
     * re-subscribing to the underlying hot flows. Call when the sheet opens so
     * the user always sees the full, unfiltered list of types.
     */
    fun onOpen() {
        _state.update { current ->
            current.copy(
                searchQuery = "",
                filteredObjectTypes = current.objectTypes,
                error = null
            )
        }
    }

    /**
     * Clears the error state and retries loading object types.
     */
    private fun retry() {
        _state.update { it.copy(error = null) }
        observeObjectTypes()
    }

    /**
     * Applies the search filter to the provided list of types.
     */
    private fun applySearchFilter(
        types: List<ObjectTypeItem>,
        query: String
    ): List<ObjectTypeItem> {
        return if (query.isBlank()) {
            types
        } else {
            types.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    /**
     * Updates the search query and filters the object types list.
     * Filtering is case-insensitive and matches against the type name.
     *
     * @param query The search query entered by the user
     */
    fun onSearchQueryChanged(query: String) {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredObjectTypes = applySearchFilter(currentState.objectTypes, query)
            )
        }
    }

    /**
     * Handles actions from the UI.
     * Only processes search actions internally; other actions are handled by parent components.
     *
     * @param action The action to process
     */
    fun onAction(action: CreateObjectAction) {
        when (action) {
            is CreateObjectAction.UpdateSearch -> onSearchQueryChanged(action.query)
            is CreateObjectAction.Retry -> retry()
            // Other actions (media, create object, attach) are handled by the parent component
            else -> { /* No-op */
            }
        }
    }

    /**
     * Resolves an object type by its unique key. Used by host fragments to
     * convert a `CreateObjectAction.CreateObjectOfType` (which carries only
     * the key) back into the full [ObjectWrapper.Type] their screen VM's
     * create method expects. Returns null if the type is unknown, e.g. when
     * it has not finished syncing into [storeOfObjectTypes] yet.
     */
    suspend fun resolveType(typeKey: TypeKey): ObjectWrapper.Type? {
        return storeOfObjectTypes.getByKey(typeKey.key)
    }

    /**
     * Uploads a list of URIs as standalone file/image/video objects in the
     * current space. Triggered from the popup's media rows. Emits to
     * [uploadSnackbar] on success so the host fragment can show the usual
     * upload-complete snackbar.
     */
    fun uploadFiles(targets: List<CreateObjectUploadTarget>) {
        if (vmParams.spaceId.id.isEmpty() || targets.isEmpty()) return
        viewModelScope.launch {
            val successes = mutableListOf<Block.Content.File.Type>()
            targets.forEach { target ->
                val path = runCatching { fileSharer.getPath(target.uri) }
                    .getOrNull()
                    ?.takeIf { it.isNotEmpty() }
                if (path == null) {
                    Timber.w("Upload: could not resolve path for ${target.uri}")
                    target.sourceFilePath?.let { src ->
                        runCatching { java.io.File(src).delete() }
                    }
                    return@forEach
                }
                uploadFile.async(
                    UploadFile.Params(
                        space = vmParams.spaceId,
                        path = path,
                        type = target.type,
                        createdInContext = null
                    )
                ).fold(
                    onSuccess = { uploaded ->
                        Timber.d("Upload success id=${uploaded.id}")
                        successes += target.type
                    },
                    onFailure = { e -> Timber.e(e, "Upload failed for $path") }
                )
                runCatching { java.io.File(path).delete() }
                target.sourceFilePath?.let { src ->
                    runCatching { java.io.File(src).delete() }
                }
            }
            if (successes.isNotEmpty()) {
                _uploadSnackbar.emit(successes.toSnackbarVariant())
            }
        }
    }

    private fun List<Block.Content.File.Type>.toSnackbarVariant(): UploadSuccessSnackbar {
        val distinct = distinct()
        if (distinct.size > 1) return UploadSuccessSnackbar.Mixed
        return when (distinct.single()) {
            Block.Content.File.Type.IMAGE -> UploadSuccessSnackbar.Image
            Block.Content.File.Type.VIDEO -> UploadSuccessSnackbar.Video
            else -> UploadSuccessSnackbar.File
        }
    }

    /**
     * Parameters for the ViewModel.
     *
     * @param spaceId The current space ID, used to determine space type for sorting.
     * @param showAttachObject Whether the host surface should offer "attach existing
     *   object" as an affordance. This is currently chat-only. Defaults to false.
     * @param showMediaSection Whether the host surface should offer media attachment
     *   rows (Photos / Camera / Files). Defaults to false.
     */
    data class VmParams(
        val spaceId: SpaceId,
        val showAttachObject: Boolean = false,
        val showMediaSection: Boolean = false
    )
}

/**
 * Describes a local URI that should be uploaded to the current space when the
 * user picks it from the create-object popup's media rows.
 *
 * @property uri The content URI (from `PickVisualMedia`, `OpenMultipleDocuments`,
 *   or the `FileProvider` URI used for camera capture).
 * @property type File kind hint; middleware uses it when creating the resulting
 *   object.
 * @property sourceFilePath Optional local file path to delete once upload
 *   completes. Used by the camera capture path to clean up the FileProvider-
 *   backed temp file in the app cache.
 */
data class CreateObjectUploadTarget(
    val uri: String,
    val type: Block.Content.File.Type,
    val sourceFilePath: String? = null
)
