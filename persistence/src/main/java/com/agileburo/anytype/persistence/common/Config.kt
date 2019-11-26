package com.agileburo.anytype.persistence.common

object Config {

    const val DATABASE_NAME = "AnytypeDatabase"

    const val ACCOUNT_TABLE_NAME = "Accounts"

    const val GET_ACCOUNTS = "SELECT * FROM $ACCOUNT_TABLE_NAME"

    const val CLEAR_ACCOUNT_TABLE = "DELETE FROM $ACCOUNT_TABLE_NAME"

    const val QUERY_LAST_ACCOUNT =
        "SELECT * FROM $ACCOUNT_TABLE_NAME ORDER BY timestamp DESC LIMIT 1"

    const val QUERY_ACCOUNT_BY_ID = "SELECT * FROM $ACCOUNT_TABLE_NAME WHERE id = :id"
}