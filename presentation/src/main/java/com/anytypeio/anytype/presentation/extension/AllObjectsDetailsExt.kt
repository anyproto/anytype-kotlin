package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.ext.mapToOptionObject
import com.anytypeio.anytype.core_models.ext.toBookmarkObject
import com.anytypeio.anytype.core_models.ext.toDateObject
import com.anytypeio.anytype.core_models.ext.toFileObject
import com.anytypeio.anytype.core_models.ext.toInternalFlagsObject
import com.anytypeio.anytype.core_models.ext.toObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.objects.getProperType

fun ObjectViewDetails.getStruct(id: Id): Struct? = details[id]

fun ObjectViewDetails.getObject(id: Id): ObjectWrapper.Basic? {
    return details[id]?.toObject()
}

fun ObjectViewDetails.getOptionObject(id: Id): ObjectWrapper.Option? {
    return details[id]?.mapToOptionObject()
}

fun ObjectViewDetails.getFileObject(id: Id): ObjectWrapper.File? {
    return details[id]?.toFileObject()
}

fun ObjectViewDetails.getTypeObject(id: Id): ObjectWrapper.Type? {
    return details[id]?.mapToObjectWrapperType()
}

fun ObjectViewDetails.getDateObject(id: Id): ObjectWrapper.Date? {
    return details[id]?.toDateObject()
}

fun ObjectViewDetails.getBookmarkObject(id: Id): ObjectWrapper.Bookmark? {
    return details[id]?.toBookmarkObject()
}

fun ObjectViewDetails.getInternalFlagsObject(id: Id): ObjectWrapper.ObjectInternalFlags? {
    return details[id]?.toInternalFlagsObject()
}

fun ObjectViewDetails.getTypeForObject(currentObjectId: Id): ObjectWrapper.Type? {
    val currentObject = getObject(currentObjectId)
    val type = currentObject?.getProperType()
    if (type != null) {
        val objType = getTypeObject(type)
        if (objType != null) {
            return objType
        }
    }
    return null
}

suspend fun ObjectViewDetails.getTypeForObjectAndTargetTypeForTemplate(
    currentObjectId: Id,
    storeOfObjectTypes: StoreOfObjectTypes
): ObjectWrapper.Type? {
    val currentObject = getObject(currentObjectId)
    val type = currentObject?.getProperType()
    if (type != null) {
        val currType = getTypeObject(type)
        val effectiveType = if (currType?.uniqueKey == ObjectTypeIds.TEMPLATE) {
            currentObject.targetObjectType?.let { storeOfObjectTypes.get(it) }
        } else {
            currType
        }
        return effectiveType
    }
    return null
}