package com.anytypeio.anytype.presentation.page.cover

import android.graphics.Color

enum class CoverColor(val code: String, val color: Int) {
    YELLOW(
        color = Color.parseColor("#FBE885"),
        code = "yellow"
    ),
    ORANGE(
        color = Color.parseColor("#F5B748"),
        code = "orange"
    ),
    RED(
        color = Color.parseColor("#E46036"),
        code = "red"
    ),
    PINK(
        color = Color.parseColor("#E6B1A4"),
        code = "pink"
    ),
    PURPLE(
        color = Color.parseColor("#611A36"),
        code = "purple"
    ),
    BLUE(
        color = Color.parseColor("#376BE1"),
        code = "blue"
    ),
    ICE(
        color = Color.parseColor("#97CCEF"),
        code = "ice"
    ),
    TEAL(
        color = Color.parseColor("#9FB0B6"),
        code = "teal"
    ),
    GREEN(
        color = Color.parseColor("#336C45"),
        code = "green"
    ),
    LIGHT_GREY(
        color = Color.parseColor("#DFDDD1"),
        code = "lightgrey"
    ),
    DARK_GREY(
        color = Color.parseColor("#ACA998"),
        code = "darkgrey"
    ),
    BLACK(
        color = Color.parseColor("#2C2B28"),
        code = "black"
    ),
}