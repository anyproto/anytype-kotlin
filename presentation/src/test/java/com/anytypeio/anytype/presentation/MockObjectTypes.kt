package com.anytypeio.anytype.presentation

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds.AUDIO
import com.anytypeio.anytype.core_models.ObjectTypeIds.BOOKMARK
import com.anytypeio.anytype.core_models.ObjectTypeIds.DASHBOARD
import com.anytypeio.anytype.core_models.ObjectTypeIds.DATE
import com.anytypeio.anytype.core_models.ObjectTypeIds.FILE
import com.anytypeio.anytype.core_models.ObjectTypeIds.NOTE
import com.anytypeio.anytype.core_models.ObjectTypeIds.OBJECT_TYPE
import com.anytypeio.anytype.core_models.ObjectTypeIds.PAGE
import com.anytypeio.anytype.core_models.ObjectTypeIds.PROFILE
import com.anytypeio.anytype.core_models.ObjectTypeIds.RELATION
import com.anytypeio.anytype.core_models.ObjectTypeIds.RELATION_OPTION
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectTypeIds.TASK
import com.anytypeio.anytype.core_models.ObjectTypeIds.TEMPLATE
import com.anytypeio.anytype.core_models.ObjectTypeIds.VIDEO
import com.anytypeio.anytype.core_models.ObjectTypeIds.WORKSPACE
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.test_utils.MockDataFactory

object MockObjectTypes {

    val objectTypeSet = StubObjectType(
        id = SET,
        name = "Set",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.SET.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Collection of objects with equal types and relations. Database experience based on all objects in Anytype",
        isReadOnly = true
    )

    val objectTypeAudio = StubObjectType(
        id = AUDIO,
        name = "Audio",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.FILE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Auto-generated object from .wav, .mp3, .ogg files added to Anytype. Sound when recorded, with ability to reproduce",
        isReadOnly = true,
        iconEmoji = "🎵"
    )

    val objectTypeVideo = StubObjectType(
        id = VIDEO,
        name = "Video",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.FILE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Auto-generated object from .mpeg-4 files added to Anytype. The recording of moving visual images",
        isReadOnly = true,
        iconEmoji = "📽"
    )

    val objectTypeFile = StubObjectType(
        id = FILE,
        name = "File",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.FILE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Auto-generated object from files added to Anytype. Computer resource for recording data in a computer storage device",
        isReadOnly = true,
        iconEmoji = "🗂️"
    )

    val objectTypeBookmark = StubObjectType(
        id = BOOKMARK,
        name = "Bookmark",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.PAGE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "URL that is stored as Object and may be categorised and linked with objects",
        isReadOnly = true,
        iconEmoji = "🔖"
    )

    val objectTypeDashboard = StubObjectType(
        id = DASHBOARD,
        name = "Dashboard",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.HOME.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Internal home dashboard with favourite objects",
        isReadOnly = true,
        isHidden = true,
        iconEmoji = ""
    )

    val objectTypeDate = StubObjectType(
        id = DATE,
        name = "Date",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.DATE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Gregorian calendar date",
        isReadOnly = true,
        iconEmoji = "📅"
    )

    val objectTypeNote = StubObjectType(
        id = NOTE,
        name = "Note",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.PAGE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Blank canvas with no Title. A brief record of points written down as an aid to memory",
        isReadOnly = true,
        iconEmoji = "📝"
    )

    val objectTypeTask = StubObjectType(
        id = TASK,
        name = "Task",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.PAGE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "A piece of work to be done or undertaken",
        isReadOnly = true,
        iconEmoji = "✅"
    )

    val objectTypeType = StubObjectType(
        id = OBJECT_TYPE,
        name = "Type",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(
            SmartBlockType.CUSTOM_OBJECT_TYPE.code.toDouble(),
            SmartBlockType.BUNDLED_OBJECT_TYPE.code.toDouble()
        ),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Object that contains a definition of some object type",
        isReadOnly = true,
        isHidden = true,
        iconEmoji = "🥚"
    )

    val objectTypePage = StubObjectType(
        id = PAGE,
        name = "Page",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.PAGE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Blank canvas with Title",
        isReadOnly = true,
        iconEmoji = "📄"
    )

    val objectTypeHuman = StubObjectType(
        id = PROFILE,
        name = "Human",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(
            SmartBlockType.PAGE.code.toDouble(),
            SmartBlockType.PROFILE_PAGE.code.toDouble()
        ),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Homo sapiens",
        isReadOnly = true,
        iconEmoji = "🧍"
    )

    val objectTypeRelation = StubObjectType(
        id = RELATION,
        name = "Relation",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(
            SmartBlockType.BUNDLED_RELATION.code.toDouble(),
            SmartBlockType.SUB_OBJECT.code.toDouble()
        ),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Meaningful connection between objects",
        isReadOnly = true,
        isHidden = true,
        iconEmoji = "🔗"
    )

    val objectTypeRelationOption = StubObjectType(
        id = RELATION_OPTION,
        name = "Relation option",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.SUB_OBJECT.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Object that contains a relation option",
        isReadOnly = true,
        isHidden = true,
        iconEmoji = "🥚"
    )

    val objectTypeSpace = StubObjectType(
        id = WORKSPACE,
        name = "Space",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.WORKSPACE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Space for sharing",
        isReadOnly = true,
        isHidden = true,
        iconEmoji = "🌎"
    )

    val objectTypeTemplate = StubObjectType(
        id = TEMPLATE,
        name = "Template",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.TEMPLATE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Sample object that has already some details in place and used to create objects from",
        isReadOnly = true,
        isHidden = true,
        iconEmoji = ""
    )

    val objectTypeCustom = StubObjectType(
        id = MockDataFactory.randomString(),
        name = "Custom object type",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.PAGE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Custom object type description",
        iconEmoji = "💯"
    )

    val objectTypeCustomDeleted = StubObjectType(
        id = MockDataFactory.randomString(),
        name = "Custom object type deleted",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.PAGE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Custom object type description",
        iconEmoji = "",
        isDeleted = true
    )
    val objectTypeCustomArchived = StubObjectType(
        id = MockDataFactory.randomString(),
        name = "Custom object type archived",
        objectType = OBJECT_TYPE,
        smartBlockTypes = listOf(SmartBlockType.PAGE.code.toDouble()),
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        description = "Custom object type description",
        iconEmoji = "",
        isArchived = true
    )

    val objectTypeList = listOf(
        objectTypeAudio, objectTypeType, objectTypeDate,
        objectTypeSpace, objectTypeBookmark, objectTypeCustom,
        objectTypeDashboard, objectTypeFile, objectTypeHuman,
        objectTypeNote, objectTypePage, objectTypeRelation,
        objectTypeRelationOption, objectTypeSet, objectTypeTask,
        objectTypeTemplate, objectTypeVideo,
        objectTypeCustomArchived, objectTypeCustomDeleted
    )
}