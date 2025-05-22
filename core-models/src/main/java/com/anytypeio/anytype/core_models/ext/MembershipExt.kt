package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.membership.MembershipConstants
import com.anytypeio.anytype.core_models.membership.MembershipUpgradeReason
import com.anytypeio.anytype.core_models.membership.TierId

fun TierId.isPossibleToUpgrade(
    reason: MembershipUpgradeReason
): Boolean {
    return when (reason) {
        MembershipUpgradeReason.NumberOfEditors -> isPossibleToUpgradeNumberOfSpaceMembers()
        MembershipUpgradeReason.NumberOfReaders -> isPossibleToUpgradeNumberOfSpaceMembers()
        //24-07-24 https://linear.app/anytype/issue/PROD-1368/[part-1]-release-4-or-payment-for-the-end-user
        //not possible to upgrade the number of shared spaces
        MembershipUpgradeReason.NumberOfSharedSpaces -> false
        MembershipUpgradeReason.StorageSpace -> isPossibleToUpgradeStorageSpace()
    }
}

fun TierId.isPossibleToUpgradeNumberOfSpaceMembers(): Boolean {
    return when (this.value) {
        MembershipConstants.NONE_ID -> true
        MembershipConstants.OLD_EXPLORER_ID -> true
        MembershipConstants.STARTER_ID -> true
        MembershipConstants.PIONEER_ID -> true
        MembershipConstants.NEW_EXPLORER_ID -> true
        MembershipConstants.BUILDER_ID -> false
        MembershipConstants.CO_CREATOR_ID -> false
        MembershipConstants.ANY_TEAM_ID -> false
        else -> true
    }
}

fun TierId.isPossibleToUpgradeStorageSpace(): Boolean {
    return when (this.value) {
        MembershipConstants.NONE_ID -> true
        MembershipConstants.OLD_EXPLORER_ID -> true
        MembershipConstants.STARTER_ID -> true
        MembershipConstants.PIONEER_ID -> true
        MembershipConstants.NEW_EXPLORER_ID -> true
        MembershipConstants.BUILDER_ID -> true
        MembershipConstants.CO_CREATOR_ID -> false
        MembershipConstants.ANY_TEAM_ID -> false
        else -> true
    }
}