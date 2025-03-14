package com.anytypeio.anytype.core_ui.features.wallpaper

import androidx.compose.ui.graphics.Color
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient

fun gradient(
    gradient: String,
    alpha: Float = 1.0f
) : List<Color> {
    return when(gradient) {
        CoverGradient.YELLOW -> {
            return listOf(
                Color(0xFFecd91b).copy(alpha = alpha),
                Color(0xFFffb522).copy(alpha = alpha)
            )
        }
        CoverGradient.RED -> {
            return listOf(
                Color(0xFFe51ca0).copy(alpha = alpha),
                Color(0xFFf55522).copy(alpha = alpha)
            )
        }
        CoverGradient.BLUE -> {
            return listOf(
                Color(0xFF3e58eb).copy(alpha = alpha),
                Color(0xFFab50cc).copy(alpha = alpha)
            )
        }
        CoverGradient.TEAL -> {
            return listOf(
                Color(0xFF0fc8ba).copy(alpha = alpha),
                Color(0xFF2aa7ee).copy(alpha = alpha)
            )
        }
        CoverGradient.PINK_ORANGE -> {
            return listOf(
                Color(0xFFD8A4E1).copy(alpha = alpha),
                Color(0xFFFDD0CD).copy(alpha = alpha),
                Color(0xFFFFCC81).copy(alpha = alpha)
            )
        }
        CoverGradient.BLUE_PINK -> {
            return listOf(
                Color(0xFF73B7F0).copy(alpha = alpha),
                Color(0xFFABB6ED).copy(alpha = alpha),
                Color(0xFFF3BFAC).copy(alpha = alpha)
            )
        }
        CoverGradient.GREEN_ORANGE -> {
            return listOf(
                Color(0xFF63B3CB).copy(alpha = alpha),
                Color(0xFFC5D3AC).copy(alpha = alpha),
                Color(0xFFF6C47A).copy(alpha = alpha)
            )
        }
        CoverGradient.SKY -> {
            return listOf(
                Color(0xFF6EB6E4).copy(alpha = alpha),
                Color(0xFFA4CFEC).copy(alpha = alpha),
                Color(0xFFDAEAF3).copy(alpha = alpha)
            )
        }
        else -> return emptyList()
    }
}