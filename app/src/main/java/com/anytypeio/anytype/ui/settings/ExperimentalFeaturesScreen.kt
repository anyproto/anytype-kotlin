package com.anytypeio.anytype.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular

@Composable
fun ExperimentalFeaturesScreen(
    isCompactModeEnabled: Boolean,
    onCompactModeToggled: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                color = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            )
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Header(
            text = stringResource(R.string.experimental_features)
        )

        Spacer(modifier = Modifier.height(10.dp))

        ToggleItemWithDescription(
            title = stringResource(R.string.experimental_features_compact_vault),
            description = stringResource(R.string.experimental_features_compact_vault_description),
            isEnabled = isCompactModeEnabled,
            onToggle = onCompactModeToggled
        )

        Divider()
    }
}

@Composable
private fun ToggleItemWithDescription(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = BodyRegular,
                color = colorResource(R.color.text_primary)
            )
            Text(
                text = description,
                style = Caption1Regular,
                color = colorResource(R.color.text_secondary)
            )
        }
        Image(
            painter = painterResource(
                if (isEnabled) R.drawable.ic_data_view_grid_checkbox_checked
                else R.drawable.ic_data_view_grid_checkbox
            ),
            contentDescription = null
        )
    }
}

@DefaultPreviews
@Composable
fun ExperimentalFeaturesScreenPreview() {
    ExperimentalFeaturesScreen(
        isCompactModeEnabled = false,
        onCompactModeToggled = {}
    )
}
