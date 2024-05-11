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

    sealed class GetPaymentUrl : MembershipErrors() {
        data class TierNotFound(override val message: String) : GetPaymentUrl()
        data class TierInvalid(override val message: String) : GetPaymentUrl()
        data class PaymentMethodInvalid(override val message: String) : GetPaymentUrl()
        data class BadAnyName(override val message: String) : GetPaymentUrl()
        data class MembershipAlreadyExists(override val message: String) : GetPaymentUrl()
    }

    sealed class FinalizePayment : MembershipErrors() {
        data class MembershipNotFound(override val message: String) : FinalizePayment()
        data class MembershipWrongState(override val message: String) : FinalizePayment()
    }

    sealed class GetVerificationEmail : MembershipErrors() {
        data class EmailWrongFormat(override val message: String) : GetVerificationEmail()
        data class EmailAlreadyVerified(override val message: String) : GetVerificationEmail()
        data class EmailAlreadySent(override val message: String) : GetVerificationEmail()
        data class EmailFailedToSend(override val message: String) : GetVerificationEmail()
        data class MembershipAlreadyExists(override val message: String) : GetVerificationEmail()
        data class Null(override val message: String) : GetVerificationEmail()
        data class BadInput(override val message: String) : GetVerificationEmail()
        data class UnknownError(override val message: String) : GetVerificationEmail()
        data class NotLoggedIn(override val message: String) : GetVerificationEmail()
        data class PaymentNodeError(override val message: String) : GetVerificationEmail()
        data class CacheError(override val message: String) : GetVerificationEmail()
        data class CanNotConnect(override val message: String) : GetVerificationEmail()
    }

    sealed class VerifyEmailCode : MembershipErrors() {
        data class EmailAlreadyVerified(override val message: String) : VerifyEmailCode()
        data class CodeExpired(override val message: String) : VerifyEmailCode()
        data class CodeWrong(override val message: String) : VerifyEmailCode()
        data class MembershipNotFound(override val message: String) : VerifyEmailCode()
        data class MembershipAlreadyActive(override val message: String) : VerifyEmailCode()
        data class Null(override val message: String) : VerifyEmailCode()
        data class BadInput(override val message: String) : VerifyEmailCode()
        data class UnknownError(override val message: String) : VerifyEmailCode()
        data class NotLoggedIn(override val message: String) : VerifyEmailCode()
        data class PaymentNodeError(override val message: String) : VerifyEmailCode()
        data class CacheError(override val message: String) : VerifyEmailCode()
        data class CanNotConnect(override val message: String) : VerifyEmailCode()
    }
}
