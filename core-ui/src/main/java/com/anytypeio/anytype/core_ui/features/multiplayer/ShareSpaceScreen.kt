package com.anytypeio.anytype.core_ui.features.multiplayer

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_PRIMARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
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
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShareSpaceScreen(
    isLoadingInProgress: Boolean,
    isCurrentUserOwner: Boolean,
    members: List<ShareSpaceMemberView>,
    incentiveState: ShareSpaceViewModel.ShareSpaceIncentiveState,
    inviteLinkAccessLevel: SpaceInviteLinkAccessLevel,
    inviteLinkAccessLoading: Boolean,
    confirmationDialogLevel: SpaceInviteLinkAccessLevel?,
    onViewRequestClicked: (ShareSpaceMemberView) -> Unit,
    onCanViewClicked: (ShareSpaceMemberView) -> Unit,
    onCanEditClicked: (ShareSpaceMemberView) -> Unit,
    onRemoveMemberClicked: (ShareSpaceMemberView) -> Unit,
    onIncentiveClicked: () -> Unit,
    onMemberClicked: (ObjectWrapper.SpaceMember) -> Unit,

    onInviteLinkAccessLevelSelected: (SpaceInviteLinkAccessLevel) -> Unit,
    onInviteLinkAccessChangeConfirmed: () -> Unit,
    onInviteLinkAccessChangeCancel: () -> Unit,

    onShareInviteLinkClicked: (String) -> Unit,
    onCopyInviteLinkClicked: (String) -> Unit,
    onShareQrCodeClicked: (String) -> Unit
) {
    val nestedScrollInteropConnection = rememberNestedScrollInteropConnection()
    var showInviteLinkAccessSelector by remember(false) { mutableStateOf(false) }
    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Toolbar(title = stringResource(R.string.multiplayer_members))
            }
            Section(
                title = stringResource(R.string.multiplayer_members_invite_links_section)
            )
            val item = inviteLinkAccessLevel.getInviteLinkItemParams()
            AccessLevelOption(
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleThrottledClickable {
                        showInviteLinkAccessSelector = !showInviteLinkAccessSelector
                    },
                uiItemUI = item
            )
            
            // Show invite link and copy button when not LINK_DISABLED
            when (inviteLinkAccessLevel) {
                is SpaceInviteLinkAccessLevel.EditorAccess -> InviteLinkDisplay(
                    link = inviteLinkAccessLevel.link,
                    onCopyClicked = onCopyInviteLinkClicked,
                    onShareClicked = onShareInviteLinkClicked,
                    onQrCodeClicked = onShareQrCodeClicked
                )
                is SpaceInviteLinkAccessLevel.RequestAccess -> InviteLinkDisplay(
                    link = inviteLinkAccessLevel.link,
                    onCopyClicked = onCopyInviteLinkClicked,
                    onShareClicked = onShareInviteLinkClicked,
                    onQrCodeClicked = onShareQrCodeClicked
                )
                is SpaceInviteLinkAccessLevel.ViewerAccess -> InviteLinkDisplay(
                    link = inviteLinkAccessLevel.link,
                    onCopyClicked = onCopyInviteLinkClicked,
                    onShareClicked = onShareInviteLinkClicked,
                    onQrCodeClicked = onShareQrCodeClicked
                )
                SpaceInviteLinkAccessLevel.LinkDisabled -> {}
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
        SideEffect {
            anchoredDraggableState.updateAnchors(anchors)
        }
        //Invite Link Access Selector
        if (showInviteLinkAccessSelector) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showInviteLinkAccessSelector = false },
                dragHandle = null,
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
            ) {
                InviteLinkAccessSelector(
                    currentAccessLevel = inviteLinkAccessLevel,
                    onAccessLevelChanged = {
                        showInviteLinkAccessSelector = false
                        onInviteLinkAccessLevelSelected(it)
                    }
                )
            }
        }
        val loadingAlpha by animateFloatAsState(targetValue = if (isLoadingInProgress) 1f else 0f)

        if (isLoadingInProgress) {
            DotsLoadingIndicator(
                animating = true,
                modifier = Modifier
                    .graphicsLayer { alpha = loadingAlpha }
                    .align(Alignment.Center),
                animationSpecs = FadeAnimationSpecs(itemCount = 3),
                color = colorResource(id = R.color.text_primary),
                size = ButtonSize.Large
            )
        }
        
        // Confirmation dialog for invite link access changes
        if (confirmationDialogLevel != null) {
            showConfirmScreen(
                onInviteLinkAccessChangeConfirmed = onInviteLinkAccessChangeConfirmed,
                onInviteLinkAccessChangeCancel = onInviteLinkAccessChangeCancel,
                onDismissRequest = onInviteLinkAccessChangeCancel,
                isLoading = inviteLinkAccessLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun showConfirmScreen(
    onInviteLinkAccessChangeConfirmed: () -> Unit,
    onInviteLinkAccessChangeCancel: () -> Unit,
    onDismissRequest: () -> Unit,
    isLoading: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        GenericAlert(
            onFirstButtonClicked = onInviteLinkAccessChangeCancel,
            onSecondButtonClicked = {
                onInviteLinkAccessChangeConfirmed()
            },
            config = AlertConfig.WithTwoButtons(
                title = stringResource(id = R.string.multiplayer_link_will_be_invalidated),
                description = stringResource(R.string.multiplayer_link_will_be_invalidated_desc),
                firstButtonText = stringResource(R.string.cancel),
                secondButtonText = stringResource(R.string.confirm),
                firstButtonType = BUTTON_SECONDARY,
                secondButtonType = BUTTON_PRIMARY,
                icon = R.drawable.ic_popup_alert_56,
                isSecondButtonLoading = isLoading,
            )
        )
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
            .noRippleThrottledClickable { onMemberClicked(member) }
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
fun InviteLinkDisplay(
    modifier: Modifier = Modifier,
    link: String,
    onCopyClicked: (String) -> Unit,
    onShareClicked: (String) -> Unit,
    onQrCodeClicked: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Link text with three dots menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.transparent_tertiary),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = link,
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))
            
            // Three dots menu trigger
            Box {
                Image(
                    painter = painterResource(id = R.drawable.ic_action_more),
                    contentDescription = "More options",
                    modifier = Modifier
                        .size(24.dp)
                        .noRippleClickable {
                            showMenu = true
                        },
                    contentScale = ContentScale.Inside
                )
                
                // Dropdown menu
                DropdownMenu(
                    modifier = Modifier.widthIn(min = 252.dp),
                    containerColor = colorResource(R.color.background_secondary),
                    shape = RoundedCornerShape(12.dp),
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Copy link
                    DropdownMenuItem(
                        onClick = {
                            onCopyClicked(link)
                            showMenu = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.multiplayer_copy_link),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.weight(1.0f)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_object_action_copy_link,),
                            contentDescription = "Copy link icon",
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.CenterVertically),
                            colorFilter = ColorFilter.tint(
                                colorResource(id = R.color.text_primary)
                            )
                        )
                    }
                    
                    Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                    
                    // Share link
                    DropdownMenuItem(
                        onClick = {
                            onShareClicked(link)
                            showMenu = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.multiplayer_share_invite_link_menu),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.weight(1.0f)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_menu_share_link),
                            contentDescription = "Share link icon",
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                    
                    Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                    
                    // Show QR code
                    DropdownMenuItem(
                        onClick = {
                            onQrCodeClicked(link)
                            showMenu = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.multiplayer_qr_invite_link_menu),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.weight(1.0f)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_qr_code_24),
                            contentDescription = "QR code icon",
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.CenterVertically),
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Copy button
        ButtonPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            text = stringResource(R.string.copy_link),
            onClick = {
                onCopyClicked(link)
            },
            size = ButtonSize.Large,
        )
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
        onShareQrCodeClicked = {},
        incentiveState = ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceReaders,
        onIncentiveClicked = {},
        isLoadingInProgress = false,
        onMemberClicked = {},
        inviteLinkAccessLevel = SpaceInviteLinkAccessLevel.EditorAccess("https://example.com/invite"),
        inviteLinkAccessLoading = false,
        confirmationDialogLevel = null,
        onInviteLinkAccessLevelSelected = {},
        onInviteLinkAccessChangeConfirmed = {},
        onInviteLinkAccessChangeCancel = {},
        onCopyInviteLinkClicked = {}
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