package com.anytypeio.anytype.ui_settings.account

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.foundation.OptionMembership
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Arrow
import com.anytypeio.anytype.core_ui.foundation.OptionWithBadge
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.ui_settings.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun ProfileSettingsScreen(
    onKeychainPhraseClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    isLogoutInProgress: Boolean,
    isDebugEnabled: Boolean,
    onNameChange: (String) -> Unit,
    onProfileIconClick: () -> Unit,
    account: AccountProfile,
    onAppearanceClicked: () -> Unit,
    onDataManagementClicked: () -> Unit,
    onMySitesClicked: () -> Unit,
    onAboutClicked: () -> Unit,
    onSpacesClicked: () -> Unit,
    onMembershipClicked: () -> Unit,
    membershipStatus: MembershipStatus?,
    showMembership: ShowMembership?,
    clearProfileImage: () -> Unit,
    onDebugClicked: () -> Unit,
    onHeaderTitleClicked: () -> Unit,
    notificationsDisabled: Boolean,
    onOpenNotificationSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Header(
                account = account,
                onNameSet = onNameChange,
                onProfileIconClick = onProfileIconClick,
                clearProfileImage = clearProfileImage,
                onTitleClicked = onHeaderTitleClicked
            )
        }
        item {
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .padding(top = 4.dp)
            )
        }
        item {
            Divider()
        }
        item {
            Section(stringResource(R.string.settings_application))
        }
        item {
            Option(
                image = R.drawable.ic_settings_appearance,
                text = stringResource(R.string.appearance),
                onClick = onAppearanceClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            OptionWithBadge(
                image = R.drawable.ic_settings_notifications,
                text = stringResource(R.string.notifications_title),
                showBadge = notificationsDisabled,
                onClick = onOpenNotificationSettings
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }

        item {
            Option(
                image = R.drawable.ic_settings_access,
                text = stringResource(R.string.login_key),
                onClick = onKeychainPhraseClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        if (showMembership?.isShowing == true) {
            item {
                OptionMembership(
                    image = R.drawable.ic_settings_membership,
                    text = stringResource(R.string.settings_membership),
                    onClick = onMembershipClicked,
                    membershipStatus = membershipStatus
                )
            }
            item {
                Divider(paddingStart = 60.dp)
            }
        }

        item {
            Section(stringResource(R.string.data_management))
        }

        item {
            Option(
                image = R.drawable.ic_settings_spaces,
                text = stringResource(R.string.multiplayer_spaces),
                onClick = onSpacesClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }

        item {
            Option(
                image = R.drawable.ic_settings_data_management,
                text = stringResource(R.string.local_storage),
                onClick = onDataManagementClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Option(
                image = R.drawable.ic_settings_my_sites,
                text = stringResource(R.string.settings_my_sites),
                onClick = onMySitesClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }

        item {
            Section(stringResource(R.string.vault_settings_section_misc))
        }

        item {
            Option(
                image = R.drawable.ic_settings_about,
                text = stringResource(R.string.about),
                onClick = onAboutClicked
            )
        }
        if (isDebugEnabled) {
            item {
                Divider(paddingStart = 60.dp)
            }
            item {
                Option(
                    image = R.drawable.ic_settings_debug,
                    text = stringResource(R.string.debug),
                    onClick = onDebugClicked
                )
            }
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            LogoutButton(onLogoutClicked, isLogoutInProgress)
        }
        item {
            Box(Modifier.height(54.dp))
        }
    }
}

@Composable
private fun LogoutButton(
    onLogoutClicked: () -> Unit,
    isLogoutInProgress: Boolean
) {
    Row(
        modifier = Modifier
            .height(52.dp)
            .clickable {
                onLogoutClicked()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_settings_log_out),
            contentDescription = "Option icon",
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
        Box(
            modifier = Modifier
                .weight(1.0f)
                .height(52.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(R.string.log_out),
                color = colorResource(R.color.palette_system_red),
                style = BodyRegular,
                modifier = Modifier.padding(
                    start = 10.dp
                )
            )
            if (isLogoutInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                        .size(24.dp),
                    color = colorResource(R.color.shape_secondary)
                )
            }
        }
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterEnd
        ) {
            Arrow()
        }
    }
}

@Composable
fun Section(name: String) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(
                start = 20.dp,
                bottom = 8.dp
            ),
            color = colorResource(R.color.text_secondary),
            style = Caption1Regular
        )
    }
}

@Composable
fun Action(
    name: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = name,
            color = color,
            style = BodyRegular,
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
    }
}

