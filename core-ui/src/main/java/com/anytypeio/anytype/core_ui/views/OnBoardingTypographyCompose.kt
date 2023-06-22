package com.anytypeio.anytype.core_ui.views

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R

val fontRiccioneRegular = FontFamily(
    Font(R.font.riccione_regular, weight = FontWeight.Normal)
)

val HeadlineOnBoardingTitle =
    TextStyle(
        fontFamily = fontRiccioneRegular,
        fontWeight = FontWeight.W500,
        fontSize = 60.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.05).em
    )

val HeadlineOnBoardingDescription =
    TextStyle(
        fontFamily = fontInterRegular,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.017).em
    )

val TextOnBoardingDescription =
    TextStyle(
        fontFamily = fontInterRegular,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.017).em
    )

val TitleLogin = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.024).em
)

val ConditionLogin = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W700,
    fontSize = 11.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.006).em
)
