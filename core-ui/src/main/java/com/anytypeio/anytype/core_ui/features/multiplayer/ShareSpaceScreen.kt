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
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceMemberView
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView

@Composable
fun ShareSpaceScreen(
    members: List<ShareSpaceMemberView>,
    viewState: ShareSpaceViewModel.ViewState,
    onRegenerateInviteLinkClicked: () -> Unit,
    onShareInviteLinkClicked: () -> Unit,
    onViewRequestClicked: (ShareSpaceMemberView) -> Unit,
    onApproveUnjoinRequestClicked: (ShareSpaceMemberView) -> Unit,
    onCanViewClicked: (ShareSpaceMemberView) -> Unit,
    onCanEditClicked: (ShareSpaceMemberView) -> Unit,
    onRemoveMemberClicked: (ShareSpaceMemberView) -> Unit,
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    Toolbar(title = stringResource(R.string.multiplayer_share_space))
                    Image(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                        ,
                        painter = painterResource(id = R.drawable.ic_action_more),
                        contentDescription = "Menu button"
                    )
                }
            }
            item {
                Section(
                    title = stringResource(R.string.multiplayer_members_and_requests)
                )
            }
            members.forEachIndexed { index, member ->
                item {
                    when(val config = member.config) {
                        is ShareSpaceMemberView.Config.Member -> {
                            SpaceMember(
                                member = member.obj,
                                config = config,
                                onCanEditClicked = {
                                    onCanEditClicked(member)
                                },
                                onCanViewClicked = {
                                    onCanViewClicked(member)
                                },
                                onRemoveMemberClicked = {
                                    onRemoveMemberClicked(member)
                                },
                                icon = member.icon
                            )
                        }
                        is ShareSpaceMemberView.Config.Request -> {
                            SpaceMemberRequest(
                                member = member.obj,
                                icon = member.icon,
                                request = config,
                                onViewRequestClicked = {
                                    onViewRequestClicked(member)
                                },
                                onApproveUnjoinRequestClicked = {
                                    onApproveUnjoinRequestClicked(member)
                                }
                            )
                        }
                    }
                    if (index != members.lastIndex) {
                        Divider()
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
    member: ObjectWrapper.Participant,
    icon: SpaceMemberIconView,
    config: ShareSpaceMemberView.Config.Member,
    onCanEditClicked: () -> Unit,
    onCanViewClicked: () -> Unit,
    onRemoveMemberClicked: () -> Unit
) {
    var isMemberMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        SpaceMemberIcon(
            icon = icon,
            modifier = Modifier.align(Alignment.CenterVertically)
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
            Text(
                text = when(config) {
                    ShareSpaceMemberView.Config.Member.Writer -> {
                        stringResource(id = R.string.multiplayer_can_edit)
                    }
                    ShareSpaceMemberView.Config.Member.Owner -> {
                        stringResource(id = R.string.multiplayer_owner)
                    }
                    ShareSpaceMemberView.Config.Member.Reader -> {
                        stringResource(id = R.string.multiplayer_can_read)
                    }
                    else -> EMPTY_STRING_VALUE
                },
                style = Relations3,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        if (config !is ShareSpaceMemberView.Config.Member.Owner) {
            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_action_more),
                    contentDescription = "Menu button",
                    modifier = Modifier.noRippleClickable { isMemberMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = isMemberMenuExpanded,
                    onDismissRequest = {
                        isMemberMenuExpanded = false
                    },
                    modifier = Modifier.background(
                        color = colorResource(id = R.color.background_secondary)
                    )
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onCanViewClicked().also {
                                isMemberMenuExpanded = false
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.multiplayer_can_read),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.weight(1.0f)
                        )
                    if (config is ShareSpaceMemberView.Config.Member.Reader) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_check),
                            contentDescription = "Checked icon",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            onCanEditClicked().also {
                                isMemberMenuExpanded = false
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.multiplayer_can_edit),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.weight(1.0f)
                        )
                    if (config is ShareSpaceMemberView.Config.Member.Writer) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dropdown_menu_check),
                            contentDescription = "Checked icon",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    }
                    Divider()
                    DropdownMenuItem(
                        onClick = {
                            isMemberMenuExpanded = false
                            onRemoveMemberClicked()
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.multiplayer_remove_member),
                            style = BodyRegular,
                            color = colorResource(id = R.color.palette_dark_red)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun SpaceMemberIcon(
    icon: SpaceMemberIconView,
    modifier: Modifier,
    iconSize: Dp = 48.dp,
    textSize: TextUnit = 28.sp
) {
    when (icon) {
        is SpaceMemberIconView.Placeholder -> {
            Box(
                modifier = modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(color = colorResource(id = R.color.text_tertiary))
            ) {
                Text(
                    text = icon
                        .name
                        .ifEmpty { stringResource(id = R.string.u) }
                        .take(1)
                        .uppercase(),
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        fontSize = textSize,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text_white)
                    )
                )
            }
        }

        is SpaceMemberIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(icon.url),
                contentDescription = "Icon from URI",
                modifier = modifier
                    .size(iconSize)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun SpaceMemberRequest(
    member: ObjectWrapper.Participant,
    icon: SpaceMemberIconView,
    request: ShareSpaceMemberView.Config.Request,
    onViewRequestClicked: () -> Unit,
    onApproveUnjoinRequestClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        SpaceMemberIcon(
            icon = icon,
            modifier = Modifier.align(Alignment.CenterVertically)
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
                    onClick = throttledClick(
                        onClick = { onViewRequestClicked() }
                    ),
                    size = ButtonSize.Small,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            ShareSpaceMemberView.Config.Request.Unjoin -> {
                ButtonSecondary(
                    text = stringResource(R.string.multiplayer_approve_request),
                    onClick = throttledClick(
                        onClick = { onApproveUnjoinRequestClicked() }
                    ),
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
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        request = ShareSpaceMemberView.Config.Request.Join,
        onApproveUnjoinRequestClicked = {},
        onViewRequestClicked = {}
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
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        request = ShareSpaceMemberView.Config.Request.Unjoin,
        onApproveUnjoinRequestClicked = {},
        onViewRequestClicked = {}
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
                    ),
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Konstantin"
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
                    ),
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Evgenii"
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
                    config = ShareSpaceMemberView.Config.Request.Unjoin,
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Aleksey"
                    )
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
                    config = ShareSpaceMemberView.Config.Request.Join,
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Anton"
                    )
                )
            )
        },
        onApproveUnjoinRequestClicked = {},
        onViewRequestClicked = {},
        onRemoveMemberClicked = {},
        onCanViewClicked = {},
        onCanEditClicked = {}
    )
}

@Composable
@Preview
private fun SpaceOwnerMemberPreview() {
    SpaceMember(
        member = ObjectWrapper.Participant(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Evgenii"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Evgenii"),
        config = ShareSpaceMemberView.Config.Member.Owner,
        onCanEditClicked = {},
        onCanViewClicked = {},
        onRemoveMemberClicked = {}
    )
}

@Composable
@Preview
private fun SpaceEditorMemberPreview() {
    SpaceMember(
        member = ObjectWrapper.Participant(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Evgenii"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Evgenii"),
        config = ShareSpaceMemberView.Config.Member.Writer,
        onCanEditClicked = {},
        onCanViewClicked = {},
        onRemoveMemberClicked = {}
    )
}