package com.agileburo.anytype.common

object Config {
    const val DATABASE_NAME = "AnytypeDatabase"
    const val ACCOUNT_TABLE_NAME = "Accounts"

    const val CLEAR_ACCOUNT_TABLE = "DELETE FROM $ACCOUNT_TABLE_NAME"
}