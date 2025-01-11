package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.AvatarTitle
import com.anytypeio.anytype.presentation.objects.ObjectIcon


val avatarBackgroundColor = R.color.shape_transparent_secondary
val avatarTextColor = R.color.glyph_active

@Composable
fun AvatarIconView(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Profile.Avatar,
    isCircleShape: Boolean = true
) {
    val (radius, fontSize) = getAvatarIconParams(iconSize)

    Box(
        modifier = modifier
            .size(iconSize)
            .background(
                shape = if (isCircleShape) {
                    CircleShape
                } else {
                    RoundedCornerShape(radius)
                },
                color = colorResource(id = avatarBackgroundColor)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon
                .name
                .ifEmpty { stringResource(id = R.string.u) }
                .take(1)
                .uppercase(),
            style = AvatarTitle,
            fontSize = fontSize.sp,
            color = colorResource(id = avatarTextColor)
        )
    }
}

private fun getAvatarIconParams(size: Dp): Pair<Int, Int> {
    return when (size) {
        in 0.dp..16.dp -> 2 to 11
        in 17.dp..18.dp -> 2 to 12
        in 19.dp..24.dp -> 2 to 13
        in 25.dp..32.dp -> 4 to 20
        in 33.dp..40.dp -> 5 to 24
        in 41.dp..48.dp -> 6 to 28
        in 49.dp..64.dp -> 8 to 40
        in 65.dp..80.dp -> 12 to 44
        in 81.dp..96.dp -> 12 to 64
        else -> 12 to 64
    }
}

@DefaultPreviews
@Composable
fun Avatar20IconViewPreview() {
    AvatarIconView(
        iconSize = 20.dp,
        icon = ObjectIcon.Profile.Avatar("John Doe"),
        modifier = Modifier
    )
}

@DefaultPreviews
@Composable
fun Avatar32IconViewPreview() {
    AvatarIconView(
        iconSize = 32.dp,
        icon = ObjectIcon.Profile.Avatar("John Doe"),
        modifier = Modifier
    )
}

@DefaultPreviews
@Composable
fun Avatar48IconViewPreview() {
    AvatarIconView(
        iconSize = 48.dp,
        icon = ObjectIcon.Profile.Avatar("John Doe"),
        modifier = Modifier
    )
}

@DefaultPreviews
@Composable
fun Avatar64IconViewPreview() {
    AvatarIconView(
        iconSize = 64.dp,
        icon = ObjectIcon.Profile.Avatar("John Doe"),
        modifier = Modifier
    )
}