package com.anytypeio.anytype.ui.onboarding

enum class OnboardingPage(val num: Int, val visible: Boolean) {
    AUTH(0, false),
    MNEMONIC(2, false),
    SET_PROFILE_NAME(1, false),
    RECOVERY(5, false),
    SET_EMAIL(3, false),
    SELECTION(6, false),
    USECASE(7, false)
}