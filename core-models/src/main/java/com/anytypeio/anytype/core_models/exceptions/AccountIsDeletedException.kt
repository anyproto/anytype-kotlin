package com.anytypeio.anytype.core_models.exceptions

class AccountIsDeletedException : Exception()
class NeedToUpdateApplicationException: Exception()
class AccountMigrationNeededException: Exception()

sealed class MigrationFailedException : Exception() {
    class NotEnoughSpace : MigrationFailedException()
}