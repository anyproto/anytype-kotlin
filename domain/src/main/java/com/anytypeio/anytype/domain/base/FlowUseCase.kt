package com.anytypeio.anytype.domain.base

import kotlinx.coroutines.flow.Flow

@Deprecated(
    "Consider replace with the more useful class",
    replaceWith = ReplaceWith(
        expression = "ResultInteractor<Params, Type>",
        imports = arrayOf("com.anytypeio.anytype.domain.base")
    )
)
abstract class FlowUseCase<out Type, in Params> where Type : Any {
    abstract fun build(params: Params? = null): Flow<Type>
}