package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.canChangeReaderToWriter
import com.anytypeio.anytype.domain.`object`.canChangeWriterToReader
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceMemberView

fun List<ObjectWrapper.SpaceMember>.toView(
    spaceView: ObjectWrapper.SpaceView?,
    urlBuilder: UrlBuilder,
    isCurrentUserOwner: Boolean,
    account: Id? = null
): List<ShareSpaceMemberView> {
    return this.mapNotNull { spaceMember ->
        if (spaceView == null) return@mapNotNull null
        val canChangeReaderToWriter = spaceView.canChangeReaderToWriter(participants = this)
        val canChangeWriterToReader = spaceView.canChangeWriterToReader(participants = this)
        ShareSpaceMemberView.fromObject(
            obj = spaceMember,
            urlBuilder = urlBuilder,
            canChangeWriterToReader = canChangeWriterToReader,
            canChangeReaderToWriter = canChangeReaderToWriter,
            includeRequests = isCurrentUserOwner,
            account = account
        )
    }
}