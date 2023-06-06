package com.anytypeio.anytype.ui.onboarding

sealed interface OnboardingScreenContract {
    object JoinClick : OnboardingScreenContract
    object LogInClick : OnboardingScreenContract
    object TermsOfUseClick : OnboardingScreenContract
    object PrivacyPolicyClick : OnboardingScreenContract
}

enum class Page(val num: Int, val visible: Boolean) {
    AUTH(0, false),
    INVITE_CODE(1, true),
    VOID(2, true),
    MNEMONIC(3, true),
    SOUL_CREATION(4, true),
    SOUL_CREATION_ANIM(5, false)
}