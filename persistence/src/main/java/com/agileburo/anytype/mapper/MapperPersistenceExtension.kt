package com.agileburo.anytype.mapper

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.model.AccountTable

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-22.
 */

fun AccountTable.toEntity(): AccountEntity =
    AccountEntity(
        id = this.id,
        name = this.name
    )

//Todo исправить timestamp
fun AccountEntity.toTable(): AccountTable =
    AccountTable(
        id = this.id,
        name = this.name,
        timestamp = 0
    )