package com.anytypeio.anytype.ui.objects.types.pickers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper

interface ObjectTypeSelectionListener {
    fun onSelectObjectType(objType: ObjectWrapper.Type)
}

interface ObjectTypeUpdateListener {
    fun onUpdateObjectType(objType: ObjectWrapper.Type)
}

interface WidgetObjectTypeListener {
    fun onCreateWidgetObject(objType: ObjectWrapper.Type, widgetId: Id, source: Id)
}

interface WidgetSourceTypeListener {
    fun onSetNewWidgetSource(objType: ObjectWrapper.Type, widgetId: Id)
}