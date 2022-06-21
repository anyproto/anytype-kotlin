package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase

abstract class RemoveIcon<T> : BaseUseCase<Payload, T>()