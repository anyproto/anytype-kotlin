package com.anytypeio.anytype.feature_allcontent.ui

import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab

fun AllContentMode.text(): String {
    return when (this) {
        AllContentMode.AllContent -> "All content"
        AllContentMode.Unlinked -> "Only unlinked"
    }
}

fun AllContentTab.text(): String {
    return when (this) {
        AllContentTab.OBJECTS -> "Objects"
        AllContentTab.FILES -> "Files"
        AllContentTab.MEDIA -> "Media"
        AllContentTab.BOOKMARKS -> "Bookmarks"
        AllContentTab.TYPES -> "Object Types"
        AllContentTab.RELATIONS -> "Relations"
    }
}
fun AllContentSort.text(): String {
    return when (this) {
        is AllContentSort.ByName -> "Name"
        is AllContentSort.ByDateUpdated -> "Date updated"
        is AllContentSort.ByDateCreated -> "Date created"
    }
}