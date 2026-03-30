package com.anytypeio.anytype.ui.spaces

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceMemberIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.CreateSpaceViewModel.SpaceMemberView


@Composable
fun CreateSpaceScreen(
    spaceIconView: SpaceIconView,
    selectedMembers: List<SpaceMemberView> = emptyList(),
    onCreate: (Name) -> Unit,
    onBackClicked: () -> Unit,
    onSpaceIconUploadClicked: () -> Unit,
    onSpaceIconRemoveClicked: () -> Unit,
    isLoading: State<Boolean>
) {
    var innerValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(16.dp)
            )

    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        CreateChannelTopBar(
            onBackClick = onBackClicked,
            onCreateClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onCreate(innerValue.text)
            },
            isLoading = isLoading.value
        )
        Spacer(modifier = Modifier.height(8.dp))
        SpaceIcon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            spaceIconView = when (spaceIconView) {
                is SpaceIconView.DataSpace.Placeholder -> spaceIconView.copy(
                    name = innerValue.text.ifEmpty {
                        stringResource(id = R.string.u)
                    }
                )
                else -> spaceIconView
            },
            onSpaceIconUploadClicked = onSpaceIconUploadClicked,
            onSpaceIconRemoveClicked = onSpaceIconRemoveClicked,
            isLoading = isLoading.value
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
        ) {
            OutlinedTextField(
                value = innerValue,
                onValueChange = {
                    innerValue = it
                },
                textStyle = BodyRegular,
                singleLine = true,
                enabled = !isLoading.value,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = colorResource(id = R.color.text_primary),
                    disabledTextColor = colorResource(id = R.color.text_tertiary),
                    cursorColor = colorResource(id = R.color.color_accent),
                    focusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                    unfocusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                    errorContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                    disabledContainerColor = colorResource(id = R.color.shape_transparent_tertiary),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 0.dp, top = 12.dp)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                shape = RoundedCornerShape(size = 20.dp),
                placeholder = {
                    Text(
                        modifier = Modifier.padding(start = 1.dp),
                        text = stringResource(id = R.string.untitled),
                        style = BodySemiBold,
                        color = colorResource(id = R.color.text_tertiary)
                    )
                }
            )
        }
        if (selectedMembers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Section(
                title = stringResource(id = com.anytypeio.anytype.localization.R.string.multiplayer_members),
                textPaddingStart = 16.dp
            )
            selectedMembers.forEach { member ->
                SelectedMemberRow(member = member)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun CreateChannelTopBar(
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Image(
            painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ic_back_24),
            contentDescription = "Back",
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterStart)
                .noRippleThrottledClickable { onBackClick() },
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary))
        )

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.create_channel),
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            ButtonOnboardingPrimaryLarge(
                text = stringResource(id = R.string.create),
                onClick = onCreateClick,
                loading = isLoading,
                size = ButtonSize.Small,
            )
        }
    }
}

@Composable
fun SpaceIcon(
    modifier: Modifier,
    spaceIconView: SpaceIconView,
    onSpaceIconUploadClicked: () -> Unit,
    onSpaceIconRemoveClicked: () -> Unit,
    isLoading: Boolean = false
) {
    val context = LocalContext.current

    val isIconMenuExpanded = remember {
        mutableStateOf(false)
    }

    Box(modifier = modifier.wrapContentSize()) {
        SpaceIconView(
            icon = spaceIconView,
            modifier = Modifier.clickable(enabled = !isLoading) {
                isIconMenuExpanded.value = !isIconMenuExpanded.value
            }
        )
        // Edit badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 6.dp)
                .size(32.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .background(
                    color = colorResource(id = R.color.shape_secondary),
                    shape = CircleShape
                )
                .clickable(enabled = !isLoading) {
                    isIconMenuExpanded.value = !isIconMenuExpanded.value
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_edit_24),
                contentDescription = null,
                contentScale = ContentScale.Inside,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary))
            )
        }
        DropdownMenu(
            modifier = Modifier,
            expanded = isIconMenuExpanded.value,
            offset = DpOffset(x = 0.dp, y = 6.dp),
            onDismissRequest = {
                isIconMenuExpanded.value = false
            },
            shape = RoundedCornerShape(10.dp),
            containerColor = colorResource(id = R.color.background_secondary)
        ) {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                DropdownMenuItem(
                    onClick = {
                        onSpaceIconUploadClicked()
                        isIconMenuExpanded.value = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.profile_settings_apply_upload_image),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
            if (spaceIconView is SpaceIconView.DataSpace.Image) {
                Divider(
                    paddingStart = 0.dp,
                    paddingEnd = 0.dp,
                )
                DropdownMenuItem(
                    onClick = {
                        isIconMenuExpanded.value = false
                        onSpaceIconRemoveClicked()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.remove_image),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
        }
    }
}

@Composable
fun Section(
    title: String,
    color: Color = colorResource(id = R.color.text_secondary),
    textPaddingStart: Dp = 20.dp
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(
                    start = textPaddingStart,
                    bottom = 8.dp
                )
                .align(Alignment.BottomStart),
            text = title,
            color = color,
            style = Caption1Regular
        )
    }
}

@Composable
private fun SelectedMemberRow(member: SpaceMemberView) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceMemberIcon(
            icon = member.icon,
            modifier = Modifier,
            iconSize = 48.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = member.name,
                style = Title2,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = member.identity,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@DefaultPreviews
@Composable
fun CreateSpaceScreenPreview() {
    val state = remember { mutableStateOf(false) }
    CreateSpaceScreen(
        spaceIconView = SpaceIconView.DataSpace.Placeholder(
            color = SystemColor.RED,
            name = "My Space"
        ),
        onCreate = { },
        onBackClicked = {},
        onSpaceIconUploadClicked = {},
        onSpaceIconRemoveClicked = {},
        isLoading = state,
        selectedMembers = listOf(
            SpaceMemberView(
                identity = "1",
                name = "Alice Johnson",
                icon = SpaceMemberIconView.Placeholder("V")
            ),
            SpaceMemberView(
                identity = "2fj89dushflhsdiofhjisudhfiuadshfhsdjkhfahsdufnuisdhfhjsdhfjhsdjkafhkjsdh",
                name = "Bob Smith",
                icon = SpaceMemberIconView.Placeholder("F")
            ),
            SpaceMemberView(
                identity = "3",
                name = "Charlie Davis",
                icon = SpaceMemberIconView.Placeholder("P")
            )
        )
    )
}