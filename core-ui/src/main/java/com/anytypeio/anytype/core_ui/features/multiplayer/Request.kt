package com.anytypeio.anytype.core_ui.features.multiplayer

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonMedium
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonUpgrade
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.presentation.multiplayer.InviteButton
import com.anytypeio.anytype.presentation.multiplayer.SpaceJoinRequestViewModel.ViewState

@SuppressLint("UnusedBoxWithConstraintsScope")
@Preview(
    backgroundColor = 0x0AAEED,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Preview(
    backgroundColor = 0x000000,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun SpaceJoinRequestScreenPreview() {
    SpaceJoinRequestScreen(
        onAddEditorClicked = {},
        onAddViewerClicked = {},
        onRejectClicked = {},
        onUpgradeClicked = {},
        state = ViewState.Success(
            newMember = "1",
            newMemberName = "Merk",
            spaceName = "Investors",
            icon = SpaceMemberIconView.Placeholder("Merk"),
            buttons = listOf(
                InviteButton.JOIN_AS_VIEWER,
                InviteButton.JOIN_AS_VIEWER_DISABLED,
                InviteButton.JOIN_AS_EDITOR,
                InviteButton.JOIN_AS_EDITOR_DISABLED,
                InviteButton.ADD_MORE_VIEWERS,
                InviteButton.ADD_MORE_EDITORS,
                InviteButton.UPGRADE,
                InviteButton.REJECT
            )
        )
    )
}

@Composable
fun SpaceJoinRequestScreen(
    state: ViewState,
    onAddViewerClicked: (Id) -> Unit,
    onAddEditorClicked: (Id) -> Unit,
    onRejectClicked: (Id) -> Unit,
    onUpgradeClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        when (state) {
            ViewState.Init -> {
                AnimatedVisibility(
                    visible = state is ViewState.Init,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(346.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp),
                            color = colorResource(R.color.shape_secondary),
                            trackColor = colorResource(R.color.shape_primary)
                        )
                    }
                }
            }

            is ViewState.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SpaceMemberIcon(
                        icon = state.icon,
                        modifier = Modifier.align(Alignment.Center),
                        iconSize = 72.dp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(
                        R.string.multiplayer_space_join_request_header,
                        state.newMemberName.ifEmpty { stringResource(id = R.string.untitled) },
                        state.spaceName.ifEmpty { stringResource(id = R.string.untitled) }
                    ),
                    style = HeadlineHeading,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        horizontal = 48.dp
                    ),
                    color = colorResource(id = R.color.text_primary)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Buttons(
                    newMember = state.newMember,
                    buttons = state.buttons,
                    onAddViewerClicked = onAddViewerClicked,
                    onAddEditorClicked = onAddEditorClicked,
                    onRejectClicked = onRejectClicked,
                    onUpgradeClicked = onUpgradeClicked
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            is ViewState.Error.ActiveTierError -> {
                ErrorState(msg = stringResource(R.string.multiplayer_space_join_request_membership_status_error))
            }

            is ViewState.Error.CurrentUserStatusError -> {
                ErrorState(msg = stringResource(R.string.multiplayer_space_join_request_current_user_error))
            }

            is ViewState.Error.NewMemberError -> {
                ErrorState(msg = stringResource(R.string.multiplayer_space_join_request_new_member_error))
            }

            is ViewState.Error.SpaceParticipantsError -> {
                ErrorState(msg = stringResource(R.string.multiplayer_space_join_request_participants_error))
            }
        }
    }
}

@Composable
private fun ErrorState(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(346.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = msg,
            style = BodyRegular,
            color = colorResource(R.color.text_primary),
            maxLines = 3
        )
    }
}

@Composable
private fun Buttons(
    newMember: Id,
    buttons: List<InviteButton>,
    onAddViewerClicked: (Id) -> Unit,
    onAddEditorClicked: (Id) -> Unit,
    onRejectClicked: (Id) -> Unit,
    onUpgradeClicked: () -> Unit,
) {
    buttons.forEach {
        when (it) {
            InviteButton.JOIN_AS_VIEWER -> ButtonSecondary(
                text = stringResource(R.string.multiplayer_space_add_viewer),
                onClick = throttledClick(
                    onClick = { onAddViewerClicked(newMember) }
                ),
                size = ButtonSize.Large,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    .fillMaxWidth()
            )

            InviteButton.JOIN_AS_VIEWER_DISABLED -> ButtonSecondary(
                text = stringResource(R.string.multiplayer_space_add_viewer),
                onClick = throttledClick(
                    onClick = { }
                ),
                size = ButtonSize.Large,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    .fillMaxWidth(),
                enabled = false
            )

            InviteButton.JOIN_AS_EDITOR -> ButtonSecondary(
                text = stringResource(R.string.multiplayer_space_add_editor),
                onClick = throttledClick(
                    onClick = { onAddEditorClicked(newMember) }
                ),
                size = ButtonSize.Large,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    .fillMaxWidth()
            )

            InviteButton.JOIN_AS_EDITOR_DISABLED -> ButtonSecondary(
                text = stringResource(R.string.multiplayer_space_add_editor),
                onClick = throttledClick(
                    onClick = { }
                ),
                size = ButtonSize.Large,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    .fillMaxWidth(),
                enabled = false
            )

            InviteButton.REJECT -> ButtonWarning(
                text = stringResource(R.string.multiplayer_space_request_reject),
                onClick = throttledClick(
                    onClick = { onRejectClicked(newMember) }
                ),
                size = ButtonSize.Large,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                    .fillMaxWidth(),
            )

            InviteButton.ADD_MORE_VIEWERS -> ButtonSecondary(
                text = stringResource(R.string.multiplayer_space_add_more_viewer),
                onClick = throttledClick(
                    onClick = { onUpgradeClicked() }
                ),
                size = ButtonSize.Large,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    .fillMaxWidth()
            )

            InviteButton.ADD_MORE_EDITORS -> ButtonSecondary(
                text = stringResource(R.string.multiplayer_space_add_more_editor),
                onClick = throttledClick(
                    onClick = { onUpgradeClicked() }
                ),
                size = ButtonSize.Large,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                    .fillMaxWidth()
            )

            InviteButton.UPGRADE -> ButtonUpgrade(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                    .height(48.dp)
                    .verticalScroll(rememberScrollState()),
                onClick = { onUpgradeClicked() },
                text = stringResource(id = R.string.multiplayer_upgrade_button_request),
                style = ButtonMedium
            )
        }
    }
}
