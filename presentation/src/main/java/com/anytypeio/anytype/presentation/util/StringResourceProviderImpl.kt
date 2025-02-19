package com.anytypeio.anytype.presentation.util

import android.content.Context
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.R
import javax.inject.Inject

class StringResourceProviderImpl @Inject constructor(private val context: Context) :
    StringResourceProvider {

    override fun getRelativeDateName(relativeDate: RelativeDate): String {
        return when (relativeDate) {
            RelativeDate.Empty -> ""
            is RelativeDate.Other -> relativeDate.formattedDate
            is RelativeDate.Today -> context.getString(R.string.today)
            is RelativeDate.Tomorrow -> context.getString(R.string.tomorrow)
            is RelativeDate.Yesterday -> context.getString(R.string.yesterday)
            else -> ""
        }
    }

    override fun getDeletedObjectTitle(): String {
        return context.getString(R.string.non_existent_object)
    }

    override fun getUntitledObjectTitle(): String {
        return context.getString(R.string.untitled)
    }

    override fun getSetOfObjectsTitle(): String {
        return context.getString(R.string.object_set_of_title)
    }
}