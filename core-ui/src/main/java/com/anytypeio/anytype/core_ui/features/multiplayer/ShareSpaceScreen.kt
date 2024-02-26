package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceMemberView
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel

@Composable
fun ShareSpaceScreen(
    members: List<ShareSpaceMemberView>,
    viewState: ShareSpaceViewModel.ViewState,
    onRegenerateInviteLinkClicked: () -> Unit,
    onShareInviteLinkClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Dragger(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(vertical = 6.dp)
                    )
                }
            }
            item {
                Toolbar(title = stringResource(R.string.multiplayer_share_space))
            }
            item {
                Section(
                    title = stringResource(R.string.multiplayer_members_and_requests)
                )
            }
            members.forEach { member ->
                item {
                    when(val config = member.config) {
                        is ShareSpaceMemberView.Config.Member -> {
                            SpaceMember(participant = member)
                        }
                        is ShareSpaceMemberView.Config.Request -> {
                            SpaceMemberRequest(
                                member = member.obj,
                                request = config
                            )
                        }
                    }
                }
            }
        }
        when(viewState) {
            ShareSpaceViewModel.ViewState.Init -> {
                // Do nothing.
            }
            is ShareSpaceViewModel.ViewState.Share -> {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    ShareInviteLinkCard(
                        link = viewState.link,
                        onShareInviteClicked = onShareInviteLinkClicked,
                        onRegenerateInviteLinkClicked = onRegenerateInviteLinkClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun SpaceMember(
    participant: ShareSpaceMemberView
) {
    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color = Color.Blue)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1.0f)
        ) {
            Text(
                text = participant.obj.name.orEmpty(),
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Can edit",
                style = Relations3,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        Image(
            modifier = Modifier.align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.ic_action_more),
            contentDescription = "Menu button"
        )
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
private fun SpaceMemberRequest(
    member: ObjectWrapper.Participant,
    request: ShareSpaceMemberView.Config.Request
) {
    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color = Color.Red)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1.0f)
        ) {
            Text(
                text = member.name.orEmpty(),
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(2.dp))
            val color = when(request) {
                ShareSpaceMemberView.Config.Request.Join -> ThemeColor.PINK
                ShareSpaceMemberView.Config.Request.Unjoin -> ThemeColor.RED
            }
            val text = when(request) {
                ShareSpaceMemberView.Config.Request.Join -> stringResource(
                    id = R.string.multiplayer_joining_requested
                )
                ShareSpaceMemberView.Config.Request.Unjoin -> stringResource(
                    id = R.string.multiplayer_unjoining_requested
                )
            }
            Text(
                text = text,
                color = dark(color),
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = light(color),
                        shape = RoundedCornerShape(size = 3.dp)
                    )
                    .padding(start = 6.dp, end = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Relations1
            )
        }
        when(request) {
            ShareSpaceMemberView.Config.Request.Join -> {
                ButtonSecondary(
                    text = stringResource(R.string.multiplayer_view_request),
                    onClick = { /*TODO*/ },
                    size = ButtonSize.Small,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            ShareSpaceMemberView.Config.Request.Unjoin -> {
                ButtonSecondary(
                    text = stringResource(R.string.multiplayer_approve_request),
                    onClick = { /*TODO*/ },
                    size = ButtonSize.Small,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
@Preview
fun SpaceJoinRequestPreview() {
    SpaceMemberRequest(
        member = ObjectWrapper.Participant(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to "Konstantin",
                Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
            )
        ),
        request = ShareSpaceMemberView.Config.Request.Join
    )
}

@Composable
@Preview
fun SpaceUnjoinRequestPreview() {
    SpaceMemberRequest(
        member = ObjectWrapper.Participant(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to "Konstantin",
                Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
            )
        ),
        request = ShareSpaceMemberView.Config.Request.Unjoin
    )
}

@Composable
@Preview
fun ShareSpaceScreenPreview() {
    ShareSpaceScreen(
        viewState = ShareSpaceViewModel.ViewState.Share(
            link = "https://anytype.io/ibafyrfhfsag6rea3ifffsasssg..."
        ),
        onShareInviteLinkClicked = {},
        onRegenerateInviteLinkClicked = {},
        members = buildList {
            add(
                ShareSpaceMemberView(
                    obj = ObjectWrapper.Participant(
                        mapOf(
                            Relations.ID to "1",
                            Relations.NAME to "Konstantin",
                            Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                        )
                    )
                )
            )
            add(
                ShareSpaceMemberView(
                    obj = ObjectWrapper.Participant(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Evgenii"
                        )
                    )
                )
            )
            add(
                ShareSpaceMemberView(
                    obj = ObjectWrapper.Participant(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Aleksey"
                        )
                    ),
                    config = ShareSpaceMemberView.Config.Request.Unjoin
                )
            )
            add(
                ShareSpaceMemberView(
                    obj = ObjectWrapper.Participant(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Anton"
                        )
                    ),
                    config = ShareSpaceMemberView.Config.Request.Join
                )
            )
        }
    )
}

@Composable
@Preview
private fun SpaceMemberPreview() {
    SpaceMember(
        participant = ShareSpaceMemberView(
            obj = ObjectWrapper.Participant(
                mapOf(
                    Relations.ID to "2",
                    Relations.NAME to "Evgenii"
                )
            )
        )
    )
}