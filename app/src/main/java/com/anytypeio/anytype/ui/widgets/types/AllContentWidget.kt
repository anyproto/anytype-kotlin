package com.anytypeio.anytype.ui.widgets.types

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.home.InteractionMode

@Composable
fun AllContentWidgetCard(
    mode: InteractionMode,
    onWidgetClicked: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .fillMaxWidth()
            .height(52.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (mode !is InteractionMode.Edit) {
                    Modifier.clickable {
                        onWidgetClicked()
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_widget_all_content),
            contentDescription = "All content icon",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.all_content),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 44.dp, end = 16.dp),
            style = HeadlineSubheading,
            color = colorResource(id = R.color.text_primary),
        )

    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun AllContentWidgetPreview() {
    AllContentWidgetCard(
        onWidgetClicked = {},
        mode = InteractionMode.Default
    )
}

