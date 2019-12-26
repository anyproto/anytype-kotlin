package com.agileburo.anytype.presentation.databaseview.mapper

import com.agileburo.anytype.domain.database.model.Contact
import com.agileburo.anytype.domain.database.model.Tag
import com.agileburo.anytype.domain.database.model.*
import com.agileburo.anytype.presentation.databaseview.models.ListItem
import com.agileburo.anytype.presentation.databaseview.models.TagView
import com.agileburo.anytype.presentation.databaseview.models.*
import com.agileburo.anytype.presentation.databaseview.models.FilterView

fun Detail.toPresentation(): ColumnView =
    when (this) {
        is Detail.Title -> ColumnView.Title(id = this.id, name = this.name, show = this.show)
        is Detail.Number -> ColumnView.Number(id = this.id, name = this.name, show = this.show)
        is Detail.Text -> ColumnView.Text(id = this.id, name = this.name, show = this.show)
        is Detail.Date -> ColumnView.Date(id = this.id, name = this.name, show = this.show)
        is Detail.Select -> ColumnView.Select(
            id = this.id,
            name = this.name,
            select = this.select,
            show = this.show
        )
        is Detail.Multiple -> ColumnView.Multiple(
            id = this.id,
            name = this.name,
            multiSelect = this.multiSelect,
            show = this.show
        )
        is Detail.Person -> ColumnView.Person(
            id = this.id,
            name = this.name,
            accounts = this.accounts,
            show = this.show
        )
        is Detail.File -> ColumnView.File(id = this.id, name = this.name, show = this.show)
        is Detail.Bool -> ColumnView.Checkbox(id = this.id, name = this.name, show = this.show)
        is Detail.Link -> ColumnView.URL(id = this.id, name = this.name, show = this.show)
        is Detail.Email -> ColumnView.Email(id = this.id, name = this.name, show = this.show)
        is Detail.Phone -> ColumnView.Phone(id = this.id, name = this.name, show = this.show)
    }

fun ViewType.toPresentation(): TableType =
    when (this) {
        ViewType.GRID -> TableType.GRID
        ViewType.BOARD -> TableType.BOARD
        ViewType.GALLERY -> TableType.GALLERY
        ViewType.LIST -> TableType.LIST
    }

fun Display.toPresentation(): Representation {
    return Representation(
        id = this.id,
        name = this.name,
        type = this.type.toPresentation()
    )
}

fun DatabaseView.toPresentation(): Table =
    Table(
        id = this.content.databaseId,
        column = this.content.details.map { it.toPresentation() },
        representations = this.content.displays.map { it.toPresentation() },
        cell = this.content.data.map { hashMap: HashMap<String, Any> ->
            findTypeOfData(
                map = hashMap,
                details = this.content.details
            )
        }
    )

fun findTypeOfData(map: HashMap<String, Any>, details: List<Detail>): List<CellView> {
    val cells = mutableListOf<CellView>()
    map.keys.forEach { key: String ->
        details.firstOrNull { it.id == key }?.let { property ->
            cells.add(
                when (property) {
                    is Detail.Title -> CellView.Title(title = (map[key] as String))
                    is Detail.Text -> CellView.Text(text = (map[key] as String))
                    is Detail.Number -> CellView.Number(number = (map[key] as String))
                    is Detail.Date -> CellView.Date(date = (map[key] as Int))
                    is Detail.Select -> CellView.Select(select = (map[key] as String))
                    //todo add proper casting
                    is Detail.Multiple -> CellView.Multiple(multiple = (map[key] as Array<String>))
                    //todo add cast to HashMap
                    is Detail.Person -> CellView.Person(accounts = hashMapOf())
                    is Detail.File -> CellView.File(file = (map[key] as String))
                    is Detail.Bool -> CellView.Checked(isChecked = (map[key] as Boolean))
                    is Detail.Link -> CellView.Link(link = (map[key] as String))
                    is Detail.Email -> CellView.Email(email = (map[key] as String))
                    is Detail.Phone -> CellView.Phone(phone = (map[key] as String))
                }
            )
        }
    }
    return cells
}

fun Contact.toPresentation(): ListItem =
    ListItem(
        id = this.id,
        name = this.name,
        date = this.date,
        icon = this.icon,
        tags = this.tags.map { it.toPresentation() })

fun Tag.toPresentation(): TagView =
    TagView(
        id = this.id,
        name = this.name
    )

fun Filter.toPresentation(): FilterView =
    FilterView(
        id = this.detailId,
        name = this.value as String
    )