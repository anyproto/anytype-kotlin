package com.anytypeio.anytype.ui.multiplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading

@Preview
@Composable
fun HowToShareSpaceScreenPreview() {
    HowToShareSpaceScreen()
}

@Composable
fun HowToShareSpaceScreen() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.multiplayer_how_to_share_space),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = " • ",
                color = colorResource(id = R.color.text_primary),
                style = BodyRegular
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.multiplayer_how_to_share_space_step_1),
                color = colorResource(id = R.color.text_primary),
                style = BodyCalloutRegular
            )
        }
        Row(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = " • ",
                color = colorResource(id = R.color.text_primary),
                style = BodyRegular
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.multiplayer_how_to_share_space_step_2),
                color = colorResource(id = R.color.text_primary),
                style = BodyCalloutRegular
            )
        }
        Row(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = " • ",
                color = colorResource(id = R.color.text_primary),
                style = BodyRegular
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.multiplayer_how_to_share_space_step_3),
                color = colorResource(id = R.color.text_primary),
                style = BodyCalloutRegular
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}