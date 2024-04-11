package com.anytypeio.anytype.ui_settings.account

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.foundation.Arrow
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.foundation.OptionMembership
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.ui_settings.BuildConfig
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
    onNameChange: (String) -> Unit,
    onProfileIconClick: () -> Unit,
    account: ProfileSettingsViewModel.AccountProfile,
    onAppearanceClicked: () -> Unit,
    onDataManagementClicked: () -> Unit,
    onAboutClicked: () -> Unit,
    onMembershipClicked: () -> Unit,
    activeTierName: String?
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
                onProfileIconClick = onProfileIconClick
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
            Section(stringResource(R.string.settings))
        }
        item {
            Option(
                image = R.drawable.ic_appearance,
                text = stringResource(R.string.appearance),
                onClick = onAppearanceClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Option(
                image = R.drawable.ic_file_storage,
                text = stringResource(R.string.data_management),
                onClick = onDataManagementClicked
            )
        }
        if (BuildConfig.DEBUG) {
            item {
                Divider(paddingStart = 60.dp)
            }
            item {
                OptionMembership(
                    image = R.drawable.ic_membership,
                    text = stringResource(R.string.settings_membership),
                    onClick = onMembershipClicked,
                    activeTierName = activeTierName
                )
            }
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Option(
                image = R.drawable.ic_about,
                text = stringResource(R.string.about),
                onClick = onAboutClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Section(stringResource(R.string.access))
        }
        item {
            Option(
                image = R.drawable.ic_keychain_phrase,
                text = stringResource(R.string.recovery_phrase),
                onClick = onKeychainPhraseClicked
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            ActionWithProgressBar(
                name = stringResource(R.string.log_out),
                color = colorResource(R.color.palette_dark_red),
                onClick = onLogoutClicked,
                isInProgress = isLogoutInProgress
            )
        }
        item {
            Box(Modifier.height(54.dp))
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
fun Pincode(
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(52.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painterResource(R.drawable.ic_pin_code),
            contentDescription = "Pincode icon",
            modifier = Modifier.padding(
                start = 20.dp
            )
        )
        Text(
            text = stringResource(R.string.pin_code),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(
                start = 12.dp
            )
        )
        Box(
            modifier = Modifier.weight(1.0f, true),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row {
                Text(
                    text = stringResource(R.string.off),
                    fontSize = 17.sp,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.padding(end = 10.dp)
                )
                Arrow()
            }
        }
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
    isInProgress: Boolean
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
    account: ProfileSettingsViewModel.AccountProfile,
    onProfileIconClick: () -> Unit,
    onNameSet: (String) -> Unit
) {
    when (account) {
        is ProfileSettingsViewModel.AccountProfile.Data -> {
            Box(modifier = modifier.padding(vertical = 6.dp)) {
                Dragger()
            }
            Box(modifier = modifier.padding(top = 12.dp, bottom = 28.dp)) {
                ProfileTitleBlock()
            }
            Box(modifier = modifier.padding(bottom = 16.dp)) {
                ProfileImageBlock(
                    name = account.name,
                    icon = account.icon,
                    onProfileIconClick = onProfileIconClick
                )
            }
            ProfileNameBlock(name = account.name, onNameSet = onNameSet)
        }
        is ProfileSettingsViewModel.AccountProfile.Idle -> {}
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
fun ProfileTitleBlock() {
    Text(
        text = stringResource(R.string.profile),
        style = Title1,
        color = colorResource(id = R.color.text_primary)
    )
}


@Composable
fun ProfileImageBlock(
    name: String,
    icon: ProfileIconView,
    onProfileIconClick: () -> Unit
) {
    when (icon) {
        is ProfileIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(48.dp))
                    .noRippleClickable {
                        onProfileIconClick.invoke()
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
}

private const val PROFILE_NAME_CHANGE_DELAY = 300L