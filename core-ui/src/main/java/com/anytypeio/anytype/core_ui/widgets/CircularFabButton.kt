package com.anytypeio.anytype.core_ui.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable

@Composable
fun CircularFabButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    size: Dp = dimensionResource(R.dimen.nav_fab_button_size),
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    backgroundColor: Color = colorResource(id = R.color.navigation_panel),
    elevation: Dp = 20.dp,
    showBorder: Boolean = true,
    iconSize: Dp? = null,
) {
    Box(
        modifier = modifier
            // Dim the whole button (shadow included) when disabled. Applying
            // alpha first means the shadow fades with the background instead of
            // staying at full opacity and leaving a visible shadow ring around
            // a faint circle.
            .alpha(if (isEnabled) 1f else 0.5f)
            .size(size)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                clip = false
            )
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = 1.dp,
                        color = colorResource(id = R.color.shape_transparent_primary),
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (onLongClick != null) {
                    Modifier.noRippleCombinedClickable(
                        enabled = isEnabled,
                        onLongClicked = onLongClick,
                        onClick = onClick,
                    )
                } else {
                    Modifier.noRippleClickable(
                        enabled = isEnabled,
                        onClick = onClick
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = if (iconSize != null) Modifier.size(iconSize) else Modifier,
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription
        )
    }
}

@Composable
@DefaultPreviews
fun FabPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary)),
    ) {
        Spacer(modifier = Modifier.size(99.dp))
        CircularFabButton(
            iconRes = R.drawable.ic_create_obj_32,
            contentDescription = "Create",
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            onClick = {}
        )
    }
}
