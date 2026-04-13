package com.anytypeio.anytype.ui.home


import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable

@Composable
fun HomeScreenToolbar(
    modifier: Modifier = Modifier,
    onBackButtonClicked: () -> Unit,
    onSpaceSettingsClicked: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.nav_top_toolbar_height))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircularIconButton(
            iconRes = R.drawable.ic_default_top_back,
            onClick = onBackButtonClicked,
            contentDescription = stringResource(R.string.content_desc_back_button)
        )
        CircularIconButton(
            iconRes = R.drawable.ic_space_settings_24,
            onClick = onSpaceSettingsClicked,
            contentDescription = stringResource(R.string.space_settings)
        )
    }
}

@Composable
private fun CircularIconButton(
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.nav_circular_button_size))
            .clip(CircleShape)
            .background(colorResource(R.color.shape_primary))
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
        )
    }
}

@DefaultPreviews
@Composable
fun HomeScreenToolbarPreview() {
    HomeScreenToolbar(
        onBackButtonClicked = {},
        onSpaceSettingsClicked = {}
    )
}
