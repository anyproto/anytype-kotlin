package com.anytypeio.anytype.ui.library.styles

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import androidx.compose.ui.text.TextStyle as TextStyle

val fonts = FontFamily(
    Font(R.font.inter_regular),
    Font(R.font.inter_bold, weight = FontWeight.Bold),
    Font(R.font.inter_medium, weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold)
)

val TabTitleStyle = TextStyle(
    fontFamily = fonts,
    fontSize = 15.sp,
    fontWeight = FontWeight.SemiBold
)