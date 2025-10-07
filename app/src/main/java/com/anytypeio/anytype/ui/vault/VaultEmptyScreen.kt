package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize

@Composable
fun VaultEmptyState(
    modifier: Modifier = Modifier,
    textRes: Int = R.string.vault_empty_state_text,
    showButton: Boolean = true,
    onCreateSpaceClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_vault_create_space),
            contentDescription = "Empty state icon",
            modifier = Modifier.wrapContentSize()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = textRes),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (showButton) {
            ButtonSecondary(
                onClick = onCreateSpaceClicked,
                modifier = Modifier,
                size = ButtonSize.Small,
                text = stringResource(id = R.string.create_space),
            )
        }
    }
}

@DefaultPreviews
@Composable
fun VaultEmptyStatePreview() {
    VaultEmptyState(
        onCreateSpaceClicked = {}
    )
}