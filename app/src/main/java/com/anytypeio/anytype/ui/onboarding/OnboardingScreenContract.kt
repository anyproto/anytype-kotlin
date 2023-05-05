package com.anytypeio.anytype.ui.onboarding

sealed interface OnboardingScreenContract {
    object JoinClick: OnboardingScreenContract
    object LogInClick: OnboardingScreenContract
    object TermsOfUseClick: OnboardingScreenContract
    object PrivacyPolicyClick: OnboardingScreenContract
}