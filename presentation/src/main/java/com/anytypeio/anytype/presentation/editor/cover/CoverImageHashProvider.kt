package com.anytypeio.anytype.presentation.editor.cover

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id


interface CoverImageHashProvider {
    fun provide(id: Id): Hash?
}