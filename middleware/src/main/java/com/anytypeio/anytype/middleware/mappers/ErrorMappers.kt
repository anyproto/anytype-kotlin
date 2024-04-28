package com.anytypeio.anytype.middleware.mappers

import anytype.Rpc
import com.anytypeio.anytype.core_models.membership.MembershipErrors

fun Rpc.Membership.IsNameValid.Response.Error.toCore(): MembershipErrors.IsNameValid {
    return when (this.code) {
        IsNameValidErrorCode.NULL -> MembershipErrors.IsNameValid.Null("Null error code1234")
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

typealias IsNameValidErrorCode = Rpc.Membership.IsNameValid.Response.Error.Code
typealias ResolveNameErrorCode = Rpc.NameService.ResolveName.Response.Error.Code