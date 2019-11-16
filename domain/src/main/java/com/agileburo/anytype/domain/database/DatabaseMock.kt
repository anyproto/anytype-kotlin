package com.agileburo.anytype.domain.database

import com.agileburo.anytype.domain.database.model.*

object DatabaseMock {

    fun getDatabaseView(id: String): DatabaseView {

        val properties = listOf(
            Property.Number(id = "1", name = "Id"),
            Property.Title(id = "2", name = "Name"),
            Property.Email(id = "4", name = "E-mail"),
            Property.Date(id = "5", name = "Date"),
            Property.Select(
                id = "6",
                name = "Select",
                select = setOf("select1", "select2", "select3")
            ),
            Property.Multiple(
                id = "7",
                name = "Multiple",
                multiSelect = setOf(
                    "multiple1",
                    "multiple2",
                    "multiple3",
                    "multiple4",
                    "multiple5"
                )
            ),
            Property.Account(
                id = "8",
                name = "Account",
                accounts = setOf(
                    Value(name = "Gennadiy Gusarov"),
                    Value(name = "Eduard Streltsov")
                )
            ),
            Property.File(id = "9", name = "File"),
            Property.Bool(id = "10", name = "Bool"),
            Property.Link(id = "11", name = "Link"),
            Property.Phone(id = "12", name = "Phone")
        )

        val data1: HashMap<String, Any> = hashMapOf(
            "1" to "1",
            "2" to "Valentin Ivanov",
            "4" to "ivanov@gmail.com",
            "5" to 1420200661,
            "6" to "select1",
            "11" to "httpto//anytype.io",
            "12" to "+7 (1234) 5678910",
            "7" to arrayOf("value1", "value2", "value3"),
            "10" to true,
            "8" to { "name" to "Eduard Streltsov" }
        )

        val data2: HashMap<String, Any> = hashMapOf(
            "1" to "2",
            "2" to "Eduard Streltsov",
            "4" to "streltsov@gmail.com",
            "5" to 1420200661,
            "6" to "select2",
            "11" to "ftp://anytype.io"
        )

        val data3: HashMap<String, Any> = hashMapOf(
            "1" to "3",
            "2" to "Gennadiy Gusarov",
            "4" to "gusarov@gmail.com",
            "5" to 1420200662,
            "6" to "select3",
            "11" to "telnet://anytype.io"
        )

        val data4: HashMap<String, Any> = hashMapOf(
            "1" to "4",
            "2" to "Georgiy Zharkov",
            "4" to "zharkov@gmail.com",
            "5" to 1420200662,
            "6" to "select4",
            "11" to "https://anytype.io"
        )

        val data5: HashMap<String, Any> = hashMapOf(
            "1" to "5",
            "2" to "Pyotr Petrov",
            "4" to "ppetrov@gmail.com",
            "5" to 1420200663,
            "6" to "select5"
        )

        val data = listOf(data1, data2, data3, data4, data5)

        val views = listOf(
            View(
                id = "1",
                type = ViewType.GRID,
                name = "Table",
                filters = listOf(
                    Filter(
                        propertyId = "1",
                        value = "",
                        condition = FilterTypeCondition.NONE,
                        equality = FilterTypeEquality.EQUAL
                    )
                ),
                sorts = listOf(
                    Sort(propertyId = "1", type = SortType.ASC),
                    Sort(propertyId = "2", type = SortType.DESC)
                )
            )
        )

        val contentDatabaseView = ContentDatabaseView(
            data = data,
            properties = properties,
            view = "452088",
            views = views
        )

        return DatabaseView(content = contentDatabaseView)
    }
}