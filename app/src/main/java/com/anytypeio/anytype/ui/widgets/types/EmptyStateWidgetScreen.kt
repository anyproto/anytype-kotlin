package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Caption1Medium

@Composable
fun EmptyStateWidgetScreen(
    modifier: Modifier = Modifier,
    onAddWidgetClicked: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.widgets_empty_state_message),
                style = BodyRegular,
                color = colorResource(R.color.text_primary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(
                        shape = RoundedCornerShape(
                            6.dp
                        ),
                        color = colorResource(R.color.transparent_active)
                    )
                    .clip(
                        RoundedCornerShape(
                            6.dp
                        )
                    )
                    .clickable {
                        onAddWidgetClicked()
                    }
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    text = stringResource(R.string.empty_state_add_widget),
                    style = Caption1Medium,
                    color = colorResource(R.color.text_label_inversion)
                )
            }
        }
    }
}

@Composable
fun AddWidgetButton(modifier: Modifier, onAddWidgetClicked: () -> Unit) {
    Box(
        modifier = modifier
            .background(
                shape = RoundedCornerShape(6.dp),
                color = colorResource(R.color.transparent_active)
            )
            .clickable { onAddWidgetClicked() }
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            text = stringResource(R.string.empty_state_add_widget),
            style = Caption1Medium,
            color = colorResource(R.color.text_label_inversion)
        )
    }
}

@DefaultPreviews
@Composable
fun EmptyStateWidgetScreenPreview() {
    EmptyStateWidgetScreen(
        onAddWidgetClicked = {}
    )
}