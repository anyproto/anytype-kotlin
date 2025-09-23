package com.anytypeio.anytype.ui.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ModalTitle
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import timber.log.Timber

@Composable
fun HomeScreenToolbar(
    modifier: Modifier = Modifier,
    spaceIconView: SpaceIconView,
    onSpaceIconClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    name: String,
    membersCount: Int
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(56.dp)
                .align(Alignment.CenterStart)
                .noRippleClickable {
                    onBackButtonClicked()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = "Back button",
                modifier = Modifier
            )
        }

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
            style = Title2,
            color = colorResource(R.color.text_primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 104.dp,
                    top = 8.dp,
                    end = 56.dp
                )
                .noRippleClickable {
                    onSettingsClicked()
                }
            ,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val context = LocalContext.current
        val locale = context.resources.configuration.locales[0]
        val text = if (locale != null && membersCount > 0) {
            pluralStringResource(
                id = R.plurals.multiplayer_number_of_space_members,
                membersCount,
                membersCount,
                membersCount
            )
        } else {
            if (locale == null) {
                Timber.e("Error getting the locale")
            }
            stringResource(id = R.string.three_dots_text_placeholder)
        }

        Text(
            text = text,
            style = Relations2,
            color = colorResource(R.color.control_transparent_secondary),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 104.dp,
                    bottom = 8.dp
                )
                .noRippleClickable {
                    onSettingsClicked()
                }
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(56.dp)
                .align(Alignment.CenterEnd)
                .noRippleClickable {
                    onSettingsClicked()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_vault_settings),
                contentDescription = "Settings icon",
                modifier = Modifier
            )
        }
    }
}

@DefaultPreviews
@Composable
fun HomeScreenToolbarPreview() {
    HomeScreenToolbar(
        spaceIconView = SpaceIconView.DataSpace.Placeholder(name = "A"),
        onSpaceIconClicked = {},
        membersCount = 74,
        name = "Test space",
        onBackButtonClicked = {},
        onSettingsClicked = {}
    )
}
