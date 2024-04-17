package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface DeepLinkToObjectDelegate {


    class Default(

    ) {

        fun onDeepLinkToObject(obj: Id, space: SpaceId) {

        }

    }

}