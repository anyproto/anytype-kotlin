package com.anytypeio.anytype.ui.home


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.CircularFabButton

@Composable
fun HomeScreenToolbar(
    modifier: Modifier = Modifier,
    onBackButtonClicked: () -> Unit,
    onSpaceSettingsClicked: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircularFabButton(
            iconRes = R.drawable.ic_default_top_back,
            onClick = onBackButtonClicked,
            contentDescription = stringResource(R.string.content_desc_back_button)
        )
        CircularFabButton(
            iconRes = R.drawable.ic_space_list_dots,
            onClick = onSpaceSettingsClicked,
            contentDescription = stringResource(R.string.space_settings)
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
