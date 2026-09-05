package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.feature_vault.R
import com.anytypeio.anytype.feature_vault.presentation.VaultViewModel

/**
 * Quick-capture success banner: wide card above the bottom edge of the vault with
 * "{Type} created in {Space}" and a trailing "View" affordance. The whole card is
 * tappable and opens the created object (switching space if needed); the view model
 * auto-hides it after a few seconds.
 */
@Composable
fun QuickCaptureSuccessBanner(
    banner: VaultViewModel.QuickCaptureSuccess?,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep the last non-null value so the exit animation shows real content.
    var lastBanner by remember { mutableStateOf(banner) }
    if (banner != null) lastBanner = banner
    AnimatedVisibility(
        visible = banner != null,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        val content = lastBanner ?: return@AnimatedVisibility
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(color = colorResource(id = R.color.background_secondary))
                .noRippleThrottledClickable { onClicked() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_16),
                contentDescription = null,
                tint = colorResource(id = R.color.palette_system_green),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(
                    id = R.string.quick_capture_created_in,
                    content.typeName.ifEmpty { stringResource(id = R.string.untitled) },
                    content.spaceName.ifEmpty { stringResource(id = R.string.untitled) }
                ),
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.view),
                style = BodyCalloutMedium,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}
