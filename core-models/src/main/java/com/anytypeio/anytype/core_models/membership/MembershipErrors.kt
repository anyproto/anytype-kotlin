package com.anytypeio.anytype.core_models.membership

sealed class MembershipErrors() {

    data object NotLoggedIn : MembershipErrors()
    data class PaymentNodeError(val message: String) : MembershipErrors()
    data class CacheError(val message: String) : MembershipErrors()
    data class BadAnyName(val message: String) : MembershipErrors()

    sealed class GetStatus : MembershipErrors() {
        data object MembershipNotFound : GetStatus()
        data class MembershipWrongState(val message: String) : GetStatus()
    }

    sealed class IsNameValid : MembershipErrors() {
        data object TooShort : IsNameValid()
        data object TooLong : IsNameValid()
        data object HasInvalidChars : IsNameValid()
        data object TierFeaturesNoName : IsNameValid()
        data object TierNotFound : IsNameValid()
    }

    sealed class GetPaymentUrl : MembershipErrors() {
        data class TierNotFound(val message: String) : GetPaymentUrl()
        data class TierInvalid(val message: String) : GetPaymentUrl()
        data class PaymentMethodInvalid(val message: String) : GetPaymentUrl()
        data class BadAnyName(val message: String) : GetPaymentUrl()
        data class MembershipAlreadyExists(val message: String) : GetPaymentUrl()
    }

    sealed class FinalizePayment : MembershipErrors() {
        data class MembershipNotFound(val message: String) : FinalizePayment()
        data class MembershipWrongState(val message: String) : FinalizePayment()
    }

    sealed class GetVerificationEmail : MembershipErrors() {
        data class EmailWrongFormat(val message: String) : GetVerificationEmail()
        data class EmailAlreadyVerified(val message: String) : GetVerificationEmail()
        data class EmailAlreadySent(val message: String) : GetVerificationEmail()
        data class EmailFailedToSend(val message: String) : GetVerificationEmail()
        data class MembershipAlreadyExists(val message: String) : GetVerificationEmail()
    }
    
    sealed class VerifyEmailCode : MembershipErrors() {
        data class EmailAlreadyVerified(val message: String) : VerifyEmailCode()
        data class CodeExpired(val message: String) : VerifyEmailCode()
        data class CodeWrong(val message: String) : VerifyEmailCode()
        data class MembershipNotFound(val message: String) : VerifyEmailCode()
        data class MembershipAlreadyActive(val message: String) : VerifyEmailCode()
    }

}
