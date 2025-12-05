package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

object SupportedLayouts {

    /**
     * Base supported layouts (without CHAT_DERIVED)
     */
    private val baseLayouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK,
        ObjectType.Layout.AUDIO,
        ObjectType.Layout.PDF,
        ObjectType.Layout.OBJECT_TYPE
    )
    
    /**
     * Default layouts list (includes CHAT_DERIVED for data spaces).
     * For space-aware behavior, use [getLayouts] instead.
     */
    private val layouts = baseLayouts + listOf(ObjectType.Layout.CHAT_DERIVED)

    val editorLayouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK
    )
    val fileLayouts = listOf(
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.AUDIO,
        ObjectType.Layout.PDF
    )

    /**
     * System layouts that should not be shown in general object lists.
     * Note: CHAT_DERIVED is context-dependent - see [getSystemLayouts]
     */
    private val baseSystemLayouts = listOf(
        ObjectType.Layout.RELATION,
        ObjectType.Layout.RELATION_OPTION,
        ObjectType.Layout.DASHBOARD,
        ObjectType.Layout.SPACE,
        ObjectType.Layout.SPACE_VIEW,
        ObjectType.Layout.TAG
    )

    /**
     * Default system layouts (without CHAT_DERIVED).
     * For space-aware behavior, use [getSystemLayouts] instead.
     */
    val systemLayouts = baseSystemLayouts

    /**
     * Layouts that can be created through the "New Object" flow.
     * Note: CHAT_DERIVED is context-dependent - see [getCreateObjectLayouts]
     */
    private val baseCreateObjectLayouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK
    )

    /**
     * Default create object layouts (includes CHAT_DERIVED for data spaces).
     * For space-aware behavior, use [getCreateObjectLayouts] instead.
     */
    val createObjectLayouts = baseCreateObjectLayouts + listOf(ObjectType.Layout.CHAT_DERIVED)

    /**
     * Handling create-object layouts for editor. Converting an object to a chat is not currently supported.
     */
    val editorCreateObjectLayouts = baseCreateObjectLayouts


    val dateLayouts = listOf(
        ObjectType.Layout.DATE
    )

    val addAsLinkToLayouts = editorLayouts + listOf(
        ObjectType.Layout.COLLECTION
    )

    val globalSearchLayouts = createObjectLayouts + fileLayouts + dateLayouts + listOf(ObjectType.Layout.OBJECT_TYPE)

    val widgetsLayouts = layouts + dateLayouts

    val lastOpenObjectLayouts = layouts + dateLayouts

    /**
     * Layouts that are excluded from being shown as space types in the widget section.
     * This includes system layouts, date layouts, object type layout, and participant layout.
     */
    val excludedSpaceTypeLayouts = systemLayouts + dateLayouts + listOf(
        ObjectType.Layout.OBJECT_TYPE,
        ObjectType.Layout.PARTICIPANT
    )

    fun isSupportedForWidgets(layout: ObjectType.Layout?) : Boolean {
        return widgetsLayouts.contains(layout)
    }

    fun isFileLayout(layout: ObjectType.Layout?) : Boolean {
        return fileLayouts.contains(layout)
    }

    /**
     * Get system layouts for a specific space context.
     *
     * In chat and 1-1 spaces, CHAT_DERIVED is a system layout.
     * In data spaces, CHAT_DERIVED should be treated as a regular object type.
     *
     * @param spaceUxType The UX type of the current space
     * @return List of layouts that should be filtered out as system layouts
     */
    fun getSystemLayouts(spaceUxType: SpaceUxType?): List<ObjectType.Layout> {
        return when (spaceUxType) {
            SpaceUxType.CHAT, SpaceUxType.ONE_TO_ONE -> 
                baseSystemLayouts + listOf(ObjectType.Layout.CHAT_DERIVED, ObjectType.Layout.CHAT)
            else -> baseSystemLayouts
        }
    }

    /**
     * Get create object layouts for a specific space context.
     *
     * In data spaces, users should be able to create CHAT_DERIVED objects.
     * In chat and 1-1 spaces, CHAT_DERIVED creation is handled separately.
     *
     * @param spaceUxType The UX type of the current space
     * @return List of layouts that can be created in the "New Object" flow
     */
    fun getCreateObjectLayouts(spaceUxType: SpaceUxType?): List<ObjectType.Layout> {
        return when (spaceUxType) {
            SpaceUxType.CHAT, SpaceUxType.ONE_TO_ONE -> baseCreateObjectLayouts
            else -> baseCreateObjectLayouts + listOf(ObjectType.Layout.CHAT_DERIVED)
        }
    }

    /**
     * Get supported layouts for a specific space context.
     *
     * In data spaces, CHAT_DERIVED objects should be visible and manageable.
     * In chat and 1-1 spaces, CHAT_DERIVED is handled through special UI.
     *
     * @param spaceUxType The UX type of the current space
     * @return List of layouts that are supported for display/interaction
     */
    fun getLayouts(spaceUxType: SpaceUxType? = null): List<ObjectType.Layout> {
        return when (spaceUxType) {
            SpaceUxType.CHAT, SpaceUxType.ONE_TO_ONE -> baseLayouts
            else -> baseLayouts + listOf(ObjectType.Layout.CHAT_DERIVED)
        }
    }

    /**
     * Get layouts for object search (global search, link to, etc.).
     * 
     * Combines base layouts, file layouts, date layouts, and object type layout,
     * filtered by space UX type.
     * 
     * @param spaceUxType The UX type of the current space
     * @return List of layouts for object search operations
     */
    fun getObjectSearchLayouts(spaceUxType: SpaceUxType? = null): List<ObjectType.Layout> {
        return getLayouts(spaceUxType)
            .plus(fileLayouts)
            .plus(dateLayouts)
            .plus(listOf(ObjectType.Layout.OBJECT_TYPE))
            .distinct()
    }
}
