package com.anytypeio.anytype.core_ui.features.multiplayer

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonUpgrade
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceMemberView
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel.ShareLinkViewState
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShareSpaceScreen(
    isLoadingInProgress: Boolean,
    spaceAccessType: SpaceAccessType?,
    isCurrentUserOwner: Boolean,
    members: List<ShareSpaceMemberView>,
    shareLinkViewState: ShareLinkViewState,
    incentiveState: ShareSpaceViewModel.ShareSpaceIncentiveState,
    onGenerateInviteLinkClicked: () -> Unit,
    onShareInviteLinkClicked: () -> Unit,
    onViewRequestClicked: (ShareSpaceMemberView) -> Unit,
    onCanViewClicked: (ShareSpaceMemberView) -> Unit,
    onCanEditClicked: (ShareSpaceMemberView) -> Unit,
    onRemoveMemberClicked: (ShareSpaceMemberView) -> Unit,
    onStopSharingClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit,
    onShareQrCodeClicked: () -> Unit,
    onDeleteLinkClicked: () -> Unit,
    onIncentiveClicked: () -> Unit,
    onMemberClicked: (ObjectWrapper.SpaceMember) -> Unit
) {
    val nestedScrollInteropConnection = rememberNestedScrollInteropConnection()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollInteropConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Dragger(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 6.dp)
                )
            }
            var isMenuExpanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isCurrentUserOwner) {
                    Toolbar(title = stringResource(R.string.multiplayer_sharing))
                } else {
                    Toolbar(title = stringResource(R.string.multiplayer_members))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    if (isCurrentUserOwner) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_action_more),
                            contentDescription = "Menu button",
                            modifier = Modifier.noRippleClickable {
                                isMenuExpanded = true
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = {
                            isMenuExpanded = false
                        },
                        modifier = Modifier.background(
                            color = colorResource(id = R.color.background_secondary)
                        )
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onMoreInfoClicked()
                                isMenuExpanded = false
                            }
                        ) {
                            Text(
                                text = stringResource(id = R.string.multiplayer_more_info),
                                style = BodyRegular,
                                color = colorResource(id = R.color.text_primary),
                                modifier = Modifier.weight(1.0f)
                            )
                        }
                        if (spaceAccessType == SpaceAccessType.SHARED && isCurrentUserOwner) {
                            Divider(
                                paddingStart = 0.dp,
                                paddingEnd = 0.dp
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onStopSharingClicked()
                                    isMenuExpanded = false
                                }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.multiplayer_space_stop_sharing),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.palette_dark_red),
                                    modifier = Modifier.weight(1.0f)
                                )
                            }
                        }
                    }
                }
            }
            Section(
                title = stringResource(R.string.multiplayer_members_and_requests)
            )
            Incentive(
                incentiveState = incentiveState,
                onIncentiveClicked = onIncentiveClicked
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                members.forEachIndexed { index, member ->
                    item {
                        when (val config = member.config) {
                            is ShareSpaceMemberView.Config.Member -> {
                                SpaceMember(
                                    member = member.obj,
                                    isCurrentUserOwner = isCurrentUserOwner,
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
                                    icon = member.icon,
                                    canEditEnabled = member.canEditEnabled,
                                    canReadEnabled = member.canReadEnabled,
                                    isUser = member.isUser,
                                    onMemberClicked = onMemberClicked
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
                                    isUser = member.isUser
                                )
                            }
                        }
                        if (index != members.lastIndex) {
                            Divider()
                        }
                    }
                }
                if (members.size > 2) {
                    // Workaround adding footer to prevent content invisible behind link card
                    item {
                        Spacer(modifier = Modifier.height(150.dp))
                    }
                }
            }
        }

        val density = LocalDensity.current
        val draggedDownAnchorTop = with(density) { 250.dp.toPx() }

        val anchors = DraggableAnchors {
            DragValue.DRAGGED_DOWN at draggedDownAnchorTop
            DragValue.DRAGGED_UP at 0f
        }

        val decayAnimation = rememberSplineBasedDecay<Float>()

        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = DragValue.DRAGGED_UP,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                snapAnimationSpec = tween(),
                decayAnimationSpec = decayAnimation
            )
        }
        val offset =
            if (anchoredDraggableState.offset.isNaN()) 0 else anchoredDraggableState.offset.toInt()
        SideEffect {
            anchoredDraggableState.updateAnchors(anchors)
        }
        AnimatedVisibility(
            visible = shareLinkViewState is ShareLinkViewState.Shared,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Box(modifier = Modifier
                .padding(16.dp)
                .offset {
                    IntOffset(x = 0, y = offset)
                }
                .anchoredDraggable(anchoredDraggableState, Orientation.Vertical)
            ) {
                if (shareLinkViewState is ShareLinkViewState.Shared) {
                    ShareInviteLinkCard(
                        link = shareLinkViewState.link,
                        onShareInviteClicked = onShareInviteLinkClicked,
                        onDeleteLinkClicked = onDeleteLinkClicked,
                        onShowQrCodeClicked = onShareQrCodeClicked,
                        isCurrentUserOwner = isCurrentUserOwner
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = shareLinkViewState is ShareLinkViewState.NotGenerated,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                GenerateInviteLinkCard(
                    onGenerateInviteLinkClicked = onGenerateInviteLinkClicked
                )
            }
        }
        val loadingAlpha by animateFloatAsState(targetValue = if (isLoadingInProgress) 1f else 0f)

        if (isLoadingInProgress) {
            DotsLoadingIndicator(
                animating = true,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha }.align(Alignment.Center),
                animationSpecs = FadeAnimationSpecs(itemCount = 3),
                color = colorResource(id = R.color.text_primary),
                size = ButtonSize.Large
            )
        }
    }
}

