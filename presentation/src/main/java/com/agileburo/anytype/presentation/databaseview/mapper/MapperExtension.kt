package com.agileburo.anytype.presentation.databaseview.mapper

import com.agileburo.anytype.domain.database.model.DatabaseView
import com.agileburo.anytype.domain.database.model.DisplayView
import com.agileburo.anytype.domain.database.model.Property
import com.agileburo.anytype.domain.database.model.ViewType
import com.agileburo.anytype.presentation.databaseview.models.*

fun Property.toPresentation(): ColumnView =
    when (this) {
        is Property.Title -> ColumnView.Title(id = this.id, name = this.name)
        is Property.Number -> ColumnView.Number(id = this.id, name = this.name)
        is Property.Text -> ColumnView.Text(id = this.id, name = this.name)
        is Property.Date -> ColumnView.Date(id = this.id, name = this.name)
        is Property.Select -> ColumnView.Select(
            id = this.id,
            name = this.name,
            select = this.select
        )
        is Property.Multiple -> ColumnView.Multiple(
            id = this.id,
            name = this.name,
            multiSelect = this.multiSelect
        )
        is Property.Account -> ColumnView.Account(
            id = this.id,
            name = this.name,
            accounts = this.accounts
        )
        is Property.File -> ColumnView.File(id = this.id, name = this.name)
        is Property.Bool -> ColumnView.Bool(id = this.id, name = this.name)
        is Property.Link -> ColumnView.Link(id = this.id, name = this.name)
        is Property.Email -> ColumnView.Email(id = this.id, name = this.name)
        is Property.Phone -> ColumnView.Phone(id = this.id, name = this.name)
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

fun findTypeOfData(map: HashMap<String, Any>, properties: List<Property>): List<CellView> {
    val cells = mutableListOf<CellView>()
    map.keys.forEach { key: String ->
        properties.firstOrNull { it.id == key }?.let { property ->
            cells.add(
                when (property) {
                    is Property.Title -> CellView.Title(title = (map[key] as String))
                    is Property.Text -> CellView.Text(text = (map[key] as String))
                    is Property.Number -> CellView.Number(number = (map[key] as String))
                    is Property.Date -> CellView.Date(date = (map[key] as Int))
                    is Property.Select -> CellView.Select(select = (map[key] as String))
                    //todo add proper casting
                    is Property.Multiple -> CellView.Multiple(multiple = (map[key] as Array<String>))
                    //todo add cast to HashMap
                    is Property.Account -> CellView.Account(accounts = hashMapOf())
                    is Property.File -> CellView.File(file = (map[key] as String))
                    is Property.Bool -> CellView.Checked(isChecked = (map[key] as Boolean))
                    is Property.Link -> CellView.Link(link = (map[key] as String))
                    is Property.Email -> CellView.Email(email = (map[key] as String))
                    is Property.Phone -> CellView.Phone(phone = (map[key] as String))
                }
            )
        }
    }
    return cells
}
