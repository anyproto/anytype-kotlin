package com.anytypeio.anytype.domain.resources

import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType

interface StringResourceProvider {
    fun getRelativeDateName(relativeDate: RelativeDate): String
    fun getDeletedObjectTitle(): String
    fun getUntitledObjectTitle(): String
    fun getSetOfObjectsTitle(): String
    fun getPropertiesFormatPrettyString(format: RelationFormat): String
    fun getDefaultSpaceName(): String
    fun getAttachmentText(): String
    fun getSpaceAccessTypeName(accessType: SpaceAccessType?): String
    fun getYesterday(): String
    fun getInitialSpaceName(): String
    fun getUntitledCreatorName(): String
    fun getMessagesCountText(count: Int): String
}