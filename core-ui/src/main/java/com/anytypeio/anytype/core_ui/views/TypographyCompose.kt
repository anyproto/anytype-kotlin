package com.anytypeio.anytype.core_ui.views

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import com.anytypeio.anytype.core_ui.R

val fontInterRegular = FontFamily(
    Font(R.font.inter_regular, weight = FontWeight.Normal)
)

val fontInterMedium = FontFamily(
    Font(R.font.inter_medium, weight = FontWeight.Medium)
)

val fontInterBold = FontFamily(
    Font(R.font.inter_bold, weight = FontWeight.Bold)
)

val fontInterSemibold = FontFamily(
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold)
)

val fontIBM = FontFamily(
    Font(R.font.ibm_plex_mono, weight = FontWeight.Normal)
)

val Caption1Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.006).sp
)

val BodyCalloutMedium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.014).sp
)

val ButtonMedium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)