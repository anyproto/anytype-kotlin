package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.ui_settings.R

@Composable
fun ViewerSpaceSettings(
    title: String,
    description: String,
    icon: SpaceIconView,
    uiEvent: (UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Box(
            modifier = Modifier
                .height(48.dp)
                .width(56.dp)
                .align(Alignment.End)
        ) {
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(R.drawable.ic_more_32),
                contentDescription = "Three dots button"
            )
        }
        NewSpaceIcon(
            modifier = Modifier.fillMaxWidth(),
            icon = icon,
            isEditEnabled = false,
            uiEvent = uiEvent
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title.ifEmpty { stringResource(R.string.untitled) },
            style = HeadlineHeading,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        if (description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_primary),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        MultiplayerButtons(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            uiEvent = uiEvent
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@DefaultPreviews
@Composable
fun ViewerSpaceSettingsWithDescriptionPreview() {
    ViewerSpaceSettings(
        title = "Susan Sontag",
        description = stringResource(R.string.default_text_placeholder),
        uiEvent = {},
        icon = SpaceIconView.Placeholder(
            name = "Susan",
            color = SystemColor.SKY
        )
    )
}

@DefaultPreviews
@Composable
fun ViewerSpaceSettingsWithoutDescriptionPreview() {
    ViewerSpaceSettings(
        title = "Susan Sontag",
        description = "",
        uiEvent = {},
        icon = SpaceIconView.Placeholder(
            name = "Susan",
            color = SystemColor.SKY
        )
    )
}