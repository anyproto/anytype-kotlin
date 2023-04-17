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

val HeadlineTitle =
    TextStyle(
        fontFamily = fontInterBold,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 30.5.sp,
        letterSpacing = (-0.017).sp
    )

val HeadlineHeading =
    TextStyle(
        fontFamily = fontInterBold,
        fontWeight = FontWeight.W700,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.016).sp
    )

val HeadlineSubheading =
    TextStyle(
        fontFamily = fontInterBold,
        fontWeight = FontWeight.W700,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.016).sp
    )

val BodyBold = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)

val BodyBoldItalic = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontStyle = FontStyle.Italic,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)

val BodyRegular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)

val BodyItalic = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontStyle = FontStyle.Italic,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)

val BodyCallout = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.016).sp
)

val PreviewTitle1Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.024).sp
)

val PreviewTitle1Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.024).sp
)

val PreviewTitle2Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.016).sp
)

val PreviewTitle2Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.016).sp
)

val Relations1 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.016).sp
)

val Relations2 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.006).sp
)

val Relations3 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 12.sp,
    lineHeight = 15.sp
)

val CodeBlock = TextStyle(
    fontFamily = fontIBM,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.016).sp
)

val Title1 = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)

val Title2 = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.014).sp
)

val Title3 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.014).sp
)

val UXBody = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)

val BodyCalloutMedium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.014).sp
)

val BodyCalloutRegular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.014).sp
)

val Caption1Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.006).sp
)

val Caption1Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.006).sp
)

val Caption2Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = (-0.006).sp
)

val Caption2Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = (-0.006).sp
)

val ButtonMedium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)

val ButtonRegular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).sp
)