package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun TypeIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.TypeIcon,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp = 20.dp,
    backgroundColor: Int = R.color.shape_tertiary
) {
    //todo next PR
}