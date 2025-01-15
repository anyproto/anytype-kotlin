package com.anytypeio.anytype.ui.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun HomeScreenToolbar(
    spaceIconView: SpaceIconView,
    onSpaceIconClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    name: String,
    membersCount: Int
) {
    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .height(52.dp)
    ) {

        Image(
            painter = painterResource(R.drawable.ic_home_top_toolbar_back),
            contentDescription = "Back button",
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart)
                .noRippleClickable {
                    onBackButtonClicked()
                }
        )

        SpaceIconView(
            modifier = Modifier
                .padding(start = 60.dp)
                .align(Alignment.CenterStart),
            icon = spaceIconView,
            onSpaceIconClick = {
                onSpaceIconClicked()
            },
            mainSize = 32.dp
        )

        Text(
            text = name.ifEmpty { stringResource(R.string.untitled) },
            style = PreviewTitle2Medium,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(
                start = 104.dp,
                top = 8.dp,
                end = 56.dp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = if (membersCount > 0 ) {
                pluralStringResource(
                    id = R.plurals.multiplayer_number_of_space_members,
                    membersCount,
                    membersCount,
                    membersCount
                )
            } else
                stringResource(id = R.string.three_dots_text_placeholder),
            style = Relations2,
            color = colorResource(R.color.text_secondary),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 104.dp,
                    bottom = 8.dp
                )
        )

        Image(
            painter = painterResource(id = com.anytypeio.anytype.R.drawable.ic_vault_settings),
            contentDescription = "Settings icon",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .noRippleClickable {
                    onSettingsClicked()
                }
        )
    }
}

@DefaultPreviews
@Composable
fun HomeScreenToolbarPreview() {
    HomeScreenToolbar(
        spaceIconView = SpaceIconView.Placeholder(name = "A"),
        onSpaceIconClicked = {},
        membersCount = 74,
        name = "Test space",
        onBackButtonClicked = {},
        onSettingsClicked = {}
    )
}
