package com.anytypeio.anytype.ui.onboarding

enum class OnboardingPage(val num: Int, val visible: Boolean) {
    AUTH(0, false),
    VOID(1, true),
    MNEMONIC(2, false),
    SET_PROFILE_NAME(1, false),
    SOUL_CREATION_ANIM(4, false),
    RECOVERY(5, false),
    SET_EMAIL(3, false),
}