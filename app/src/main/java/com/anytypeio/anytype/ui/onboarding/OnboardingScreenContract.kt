package com.anytypeio.anytype.ui.onboarding

enum class OnboardingPage(val num: Int, val visible: Boolean) {
    AUTH(0, false),
    VOID(1, true),
    MNEMONIC(2, true),
    SOUL_CREATION(3, true),
    SOUL_CREATION_ANIM(4, false),
    RECOVERY(5, false),
    ENTER_THE_VOID(6, false)
}