@Composable
fun ActionWithProgressBar(
    name: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {},
    isInProgress: Boolean,
    textStartPadding: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = name,
            color = color,
            style = BodyRegular,
            modifier = Modifier.padding(
                start = textStartPadding
            )
        )
        if (isInProgress) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .size(24.dp),
                color = colorResource(R.color.shape_secondary)
            )
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    account: AccountProfile,
    onProfileIconClick: () -> Unit,
    onNameSet: (String) -> Unit,
    clearProfileImage: () -> Unit,
    onTitleClicked: () -> Unit
) {
    when (account) {
        is AccountProfile.Data -> {
            Box(modifier = modifier.padding(vertical = 6.dp)) {
                Dragger()
            }
            Box(modifier = modifier.padding(top = 12.dp, bottom = 28.dp)) {
                ProfileTitleBlock(onTitleClicked)
            }
            Box(modifier = modifier.padding(bottom = 16.dp)) {
                ProfileImageBlock(
                    name = account.name,
                    icon = account.icon,
                    onProfileIconClick = onProfileIconClick,
                    clearProfileImage = clearProfileImage
                )
            }
            ProfileNameBlock(name = account.name, onNameSet = onNameSet)
        }
        is AccountProfile.Idle -> {}
    }
}

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class)
@Composable
fun ProfileNameBlock(
    modifier: Modifier = Modifier,
    name: String,
    onNameSet: (String) -> Unit
) {

    val nameValue = remember { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(nameValue.value) {
        snapshotFlow { nameValue.value }
            .debounce(PROFILE_NAME_CHANGE_DELAY)
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { query ->
                onNameSet(query)
            }
    }

    Column(modifier = modifier.padding(start = 20.dp)) {
        Text(
            text = stringResource(id = R.string.name),
            color = colorResource(id = R.color.text_secondary),
            fontSize = 13.sp
        )
        BasicTextField(
            value = nameValue.value,
            onValueChange = {
                nameValue.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 20.dp),
            enabled = true,
            textStyle = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.text_primary)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = nameValue.value,
                    innerTextField = innerTextField,
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    placeholder = {
                        Text(text = stringResource(R.string.account_name))
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = colorResource(id = R.color.text_primary),
                        backgroundColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        placeholderColor = colorResource(id = R.color.text_tertiary),
                        cursorColor = colorResource(id = R.color.orange)
                    ),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        top = 0.dp,
                        end = 0.dp,
                        bottom = 0.dp
                    ),
                    border = {},
                    interactionSource = remember { MutableInteractionSource() },
                    visualTransformation = VisualTransformation.None
                )
            }
        )
    }

}

@Composable
fun ProfileTitleBlock(
    onClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.settings),
        style = Title1,
        color = colorResource(id = R.color.text_primary),
        modifier = Modifier.noRippleClickable {
            onClick()
        }
    )
}


@Composable
fun ProfileImageBlock(
    name: String,
    icon: ProfileIconView,
    onProfileIconClick: () -> Unit,
    clearProfileImage: () -> Unit
) {
    val isIconMenuExpanded = remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    when (icon) {
        is ProfileIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(model = icon.url),
                contentDescription = "Custom image profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(48.dp))
                    .noRippleClickable {
                        isIconMenuExpanded.value = !isIconMenuExpanded.value
                    }
            )
        }
        else -> {
            val nameFirstChar = if (name.isEmpty()) {
                stringResource(id = R.string.account_default_name)
            } else {
                name.first().uppercaseChar().toString()
            }
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(48.dp))
                    .background(colorResource(id = R.color.text_tertiary))
                    .noRippleClickable {
                        onProfileIconClick.invoke()
                    }
            ) {
                Text(
                    text = nameFirstChar,
                    style = MaterialTheme.typography.h3.copy(
                        color = colorResource(id = R.color.text_white),
                        fontSize = 64.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(10.dp))
    ) {
        DropdownMenu(
            modifier = Modifier
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = colorResource(id = R.color.background_secondary)),
            expanded = isIconMenuExpanded.value,
            offset = DpOffset(x = 0.dp, y = 6.dp),
            onDismissRequest = {
                isIconMenuExpanded.value = false
            }
        ) {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                DropdownMenuItem(
                    onClick = {
                        onProfileIconClick.invoke()
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
            Divider(
                paddingStart = 0.dp,
                paddingEnd = 0.dp,
            )
            DropdownMenuItem(
                onClick = {
                    isIconMenuExpanded.value = false
                    clearProfileImage.invoke()
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

@DefaultPreviews
@Composable
private fun ProfileSettingPreview() {
    ProfileSettingsScreen(
        onKeychainPhraseClicked = {},
        onLogoutClicked = {},
        isLogoutInProgress = false,
        onNameChange = {},
        onProfileIconClick = {},
        account = AccountProfile.Data(
            "Walter",
            icon = ProfileIconView.Placeholder("Walter")
        ),
        onAppearanceClicked = {},
        onDataManagementClicked = {},
        onAboutClicked = {},
        onSpacesClicked = {},
        onMembershipClicked = {},
        membershipStatus = null,
        showMembership = ShowMembership(true),
        clearProfileImage = {},
        onDebugClicked = {},
        isDebugEnabled = true,
        onHeaderTitleClicked = {},
        notificationsDisabled = true,
        onOpenNotificationSettings = {},
        onMySitesClicked = {}
    )
}

private const val PROFILE_NAME_CHANGE_DELAY = 300L