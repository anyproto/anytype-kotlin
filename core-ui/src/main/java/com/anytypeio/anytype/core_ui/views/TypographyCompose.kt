package com.anytypeio.anytype.core_ui.views

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R

val fontInterRegular = FontFamily(
    Font(R.font.inter_regular, weight = FontWeight.Normal, loadingStrategy = FontLoadingStrategy.Async)
)

val fontInterMedium = FontFamily(
    Font(R.font.inter_medium, weight = FontWeight.Medium, loadingStrategy = FontLoadingStrategy.Async)
)

val fontInterBold = FontFamily(
    Font(R.font.inter_bold, weight = FontWeight.Bold, loadingStrategy = FontLoadingStrategy.Async)
)

val fontInterSemibold = FontFamily(
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold, loadingStrategy = FontLoadingStrategy.Async)
)

val fontIBM = FontFamily(
    Font(R.font.ibm_plex_mono, weight = FontWeight.Normal, loadingStrategy = FontLoadingStrategy.Async)
)

//Content/Headlines/Title
val HeadlineTitle =
    TextStyle(
        fontFamily = fontInterBold,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 30.5.sp,
        letterSpacing = (-0.017).em
    )

//Content/Headlines/Heading
val HeadlineHeading =
    TextStyle(
        fontFamily = fontInterBold,
        fontWeight = FontWeight.W700,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.016).em
    )

//Content/Headlines/Subheading
val HeadlineSubheading =
    TextStyle(
        fontFamily = fontInterBold,
        fontWeight = FontWeight.W700,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.016).em
    )

//Content/Body/Bold
val BodyBold = TextStyle(
    fontFamily = fontInterBold,
    fontWeight = FontWeight.W700,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

val BodySemiBold = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

//Content/Body/Bold Italic
val BodyBoldItalic = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontStyle = FontStyle.Italic,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

//Content/Body/Regular
val BodyRegular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

//Content/Misc/Chat
val ContentMiscChat = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.0241).em
)

//Content/Body/Italic
val BodyItalic = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontStyle = FontStyle.Italic,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

//Content/Callout/Regular
val BodyCallout = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.016).em
)

//Content/Preview Title 1/Medium
val PreviewTitle1Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.024).em
)

//Content/Preview Title 1/Regular
val PreviewTitle1Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.024).em
)

//Content/Preview Title 2/Medium
val PreviewTitle2Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.016).em
)

//Content/Preview Title 2/Regular
val PreviewTitle2Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.016).em
)

//Content/Relation 1/Regular
val Relations1 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.016).em
)

//Content/Relation 2/Regular
val Relations2 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.006).em
)

//Content/Relation 3/Regular
val Relations3 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 12.sp,
    lineHeight = 15.sp
)

//Content/Misc/Code block
val CodeBlock = TextStyle(
    fontFamily = fontIBM,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.016).em
)

val TitleInter15 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W600,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.02).em
)

//UX/Title 1/Semibold
val Title1 = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

//UX/Title 2/Medium
val Title2 = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.014).em
)

//UX/Title 2/Regular
val Title3 = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.014).em
)

//UX/Body/Regular
val UXBody = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.024).em
)

//UX/Callout/Medium
val BodyCalloutMedium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.014).em
)

//UX/Callout/Regular
val BodyCalloutRegular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.024).sp
)

val UxSmallTextRegular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.014).em
)

val UxSmallTextMedium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.12).sp
)

//UX/Caption 1/Medium
val Caption1Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.006).em
)

//UX/Caption 1/Regular
val Caption1Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = (-0.006).em
)

//UX/Caption 2/Medium
val Caption2Medium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = (-0.006).em
)

//UX/Caption 2/Regular
val Caption2Regular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = (-0.006).em
)

val Caption2Semibold = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = (-0.006).em
)

//UX/Button/Medium
val ButtonMedium = TextStyle(
    fontFamily = fontInterMedium,
    fontWeight = FontWeight.W500,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.41).sp
)

//UX/Button/Regular
val ButtonRegular = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W400,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.41).sp
)

val ModalTitle = TextStyle(
    fontFamily = fontInterSemibold,
    fontWeight = FontWeight.W600,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.014).em
)

val AvatarTitle = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W600
)

//Content/Headlines/Title Semibold
val HeadlineTitleSemibold =
    TextStyle(
        fontFamily = fontInterSemibold,
        fontWeight = FontWeight.W600,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.017).em
    )

val chatPreviewTextStyle = TextStyle(
    fontFamily = fontInterRegular,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.014).em
)