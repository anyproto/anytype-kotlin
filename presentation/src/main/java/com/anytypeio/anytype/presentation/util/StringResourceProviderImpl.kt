package com.anytypeio.anytype.presentation.util

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
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

    override fun getPropertiesFormatPrettyString(format: RelationFormat): String {
        return when (format) {
            RelationFormat.LONG_TEXT, RelationFormat.SHORT_TEXT -> context.getString(R.string.relation_format_long_text)
            RelationFormat.NUMBER -> context.getString(R.string.relation_format_number)
            RelationFormat.STATUS -> context.getString(R.string.relation_format_status)
            RelationFormat.TAG -> context.getString(R.string.relation_format_tag)
            RelationFormat.DATE -> context.getString(R.string.relation_format_date)
            RelationFormat.FILE -> context.getString(R.string.relation_format_file)
            RelationFormat.CHECKBOX -> context.getString(R.string.relation_format_checkbox)
            RelationFormat.URL -> context.getString(R.string.relation_format_url)
            RelationFormat.EMAIL -> context.getString(R.string.relation_format_email)
            RelationFormat.PHONE -> context.getString(R.string.relation_format_phone)
            RelationFormat.EMOJI -> context.getString(R.string.relation_format_emoji)
            RelationFormat.OBJECT -> context.getString(R.string.relation_format_object)
            RelationFormat.RELATIONS -> context.getString(R.string.relation_format_relation)
            RelationFormat.UNDEFINED -> context.getString(R.string.undefined)
        }
    }

    override fun getDefaultSpaceName(): String {
        return context.getString(R.string.onboarding_my_first_space)
    }

    override fun getAttachmentText(): String {
        return context.getString(R.string.attachment)
    }

    override fun getSpaceAccessTypeName(accessType: SpaceAccessType?): String {
        return when (accessType) {
            SpaceAccessType.PRIVATE -> context.getString(R.string.space_type_private_space)
            SpaceAccessType.DEFAULT -> context.getString(R.string.space_type_default_space)
            SpaceAccessType.SHARED -> context.getString(R.string.space_type_shared_space)
            null -> EMPTY_STRING_VALUE
        }
    }

    override fun getYesterday(): String {
        return context.getString(R.string.yesterday)
    }

    override fun getInitialSpaceName(): String {
        return context.getString(R.string.onboarding_my_first_space)
    }

    override fun getUntitledCreatorName(): String {
        return context.getString(R.string.unknown)
    }
}