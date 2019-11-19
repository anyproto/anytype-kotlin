package com.agileburo.anytype.presentation.databaseview.mapper

import com.agileburo.anytype.domain.database.model.DatabaseView
import com.agileburo.anytype.domain.database.model.Property
import com.agileburo.anytype.domain.database.model.DisplayView
import com.agileburo.anytype.domain.database.model.ViewType
import com.agileburo.anytype.presentation.databaseview.models.*

fun Property.toPresentation(): Column =
    when (this) {
        is Property.Title -> Column.Title(id = this.id, name = this.name)
        is Property.Number -> Column.Number(id = this.id, name = this.name)
        is Property.Text -> Column.Text(id = this.id, name = this.name)
        is Property.Date -> Column.Date(id = this.id, name = this.name)
        is Property.Select -> Column.Select(id = this.id, name = this.name, select = this.select)
        is Property.Multiple -> Column.Multiple(
            id = this.id,
            name = this.name,
            multiSelect = this.multiSelect
        )
        is Property.Account -> Column.Account(
            id = this.id,
            name = this.name,
            accounts = this.accounts
        )
        is Property.File -> Column.File(id = this.id, name = this.name)
        is Property.Bool -> Column.Bool(id = this.id, name = this.name)
        is Property.Link -> Column.Link(id = this.id, name = this.name)
        is Property.Email -> Column.Email(id = this.id, name = this.name)
        is Property.Phone -> Column.Phone(id = this.id, name = this.name)
    }

fun ViewType.toPresentation(): TableType =
    when (this) {
        ViewType.GRID -> TableType.GRID
        ViewType.BOARD -> TableType.BOARD
        ViewType.GALLERY -> TableType.GALLERY
        ViewType.LIST -> TableType.LIST
    }

fun DisplayView.toPresentation(): Representation {
    return Representation(
        id = this.id,
        name = this.name,
        type = this.type.toPresentation()
    )
}

fun DatabaseView.toPresentation(): Table =
    Table(
        id = this.content.view,
        column = this.content.properties.map { it.toPresentation() },
        representations = this.content.displayViews.map { it.toPresentation() },
        cell = this.content.data.map { hashMap: HashMap<String, Any> ->
            findTypeOfData(
                map = hashMap,
                properties = this.content.properties
            )
        }
    )

fun findTypeOfData(map: HashMap<String, Any>, properties: List<Property>): List<Cell> {
    val cells = mutableListOf<Cell>()
    map.keys.forEach { key: String ->
        properties.firstOrNull { it.id == key }?.let { property ->
            cells.add(
                when (property) {
                    is Property.Title -> Cell.Title(title = (map[key] as String))
                    is Property.Text -> Cell.Text(text = (map[key] as String))
                    is Property.Number -> Cell.Number(number = (map[key] as String))
                    is Property.Date -> Cell.Date(date = (map[key] as Int))
                    is Property.Select -> Cell.Select(select = (map[key] as String))
                    //todo add proper casting
                    is Property.Multiple -> Cell.Multiple(multiple = (map[key] as Array<String>))
                    //todo add cast to HashMap
                    is Property.Account -> Cell.Account(accounts = hashMapOf())
                    is Property.File -> Cell.File(file = (map[key] as String))
                    is Property.Bool -> Cell.Checked(isChecked = (map[key] as Boolean))
                    is Property.Link -> Cell.Link(link = (map[key] as String))
                    is Property.Email -> Cell.Email(email = (map[key] as String))
                    is Property.Phone -> Cell.Phone(phone = (map[key] as String))
                }
            )
        }
    }
    return cells
}
