package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut(
            tween(100)
        )
    )
    {
        Column(
            modifier = Modifier
                .offset(x = offsetX - 220.dp, y = (-52).dp)
                .width(220.dp)
                .wrapContentHeight()
                .shadow(
                    elevation = 40.dp,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000)
                )
                .background(
                    color = colorResource(id = R.color.background_primary),
                    shape = RoundedCornerShape(size = 10.dp)
                )
        ) {
            Text(
                text = stringResource(R.string.small),
                style = BodyCallout,
                modifier = Modifier
                    .height(44.dp)
                    .padding(start = 16.dp, top = 11.dp)
                    .noRippleThrottledClickable {
                        action.invoke(
                            ViewerLayoutWidgetUi.Action.CardSize(
                                ViewerLayoutWidgetUi.State.CardSize.Small
                            )
                        )
                    }
            )
            Divider()
            Text(
                text = stringResource(R.string.large),
                style = BodyCallout,
                modifier = Modifier
                    .height(44.dp)
                    .padding(start = 16.dp, top = 11.dp)
                    .noRippleThrottledClickable {
                        action.invoke(
                            ViewerLayoutWidgetUi.Action.CardSize(
                                ViewerLayoutWidgetUi.State.CardSize.Large
                            )
                        )
                    }
            )
        }
    }
}