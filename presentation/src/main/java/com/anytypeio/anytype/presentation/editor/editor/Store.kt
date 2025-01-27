package com.anytypeio.anytype.presentation.editor.editor

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
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.getProperType
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.getNotIncludedRecommendedRelations
import com.anytypeio.anytype.presentation.relations.view
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * Reactive store
 * @param T stored type
 */
interface Store<T> {

    /**
     * @return streams of values
     */
    fun stream(): Flow<T>

    /**
     * @return current/last value
     */
    fun current(): T

    /**
     * Updates current values
     */
    suspend fun update(t: T)

    fun cancel()

    open class State<T>(initial: T) : Store<T> {

        private val state = MutableStateFlow(initial)

        override fun stream(): Flow<T> = state
        override fun current(): T = state.value
        override suspend fun update(t: T) {
            state.value = t
        }

        override fun cancel() {}
    }

    class Focus : State<Editor.Focus>(Editor.Focus.empty()) {
        override suspend fun update(t: Editor.Focus) {
            Timber.d("Update focus in store: $t")
            super.update(t)
        }
    }

    class Screen : State<List<BlockView>>(emptyList())

    class Details(val ctx: Id) : State<AllObjectsDetails>(AllObjectsDetails.EMPTY) {

        fun getCurrentObject() : ObjectWrapper.Basic? {
            return current().details[ctx].toObject()
        }

        fun getAsObject(target: Id) : ObjectWrapper.Basic? {
            return current().details[target].toObject()
        }

        fun getAsTypeObject(target: Id) : ObjectWrapper.Type? {
            return current().details[target]?.mapToObjectWrapperType()
        }

        fun getAsBookmarkObject(target: Id) : ObjectWrapper.Bookmark? {
            return current().details[target]?.toBookmarkObject()
        }
    }

    class ObjectRestrictions : State<List<ObjectRestriction>>(emptyList())
    class TextSelection : State<Editor.TextSelection>(Editor.TextSelection.empty())
}

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

suspend fun AllObjectsDetails.getObjRelationsViews(
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

suspend fun AllObjectsDetails.getRecommendedRelations(
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

fun AllObjectsDetails.getTypeForObject(currentObjectId: Id): ObjectWrapper.Type? {
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