@Composable
private fun Incentive(
    incentiveState: ShareSpaceViewModel.ShareSpaceIncentiveState,
    onIncentiveClicked: () -> Unit
) {
    when (incentiveState) {
        is ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceReaders -> {
            Text(
                modifier = Modifier
                    .padding(horizontal = 20.dp),
                text = stringResource(id = R.string.multiplayer_cant_add_members),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_primary)
            )
            ButtonUpgrade(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                    .height(36.dp)
                    .verticalScroll(rememberScrollState()),
                onClick = onIncentiveClicked,
                text = stringResource(id = R.string.multiplayer_upgrade_button)
            )
        }

        ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceEditors -> {
            Text(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                text = stringResource(id = R.string.multiplayer_cant_add_editors),
                style = Caption1Regular,
                color = colorResource(id = R.color.text_primary)
            )
            ButtonUpgrade(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                    .height(36.dp)
                    .verticalScroll(rememberScrollState()),
                onClick = onIncentiveClicked,
                text = stringResource(id = R.string.multiplayer_upgrade_button)
            )
        }

        ShareSpaceViewModel.ShareSpaceIncentiveState.Hidden -> {
            //show nothing
        }
    }
}

enum class DragValue { DRAGGED_DOWN, DRAGGED_UP }

@Composable
private fun SpaceMember(
    isUser: Boolean,
    isCurrentUserOwner: Boolean,
    member: ObjectWrapper.SpaceMember,
    icon: SpaceMemberIconView,
    config: ShareSpaceMemberView.Config.Member,
    onCanEditClicked: () -> Unit,
    onCanViewClicked: () -> Unit,
    onMemberClicked: (ObjectWrapper.SpaceMember) -> Unit,
    onRemoveMemberClicked: () -> Unit,
    canEditEnabled: Boolean,
    canReadEnabled: Boolean
) {
    var isMemberMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .noRippleThrottledClickable{ onMemberClicked(member) }
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
            Row {
                Text(
                    text = member.name.orEmpty().ifEmpty { stringResource(id = R.string.untitled) },
                    style = PreviewTitle2Medium,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isUser) {
                    Spacer(modifier = Modifier.width(4.dp))
                    val youAsMemberText = stringResource(id = R.string.multiplayer_you_as_member)
                    Text(
                        text = "($youAsMemberText)",
                        style = PreviewTitle2Medium,
                        color = colorResource(id = R.color.text_secondary),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (config) {
                    ShareSpaceMemberView.Config.Member.Writer -> {
                        stringResource(id = R.string.multiplayer_can_edit)
                    }

                    ShareSpaceMemberView.Config.Member.Owner -> {
                        stringResource(id = R.string.multiplayer_owner)
                    }

                    ShareSpaceMemberView.Config.Member.Reader -> {
                        stringResource(id = R.string.multiplayer_can_view)
                    }

                    else -> EMPTY_STRING_VALUE
                },
                style = Relations3,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        if (isCurrentUserOwner && config !is ShareSpaceMemberView.Config.Member.Owner) {
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
                        modifier = Modifier.alpha(if (canReadEnabled) 1.0f else 0.3f),
                        onClick = {
                            onCanViewClicked().also {
                                isMemberMenuExpanded = false
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.multiplayer_can_view),
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
                        modifier = Modifier.alpha(if (canEditEnabled) 1.0f else 0.3f),
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
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun SpaceMemberRequest(
    member: ObjectWrapper.SpaceMember,
    isUser: Boolean,
    icon: SpaceMemberIconView,
    request: ShareSpaceMemberView.Config.Request,
    onViewRequestClicked: () -> Unit
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
            Row {
                Text(
                    text = member.name.orEmpty().ifEmpty { stringResource(id = R.string.untitled) },
                    style = PreviewTitle2Medium,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isUser) {
                    Spacer(modifier = Modifier.width(4.dp))
                    val youAsMemberText = stringResource(id = R.string.multiplayer_you_as_member)
                    Text(
                        text = "($youAsMemberText)",
                        style = PreviewTitle2Medium,
                        color = colorResource(id = R.color.text_secondary),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            val color = when (request) {
                ShareSpaceMemberView.Config.Request.Join -> ThemeColor.PINK
                ShareSpaceMemberView.Config.Request.Leave -> ThemeColor.RED
            }
            val text = when (request) {
                ShareSpaceMemberView.Config.Request.Join -> stringResource(
                    id = R.string.multiplayer_join_request
                )

                ShareSpaceMemberView.Config.Request.Leave -> stringResource(
                    id = R.string.multiplayer_leave_request
                )
            }
            Text(
                text = text,
                color = colorResource(id = R.color.text_label_inversion),
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = colorResource(id = R.color.background_multiplayer_request),
                        shape = RoundedCornerShape(size = 3.dp)
                    )
                    .padding(start = 6.dp, end = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Relations1
            )
        }
        when (request) {
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

            ShareSpaceMemberView.Config.Request.Leave -> {
                ButtonSecondary(
                    text = stringResource(R.string.multiplayer_approve_request),
                    onClick = throttledClick(
                        onClick = { }
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
        member = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to "Konstantin",
                Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        request = ShareSpaceMemberView.Config.Request.Join,
        onViewRequestClicked = {},
        isUser = false
    )
}

@Composable
@Preview
fun SpaceJoinLongTitleRequestPreview() {
    SpaceMemberRequest(
        member = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to stringResource(id = R.string.default_text_placeholder),
                Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        request = ShareSpaceMemberView.Config.Request.Join,
        onViewRequestClicked = {},
        isUser = false
    )
}

@Composable
@Preview
fun SpaceLeaveRequestPreview() {
    SpaceMemberRequest(
        member = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "1",
                Relations.NAME to "Konstantin",
                Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Konstantin"),
        request = ShareSpaceMemberView.Config.Request.Leave,
        onViewRequestClicked = {},
        isUser = true
    )
}

@Composable
@Preview(
    backgroundColor = 0xFFFFFFFF,
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
fun ShareSpaceScreenPreview() {
    ShareSpaceScreen(
        shareLinkViewState = ShareSpaceViewModel.ShareLinkViewState.Shared(
            link = "https://anytype.io/ibafyrfhfsag6rea3ifffsasssg..."
        ),
        onShareInviteLinkClicked = {},
        members = buildList {
            add(
                ShareSpaceMemberView(
                    obj = ObjectWrapper.SpaceMember(
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
                    obj = ObjectWrapper.SpaceMember(
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
                    obj = ObjectWrapper.SpaceMember(
                        mapOf(
                            Relations.ID to "2",
                            Relations.NAME to "Aleksey"
                        )
                    ),
                    config = ShareSpaceMemberView.Config.Request.Leave,
                    icon = SpaceMemberIconView.Placeholder(
                        name = "Aleksey"
                    )
                )
            )
            add(
                ShareSpaceMemberView(
                    obj = ObjectWrapper.SpaceMember(
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
        onViewRequestClicked = {},
        onRemoveMemberClicked = {},
        onCanViewClicked = {},
        onCanEditClicked = {},
        isCurrentUserOwner = true,
        onStopSharingClicked = {},
        onGenerateInviteLinkClicked = {},
        onMoreInfoClicked = {},
        onShareQrCodeClicked = {},
        onDeleteLinkClicked = {},
        spaceAccessType = null,
        incentiveState = ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceReaders,
        onIncentiveClicked = {},
        isLoadingInProgress = false,
        onMemberClicked = {}
    )
}

@Composable
@Preview
private fun SpaceOwnerMemberPreview() {
    SpaceMember(
        member = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Evgenii"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Evgenii"),
        config = ShareSpaceMemberView.Config.Member.Owner,
        onCanEditClicked = {},
        onCanViewClicked = {},
        onRemoveMemberClicked = {},
        isCurrentUserOwner = true,
        canEditEnabled = true,
        canReadEnabled = true,
        isUser = true,
        onMemberClicked = {}
    )
}

@Composable
@Preview
private fun SpaceEditorMemberPreview() {
    SpaceMember(
        member = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Evgenii"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Evgenii"),
        config = ShareSpaceMemberView.Config.Member.Writer,
        onCanEditClicked = {},
        onCanViewClicked = {},
        onRemoveMemberClicked = {},
        isCurrentUserOwner = true,
        canReadEnabled = true,
        canEditEnabled = true,
        isUser = true,
        onMemberClicked = {}
    )
}

@Composable
@Preview
private fun SpaceMemberLongNamePreview() {
    SpaceMember(
        member = ObjectWrapper.SpaceMember(
            mapOf(
                Relations.ID to "2",
                Relations.NAME to "Walter Walter Walter Walter Walter Walter"
            )
        ),
        icon = SpaceMemberIconView.Placeholder(name = "Evgenii"),
        config = ShareSpaceMemberView.Config.Member.Writer,
        onCanEditClicked = {},
        onCanViewClicked = {},
        onRemoveMemberClicked = {},
        isCurrentUserOwner = true,
        canReadEnabled = true,
        canEditEnabled = true,
        isUser = true,
        onMemberClicked = {}
    )
}