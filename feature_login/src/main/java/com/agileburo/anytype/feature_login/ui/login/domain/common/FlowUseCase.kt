package com.agileburo.anytype.feature_login.ui.login.domain.common

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase<out Type, in Params> where Type : Any {
    abstract fun stream(params: Params): Flow<Type>
}