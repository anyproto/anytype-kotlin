package com.anytypeio.anytype.presentation.editor.editor

import android.graphics.Color

/**
 * @property code color code name
 * @property text text color integer for text styling
 * @property background background color integer for background/highlight styling
 */
enum class ThemeColor(
    val code: String,
    val text: Int,
    val background: Int
) {
    DEFAULT(
        code = "default",
        text = Color.parseColor("#2C2B27"),
        background = Color.parseColor("#FFFFFF")
    ),
    GREY(
        code = "grey",
        text = Color.parseColor("#929082"),
        background = Color.parseColor("#F1F0ED")
    ),
    YELLOW(
        code = "yellow",
        text = Color.parseColor("#AFA100"),
        background = Color.parseColor("#FCF8D6")
    ),
    ORANGE(
        code = "orange",
        text = Color.parseColor("#C38400"),
        background = Color.parseColor("#FFF2D7")
    ),
    RED(
        code = "red",
        text = Color.parseColor("#E9410B"),
        background = Color.parseColor("#FEE7E0")
    ),
    PINK(
        code = "pink",
        text = Color.parseColor("#D20D8F"),
        background = Color.parseColor("#FBDFF2")
    ),
    PURPLE(
        code = "purple",
        text = Color.parseColor("#9F43C1"),
        background = Color.parseColor("#F3E7F8")
    ),
    BLUE(
        code = "blue",
        text = Color.parseColor("#3E58EB"),
        background = Color.parseColor("#E4E8FC")
    ),
    ICE(
        code = "ice",
        text = Color.parseColor("#188DCF"),
        background = Color.parseColor("#DDF1FC")
    ),
    TEAL(
        code = "teal",
        text = Color.parseColor("#0BA599"),
        background = Color.parseColor("#D9F6F4")
    ),
    LIME(
        code = "lime",
        text = Color.parseColor("#4DAE00"),
        background = Color.parseColor("#E5F8D6")
    );
}