package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.feature_login.ui.login.domain.model.Account
import com.agileburo.anytype.feature_login.ui.login.domain.model.Image
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ObserveAccounts {
    fun observe(): Flow<List<Account>> = flow {
        delay(1000)
        val first = Account(
            id = "1",
            name = "Ubu",
            image = Image(
                id = "1",
                size = Image.ImageSize.SMALL
            )
        )
        emit(listOf(first))
        delay(1000)
        val second = Account(
            id = "1",
            name = "Evgenii",
            image = Image(
                id = "1",
                size = Image.ImageSize.SMALL
            )
        )
        emit(listOf(first, second))
    }
}