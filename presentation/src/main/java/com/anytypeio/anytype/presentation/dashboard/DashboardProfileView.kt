package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url

data class DashboardProfileView(
    val id: Id,
    val name: String,
    val image: Url?
)