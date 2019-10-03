package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.feature_login.ui.login.domain.common.BaseUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.common.FlowUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.model.Account
import com.agileburo.anytype.feature_login.ui.login.domain.model.Image
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class ObserveAccounts : FlowUseCase<List<Account>, BaseUseCase.None>() {
    override fun stream(params: BaseUseCase.None) = flow {
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