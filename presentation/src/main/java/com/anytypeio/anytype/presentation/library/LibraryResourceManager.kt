package com.anytypeio.anytype.presentation.library

import android.content.Context
import android.content.res.Resources
import com.anytypeio.anytype.presentation.R
import javax.inject.Inject

interface LibraryResourceManager {

    fun messageRelationAdded(name: String): String
    fun messageRelationRemoved(name: String): String
    fun messageTypeAdded(name: String): String
    fun messageTypeRemoved(name: String): String

    val errorMessage: String

    class Impl @Inject constructor(
        val context: Context
    ) : LibraryResourceManager {

        private val resources: Resources = context.resources

        override fun messageRelationAdded(name: String) =
            resources.getString(R.string.library_relation_added, name)

        override fun messageRelationRemoved(name: String) =
            resources.getString(R.string.library_relation_removed, name)

        override fun messageTypeAdded(name: String) =
            resources.getString(R.string.library_type_added, name)


        override fun messageTypeRemoved(name: String) =
            resources.getString(R.string.library_type_removed, name)

        override val errorMessage: String =
            resources.getString(R.string.library_something_went_wrong)

    }

}