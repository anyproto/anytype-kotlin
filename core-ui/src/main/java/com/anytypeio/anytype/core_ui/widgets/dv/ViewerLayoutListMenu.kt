package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi

@Composable
fun ViewerLayoutListMenu(
    show: Boolean,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
    coordinates: Rect
) {
    val offsetX = with(LocalDensity.current) { coordinates.bottomRight.x.toDp() }
    if (show) {
        Column(
            modifier = Modifier
                .offset(x = offsetX - 220.dp, y = (-52).dp)
                .width(220.dp)
                .wrapContentHeight()
                .shadow(
                    elevation = 40.dp,
                    spotColor = Color(0xCC000000),
                    ambientColor = Color(0xE6000000),
                    shape = RoundedCornerShape(size = 10.dp)
                )
                .background(
                    color = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(size = 10.dp)
                )
                .clickable {
                    // Do nothing
                }
        ) {
            Text(
                text = stringResource(R.string.small),
                style = BodyCallout,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(start = 16.dp, top = 11.dp)
                    .noRippleThrottledClickable {
                        action(
                            ViewerLayoutWidgetUi.Action.CardSize(
                                ViewerLayoutWidgetUi.State.CardSize.Small
                            )
                        )
                    }
            )
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
            Text(
                text = stringResource(R.string.large),
                style = BodyCallout,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier
                    .height(44.dp)
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 11.dp)
                    .noRippleThrottledClickable {
                        action(
                            ViewerLayoutWidgetUi.Action.CardSize(
                                ViewerLayoutWidgetUi.State.CardSize.Large
                            )
                        )
                    }
            )
        }
    }
}