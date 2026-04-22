package com.anytypeio.anytype.presentation.home

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.spaces.ResolveSpaceHomepage

object SpaceHomepageResolver {

    fun isExplicitObjectHomepage(homepage: String?): Boolean =
        !homepage.isNullOrEmpty() && homepage !in ResolveSpaceHomepage.HOMEPAGE_SPECIAL_CONSTANTS

    fun shouldSkipWidgetInjection(spaceView: ObjectWrapper.SpaceView?): Boolean =
        isExplicitObjectHomepage(spaceView?.homepage)
}
