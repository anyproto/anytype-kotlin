package com.anytypeio.anytype.ui.quickcapture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.syncstatus.StatusBadge
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.presentation.quickcapture.QuickCaptureViewModel

/**
 * Single-row header of the quick-capture sheet:
 * [ space chip ▾ ]        [sync ●] [⋯] [🗑] [ ↑ ]
 */
@Composable
fun QuickCaptureHeader(
    selectedSpace: QuickCaptureViewModel.SpaceView?,
    syncStatus: SpaceSyncAndP2PStatusState,
    isDraftEmpty: Boolean,
    hasDraftsElsewhere: Boolean,
    onSpaceChipClicked: () -> Unit,
    onSyncStatusClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    onClearDraftClicked: () -> Unit,
    onSendClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(color = colorResource(id = R.color.background_primary))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // weight(1f) with no competing weighted spacer: the chip gets every pixel the fixed
        // action buttons do not need, so the name only ellipsizes when it truly must, and
        // the dropdown chevron is never pushed out of the row.
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .noRippleThrottledClickable { onSpaceChipClicked() }
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedSpace != null) {
                SpaceIconView(
                    icon = selectedSpace.icon,
                    mainSize = 24.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedSpace.name.ifEmpty { stringResource(id = R.string.untitled) },
                    style = PreviewTitle2Medium,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down_18),
                    contentDescription = stringResource(id = R.string.quick_capture_choose_space),
                    tint = colorResource(id = R.color.glyph_active)
                )
                if (hasDraftsElsewhere) {
                    val draftsElsewhereLabel =
                        stringResource(id = R.string.quick_capture_drafts_elsewhere)
                    // The sheet always opens where this device last captured, so a user who
                    // discards that draft would never learn that other spaces still hold
                    // unfinished ones. This is the only hint that opening the picker is
                    // worthwhile. Populated by the background cross-space query, so it
                    // appears a moment after open rather than blocking it.
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.palette_system_amber_100))
                            .semantics {
                                contentDescription = draftsElsewhereLabel
                            }
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .noRippleThrottledClickable { onSyncStatusClicked() },
            contentAlignment = Alignment.Center
        ) {
            StatusBadge(
                status = syncStatus,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .noRippleThrottledClickable { onMenuClicked() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_32),
                contentDescription = null,
                tint = colorResource(id = R.color.glyph_active)
            )
        }
        if (!isDraftEmpty) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .noRippleThrottledClickable { onClearDraftClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ci_trash),
                    contentDescription = stringResource(id = R.string.quick_capture_clear_draft),
                    tint = colorResource(id = R.color.glyph_active),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Accent fill means "you can send". Explicit colors per state rather than an alpha
        // over one fill, so the enabled state is unmistakably the accent one.
        val canSend = !isDraftEmpty
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    color = if (canSend) {
                        colorResource(id = R.color.control_accent)
                    } else {
                        colorResource(id = R.color.shape_primary)
                    }
                )
                .noRippleThrottledClickable {
                    if (canSend) onSendClicked()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_up_18),
                contentDescription = stringResource(id = R.string.quick_capture_send),
                tint = if (canSend) {
                    colorResource(id = R.color.glyph_white)
                } else {
                    colorResource(id = R.color.glyph_inactive)
                }
            )
        }
    }
}

/**
 * Space picker for the chip: list rows with icon + name, checkmark on the current space,
 * plain text filter on top. Uses the elevated surface so the sheet separates from the
 * dimmed content behind it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCaptureSpacePicker(
    spaces: List<QuickCaptureViewModel.SpaceView>,
    onSpaceClicked: (Id) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) {
        spaces
    } else {
        spaces.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Dragger(modifier = Modifier.padding(vertical = 6.dp))
        }
    ) {
        DefaultSearchBar(
            modifier = Modifier.padding(horizontal = 16.dp),
            onQueryChanged = { query = it }
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = filtered,
                key = { view -> view.space.id }
            ) { view ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .noRippleThrottledClickable {
                            view.targetSpaceId?.let { onSpaceClicked(it) }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpaceIconView(
                        icon = view.icon,
                        mainSize = 48.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    if (view.hasDraft) {
                        // Left of the name: this space is already holding an unsent draft, so
                        // switching to it opens that draft rather than a blank one.
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_24),
                            contentDescription = stringResource(
                                id = R.string.quick_capture_space_has_draft
                            ),
                            tint = colorResource(id = R.color.glyph_active),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = view.name.ifEmpty { stringResource(id = R.string.untitled) },
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (view.isSelected) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check_16),
                            contentDescription = null,
                            tint = colorResource(id = R.color.glyph_active)
                        )
                    }
                }
            }
        }
    }
}

/**
 * The draft on screen was written earlier and not touched this session, and the target space
 * has nothing at stake. Moving is still available, but it is no longer assumed: reopening a
 * note and then switching space is not the same gesture as writing one and sending it
 * elsewhere. Dismissing changes nothing.
 */
@Composable
fun MoveOrNewDraftDialog(
    spaceName: String,
    onMoveDraft: () -> Unit,
    onStartNew: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = colorResource(id = R.color.background_secondary),
        title = {
            Text(
                text = stringResource(
                    id = R.string.quick_capture_move_or_new_title,
                    spaceName.ifEmpty { stringResource(id = R.string.untitled) }
                ),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        },
        text = {
            Text(
                text = stringResource(
                    id = R.string.quick_capture_move_or_new_message,
                    spaceName.ifEmpty { stringResource(id = R.string.untitled) }
                ),
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        },
        confirmButton = {
            TextButton(onClick = onMoveDraft) {
                Text(
                    text = stringResource(id = R.string.quick_capture_move_or_new_move),
                    color = colorResource(id = R.color.text_primary)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onStartNew) {
                Text(
                    text = stringResource(id = R.string.quick_capture_move_or_new_start),
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    )
}

/**
 * Both spaces hold unsent text. The question is only ever about the draft on screen — the
 * target's draft is never touched, because the user cannot see it and so cannot judge what
 * replacing it would cost. Keeping both is the safe answer and is offered first; discarding
 * destroys only what is visible, on an explicit instruction. Dismissing changes nothing.
 */
@Composable
fun DraftConflictDialog(
    spaceName: String,
    onKeepBoth: () -> Unit,
    onDiscardCurrent: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = colorResource(id = R.color.background_secondary),
        title = {
            Text(
                text = stringResource(
                    id = R.string.quick_capture_draft_conflict_title,
                    spaceName.ifEmpty { stringResource(id = R.string.untitled) }
                ),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.quick_capture_draft_conflict_message),
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        },
        // Material renders dismissButton on the LEFT. The destructive choice therefore belongs
        // in confirmButton, matching ClearDraftConfirmation below and every other destructive
        // dialog in the app — otherwise the permanent delete would sit exactly where this same
        // feature's other dialog puts "Cancel", and muscle memory would destroy a note.
        confirmButton = {
            TextButton(onClick = onDiscardCurrent) {
                Text(
                    text = stringResource(id = R.string.quick_capture_draft_conflict_discard),
                    color = colorResource(id = R.color.palette_system_red)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepBoth) {
                Text(
                    text = stringResource(id = R.string.quick_capture_draft_conflict_keep),
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    )
}

@Composable
fun ClearDraftConfirmation(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = colorResource(id = R.color.background_secondary),
        text = {
            Text(
                text = stringResource(id = R.string.quick_capture_clear_draft_confirmation),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(id = R.string.quick_capture_clear_draft),
                    color = colorResource(id = R.color.palette_system_red)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    )
}
