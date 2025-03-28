package com.anytypeio.anytype.core_models.exceptions

class AccountIsDeletedException : Exception()
class NeedToUpdateApplicationException: Exception()
class AccountMigrationNeededException: Exception()

sealed class MigrationFailedException : Exception() {
    data class NotEnoughSpace(val requiredSpaceInBytes: Long) : MigrationFailedException()
}