package com.anytypeio.anytype.core_models.membership

sealed class MembershipErrors : Exception() {

    sealed class GetStatus : MembershipErrors() {
        data class MembershipNotFound(override val message: String) : GetStatus()
        data class MembershipWrongState(override val message: String) : GetStatus()
    }

    sealed class IsNameValid : MembershipErrors() {
        data class Null(override val message: String) : IsNameValid()
        data class BadInput(override val message: String) : IsNameValid()
        data class UnknownError(override val message: String) : IsNameValid()
        data class NotLoggedIn(override val message: String) : IsNameValid()
        data class PaymentNodeError(override val message: String) : IsNameValid()
        data class CacheError(override val message: String) : IsNameValid()
        data class TooShort(override val message: String) : IsNameValid()
        data class TooLong(override val message: String) : IsNameValid()
        data class HasInvalidChars(override val message: String) : IsNameValid()
        data class TierFeaturesNoName(override val message: String) : IsNameValid()
        data class TierNotFound(override val message: String) : IsNameValid()
        data class CanNotReserve(override val message: String) : IsNameValid()
        data class CanNotConnect(override val message: String) : IsNameValid()
    }

    sealed class ResolveName : MembershipErrors() {
        data class Null(override val message: String) : ResolveName()
        data class UnknownError(override val message: String) : ResolveName()
        data class BadInput(override val message: String) : ResolveName()
        data class CanNotConnect(override val message: String) : ResolveName()
    }

//    sealed class GetPaymentUrl : MembershipErrors() {
//        data class TierNotFound(val message: String) : GetPaymentUrl()
//        data class TierInvalid(val message: String) : GetPaymentUrl()
//        data class PaymentMethodInvalid(val message: String) : GetPaymentUrl()
//        data class BadAnyName(val message: String) : GetPaymentUrl()
//        data class MembershipAlreadyExists(val message: String) : GetPaymentUrl()
//    }
//
//    sealed class FinalizePayment : MembershipErrors() {
//        data class MembershipNotFound(val message: String) : FinalizePayment()
//        data class MembershipWrongState(val message: String) : FinalizePayment()
//    }
//
//    sealed class GetVerificationEmail : MembershipErrors() {
//        data class EmailWrongFormat(val message: String) : GetVerificationEmail()
//        data class EmailAlreadyVerified(val message: String) : GetVerificationEmail()
//        data class EmailAlreadySent(val message: String) : GetVerificationEmail()
//        data class EmailFailedToSend(val message: String) : GetVerificationEmail()
//        data class MembershipAlreadyExists(val message: String) : GetVerificationEmail()
//    }
//
//    sealed class VerifyEmailCode : MembershipErrors() {
//        data class EmailAlreadyVerified(val message: String) : VerifyEmailCode()
//        data class CodeExpired(val message: String) : VerifyEmailCode()
//        data class CodeWrong(val message: String) : VerifyEmailCode()
//        data class MembershipNotFound(val message: String) : VerifyEmailCode()
//        data class MembershipAlreadyActive(val message: String) : VerifyEmailCode()
//    }

}
