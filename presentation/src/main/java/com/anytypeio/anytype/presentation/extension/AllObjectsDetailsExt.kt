package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.AllObjectsDetails
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.isValidObject
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.ext.mapToOptionObject
import com.anytypeio.anytype.core_models.ext.toBookmarkObject
import com.anytypeio.anytype.core_models.ext.toDateObject
import com.anytypeio.anytype.core_models.ext.toFileObject
import com.anytypeio.anytype.core_models.ext.toInternalFlagsObject
import com.anytypeio.anytype.core_models.ext.toObject

fun AllObjectsDetails.getStruct(id: Id): Struct? = details[id]
fun AllObjectsDetails.containsObject(id: Id): Boolean {
    return details.containsKey(id) && details[id]?.isValidObject() == true
}

fun AllObjectsDetails.getObject(id: Id): ObjectWrapper.Basic? {
    return details[id]?.toObject()
}

fun AllObjectsDetails.getOptionObject(id: Id): ObjectWrapper.Option? {
    return details[id]?.mapToOptionObject()
}

fun AllObjectsDetails.getFileObject(id: Id): ObjectWrapper.File? {
    return details[id]?.toFileObject()
}

fun AllObjectsDetails.getTypeObject(id: Id): ObjectWrapper.Type? {
    return details[id]?.mapToObjectWrapperType()
}

fun AllObjectsDetails.getDateObject(id: Id): ObjectWrapper.Date? {
    return details[id]?.toDateObject()
}

fun AllObjectsDetails.getBookmarkObject(id: Id): ObjectWrapper.Bookmark? {
    return details[id]?.toBookmarkObject()
}

fun AllObjectsDetails.getInternalFlagsObject(id: Id): ObjectWrapper.ObjectInternalFlags? {
    return details[id]?.toInternalFlagsObject()
}