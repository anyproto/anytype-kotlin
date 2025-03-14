package com.anytypeio.anytype.presentation.objects.custom_icon

sealed class CustomIconDataColor {
    abstract val iconOption: Int?

    object Placeholder : CustomIconDataColor() {
        override val iconOption: Int? = null
    }

    data class Selected(val color: CustomIconColor) : CustomIconDataColor() {
        override val iconOption: Int? get() = color.iconOption
    }
}

data class CustomIconData(
    val icon: CustomIcon,
    val color: CustomIconDataColor
) {
    // Constructor for a specific custom color.
    constructor(icon: CustomIcon, customColor: CustomIconColor) :
            this(icon, CustomIconDataColor.Selected(customColor))

    // Constructor for a placeholder icon.
    constructor(placeholderIcon: CustomIcon) :
            this(placeholderIcon, CustomIconDataColor.Placeholder)
}