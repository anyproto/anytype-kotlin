package com.anytypeio.anytype.middleware.mappers

import anytype.Rpc
import com.anytypeio.anytype.core_models.membership.MembershipErrors

fun Rpc.Membership.IsNameValid.Response.Error.toCore(): MembershipErrors.IsNameValid {
    return when (this.code) {
        IsNameValidErrorCode.NULL -> MembershipErrors.IsNameValid.Null("Null error code")
        IsNameValidErrorCode.UNKNOWN_ERROR -> MembershipErrors.IsNameValid.UnknownError(description)
        IsNameValidErrorCode.BAD_INPUT -> MembershipErrors.IsNameValid.BadInput(description)
        IsNameValidErrorCode.TOO_SHORT -> MembershipErrors.IsNameValid.TooShort(description)
        IsNameValidErrorCode.TOO_LONG -> MembershipErrors.IsNameValid.TooLong(description)
        IsNameValidErrorCode.HAS_INVALID_CHARS -> MembershipErrors.IsNameValid.HasInvalidChars(description)
        IsNameValidErrorCode.TIER_FEATURES_NO_NAME -> MembershipErrors.IsNameValid.TierFeaturesNoName(description)
        IsNameValidErrorCode.TIER_NOT_FOUND -> MembershipErrors.IsNameValid.TierNotFound(description)
        IsNameValidErrorCode.NOT_LOGGED_IN -> MembershipErrors.IsNameValid.NotLoggedIn(description)
        IsNameValidErrorCode.PAYMENT_NODE_ERROR -> MembershipErrors.IsNameValid.PaymentNodeError(description)
        IsNameValidErrorCode.CACHE_ERROR -> MembershipErrors.IsNameValid.CacheError(description)
        IsNameValidErrorCode.CAN_NOT_RESERVE -> MembershipErrors.IsNameValid.CanNotReserve(description)
        IsNameValidErrorCode.CAN_NOT_CONNECT -> MembershipErrors.IsNameValid.CanNotConnect(description)
        IsNameValidErrorCode.NAME_IS_RESERVED -> MembershipErrors.IsNameValid.NameIsReserved(description)
    }
}

fun Rpc.NameService.ResolveName.Response.Error.toCore(): MembershipErrors.ResolveName {
    return when (this.code) {
        ResolveNameErrorCode.NULL -> MembershipErrors.ResolveName.Null("Null error code")
        ResolveNameErrorCode.UNKNOWN_ERROR -> MembershipErrors.ResolveName.UnknownError(description)
        ResolveNameErrorCode.BAD_INPUT -> MembershipErrors.ResolveName.BadInput(description)
        ResolveNameErrorCode.CAN_NOT_CONNECT -> MembershipErrors.ResolveName.CanNotConnect(description)
    }
}

fun Rpc.Membership.GetVerificationEmail.Response.Error.toCore(): MembershipErrors.GetVerificationEmail {
    return when (this.code) {
        GetVerificationEmailErrorCode.EMAIL_WRONG_FORMAT -> MembershipErrors.GetVerificationEmail.EmailWrongFormat(description)
        GetVerificationEmailErrorCode.EMAIL_ALREADY_VERIFIED -> MembershipErrors.GetVerificationEmail.EmailAlreadyVerified(description)
        GetVerificationEmailErrorCode.EMAIL_FAILED_TO_SEND -> MembershipErrors.GetVerificationEmail.EmailFailedToSend(description)
        GetVerificationEmailErrorCode.MEMBERSHIP_ALREADY_EXISTS -> MembershipErrors.GetVerificationEmail.MembershipAlreadyExists(description)
        GetVerificationEmailErrorCode.NULL -> MembershipErrors.GetVerificationEmail.Null("Null error code")
        GetVerificationEmailErrorCode.UNKNOWN_ERROR -> MembershipErrors.GetVerificationEmail.UnknownError(description)
        GetVerificationEmailErrorCode.BAD_INPUT -> MembershipErrors.GetVerificationEmail.BadInput(description)
        GetVerificationEmailErrorCode.NOT_LOGGED_IN -> MembershipErrors.GetVerificationEmail.NotLoggedIn(description)
        GetVerificationEmailErrorCode.PAYMENT_NODE_ERROR -> MembershipErrors.GetVerificationEmail.PaymentNodeError(description)
        GetVerificationEmailErrorCode.CACHE_ERROR -> MembershipErrors.GetVerificationEmail.CacheError(description)
        GetVerificationEmailErrorCode.EMAIL_ALREDY_SENT -> MembershipErrors.GetVerificationEmail.EmailAlreadySent(description)
        GetVerificationEmailErrorCode.CAN_NOT_CONNECT -> MembershipErrors.GetVerificationEmail.CanNotConnect(description)
    }
}

fun Rpc.Membership.VerifyEmailCode.Response.Error.toCore(): MembershipErrors.VerifyEmailCode {
    return when (this.code) {
        VerifyEmailErrorCode.NULL -> MembershipErrors.VerifyEmailCode.Null("Null error code")
        VerifyEmailErrorCode.UNKNOWN_ERROR -> MembershipErrors.VerifyEmailCode.UnknownError(description)
        VerifyEmailErrorCode.BAD_INPUT -> MembershipErrors.VerifyEmailCode.BadInput(description)
        VerifyEmailErrorCode.NOT_LOGGED_IN -> MembershipErrors.VerifyEmailCode.NotLoggedIn(description)
        VerifyEmailErrorCode.PAYMENT_NODE_ERROR -> MembershipErrors.VerifyEmailCode.PaymentNodeError(description)
        VerifyEmailErrorCode.CACHE_ERROR -> MembershipErrors.VerifyEmailCode.CacheError(description)
        VerifyEmailErrorCode.CAN_NOT_CONNECT -> MembershipErrors.VerifyEmailCode.CanNotConnect(description)
        VerifyEmailErrorCode.EMAIL_ALREADY_VERIFIED -> MembershipErrors.VerifyEmailCode.EmailAlreadyVerified(description)
        VerifyEmailErrorCode.CODE_EXPIRED -> MembershipErrors.VerifyEmailCode.CodeExpired(description)
        VerifyEmailErrorCode.CODE_WRONG -> MembershipErrors.VerifyEmailCode.CodeWrong(description)
        VerifyEmailErrorCode.MEMBERSHIP_NOT_FOUND -> MembershipErrors.VerifyEmailCode.MembershipNotFound(description)
        VerifyEmailErrorCode.MEMBERSHIP_ALREADY_ACTIVE -> MembershipErrors.VerifyEmailCode.MembershipAlreadyActive(description)
    }
}

typealias IsNameValidErrorCode = Rpc.Membership.IsNameValid.Response.Error.Code
typealias ResolveNameErrorCode = Rpc.NameService.ResolveName.Response.Error.Code
typealias GetVerificationEmailErrorCode = Rpc.Membership.GetVerificationEmail.Response.Error.Code
typealias VerifyEmailErrorCode = Rpc.Membership.VerifyEmailCode.Response.Error.Code
