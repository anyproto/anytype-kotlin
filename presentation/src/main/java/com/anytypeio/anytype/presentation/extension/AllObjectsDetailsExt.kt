package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.ObjectViewDetails
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
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.objects.getProperType
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.getNotIncludedRecommendedRelations
import com.anytypeio.anytype.presentation.relations.view

fun ObjectViewDetails.getStruct(id: Id): Struct? = details[id]
fun ObjectViewDetails.containsObject(id: Id): Boolean {
    return details.containsKey(id) && details[id]?.isValidObject() == true
}

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

suspend fun ObjectViewDetails.getObjRelationsViews(
    ctx: Id,
    storeOfRelations: StoreOfRelations,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder
): List<ObjectRelationView> {
    val currentObject = getObject(ctx)
    if (currentObject == null || !currentObject.isValid) return emptyList()
    val keys = currentObject.map.keys.toList()
    return storeOfRelations.getByKeys(keys).map {
        it.view(
            details = this,
            values = currentObject.map,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            isFeatured = currentObject.featuredRelations.contains(it.key)
        )
    }
}

suspend fun ObjectViewDetails.getRecommendedRelations(
    ctx: Id,
    storeOfRelations: StoreOfRelations,
    fieldParser: FieldParser,
    urlBuilder: UrlBuilder
): List<ObjectRelationView> {
    val currentObject = getObject(ctx)
    if (currentObject == null || !currentObject.isValid) return emptyList()
    val typeObjectId = currentObject.getProperType()
    if (typeObjectId == null) return emptyList()
    val typeObject = getTypeObject(typeObjectId)
    if (typeObject == null) return emptyList()
    val recommendedRelations = typeObject.recommendedRelations
    val notIncludedRecommendedRelations = getNotIncludedRecommendedRelations(
        relationKeys = currentObject.map.keys,
        recommendedRelations = recommendedRelations,
        storeOfRelations = storeOfRelations
    )
    return notIncludedRecommendedRelations.map {
        it.view(
            details = this,
            values = currentObject.map,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            isFeatured = currentObject.featuredRelations.contains(it.key)
        )
    }
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