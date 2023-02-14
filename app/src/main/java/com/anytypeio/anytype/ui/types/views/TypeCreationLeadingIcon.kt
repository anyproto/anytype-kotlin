package com.anytypeio.anytype.ui.types.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui.types.views.LeadingIconDefaults.OffsetX

@Composable
fun LeadingIcon(
    icon: ObjectIcon,
    onClick: () -> Unit
) {
    val modifier = Modifier
        .offset(x = OffsetX)
        .clickable { onClick() }
    when (icon) {
        is ObjectIcon.Basic.Emoji -> {
            AndroidView(
                modifier = modifier,
                factory = { ctx -> ObjectIconWidget(ctx) },
                update = { it.setIcon(icon) }
            )
        }
        else -> {
            Image(
                modifier = modifier,
                painter = painterResource(
                    id = R.drawable.ic_page_icon_picker_choose_emoji
                ),
                contentDescription = "",
            )
        }
    }
}

@Immutable
private object LeadingIconDefaults {
    val OffsetX = 4.dp
}