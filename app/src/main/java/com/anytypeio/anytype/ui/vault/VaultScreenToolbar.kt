package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon.Profile.Avatar
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.presentation.vault.VaultUiState

@Composable
fun VaultScreenTopToolbar(
    searchQuery: String,
    profile: AccountProfile,
    uiState: VaultUiState,
    showNotificationBadge: Boolean = false,
    onUpdateSearchQuery: (String) -> Unit,
    onCreateSpaceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    when (uiState) {
        VaultUiState.Loading -> {
            Column {
                VaultScreenToolbar(
                    profile = profile,
                    showNotificationBadge = showNotificationBadge,
                    onPlusClicked = onCreateSpaceClicked,
                    onSettingsClicked = onSettingsClicked,
                    spaceCountLimitReached = false,
                    isLoading = true
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }

        is VaultUiState.Sections -> {
            Column {
                VaultScreenToolbar(
                    profile = profile,
                    showNotificationBadge = showNotificationBadge,
                    onPlusClicked = onCreateSpaceClicked,
                    onSettingsClicked = onSettingsClicked,
                    spaceCountLimitReached = uiState.mainSpaces.size >= SelectSpaceViewModel.MAX_SPACE_COUNT,
                    isLoading = false
                )
                DefaultSearchBar(
                    value = searchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    onQueryChanged = onUpdateSearchQuery
                )
            }
        }
    }
}

@Composable
fun VaultScreenToolbar(
    profile: AccountProfile,
    spaceCountLimitReached: Boolean = false,
    showNotificationBadge: Boolean = false,
    onPlusClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    isLoading: Boolean
) {

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .noRippleThrottledClickable {
                        onSettingsClicked()
                    },
                contentAlignment = Alignment.Center

            ) {
                ProfileIconWithBadge(
                    modifier = Modifier.size(34.dp),
                    profile = profile,
                    showBadge = showNotificationBadge
                )
            }

            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
            ) {
                val (t, l) = createRefs()
                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .constrainAs(t) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    style = Title1,
                    text = stringResource(R.string.vault_my_spaces),
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 7.dp)
                            .size(16.dp)
                            .constrainAs(l) {
                                end.linkTo(t.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            },
                        color = colorResource(id = R.color.glyph_active),
                        strokeWidth = 2.dp
                    )
                }
            }

            if (!spaceCountLimitReached) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(64.dp)
                        .fillMaxHeight()
                        .noRippleThrottledClickable {
                            onPlusClicked()
                        },
                    contentAlignment = Alignment.Center

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_plus_18),
                        contentDescription = stringResource(R.string.content_description_plus_button),
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileIcon(
    modifier: Modifier,
    profile: AccountProfile
) {
    when (profile) {
        is AccountProfile.Data -> {
            Box(modifier) {
                when (val icon = profile.icon) {
                    is ProfileIconView.Image -> {
                        Image(
                            painter = rememberAsyncImagePainter(icon.url),
                            contentDescription = "Custom image profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    else -> {
                        val nameFirstChar = if (profile.name.isEmpty()) {
                            stringResource(id = R.string.account_default_name)
                        } else {
                            profile.name.first().uppercaseChar().toString()
                        }
                        ListWidgetObjectIcon(
                            modifier = Modifier.fillMaxSize(),
                            icon = Avatar(
                                name = nameFirstChar
                            ),
                            iconSize = 28.dp
                        )
                    }
                }
            }
        }

        AccountProfile.Idle -> {
            // Draw nothing
        }
    }
}

@Composable
private fun ProfileIconWithBadge(
    modifier: Modifier,
    profile: AccountProfile,
    showBadge: Boolean = false
) {
    Box(
        modifier = modifier
    ) {
        // Main profile icon
        ProfileIcon(
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center),
            profile = profile
        )

        // Badge positioned in top-right corner
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .background(
                        color = colorResource(id = R.color.background_primary),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = colorResource(id = R.color.palette_system_red),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Not Scrolled"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Not Scrolled"
)
fun VaultScreenToolbarNotScrolledPreview() {
    VaultScreenToolbar(
        onPlusClicked = {},
        onSettingsClicked = {},
        profile = AccountProfile.Data(
            name = "John Doe",
            icon = ProfileIconView.Placeholder(name = "Jd")
        ),
        isLoading = false,
    )
}

@Composable
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode - Scrolled"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode - Scrolled"
)
fun VaultScreenToolbarScrolledPreview() {
    VaultScreenToolbar(
        onPlusClicked = {},
        onSettingsClicked = {},
        profile = AccountProfile.Data(
            name = "John Doe",
            icon = ProfileIconView.Placeholder(name = "Jd")
        ),
        isLoading = false,
        showNotificationBadge = true
    )
}