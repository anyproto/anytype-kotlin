package com.anytypeio.anytype.core_ui.features.sharing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular

@Composable
fun SharingNoSpacesScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Coffee icon
        Image(
            painter = painterResource(id = R.drawable.ic_popup_coffee_56),
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Empty state message
        Text(
            text = stringResource(R.string.you_dont_have_any_spaces_yet),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
    }
}


@DefaultPreviews
@Composable
private fun SharingNoSpacesScreenPreview() {
    MaterialTheme {
        SharingNoSpacesScreen()
    }
}