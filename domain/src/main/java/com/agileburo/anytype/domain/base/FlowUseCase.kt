package com.agileburo.anytype.domain.base

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase<out Type, in Params> where Type : Any {
    abstract fun stream(params: Params): Flow<Type>
}