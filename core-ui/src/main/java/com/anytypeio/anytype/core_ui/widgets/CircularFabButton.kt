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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
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
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.nav_fab_button_size))
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                clip = false
            )
            .background(
                color = colorResource(id = R.color.navigation_panel),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_transparent_primary),
                shape = CircleShape
            )
            .alpha(if (isEnabled) 1f else 0.5f)
            .then(
                if (onLongClick != null) {
                    Modifier.noRippleCombinedClickable(
                        enabled = isEnabled,
                        onLongClicked = onLongClick,
                        onClick = onClick,
                    )
                } else {
                    Modifier.noRippleClickable(onClick = onClick)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
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
