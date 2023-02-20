package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import com.anytypeio.anytype.core_ui.features.objects.ObjectActionAdapter
import com.anytypeio.anytype.presentation.objects.ObjectAction

class CollectionActionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseActionWidget<ObjectAction>(context, attrs) {

    override fun provideAdapter() = ObjectActionAdapter { action -> actionListener(action) }
}