package com.agileburo.anytype.domain.database

import com.agileburo.anytype.domain.database.model.*

object DatabaseMock {

    const val ID = "343253"

    fun getDatabaseView(id: String): DatabaseView {

        val details = listOf(
            Detail.Number(id = "21", name = "Id", show = true),
            Detail.Title(id = "22", name = "Name", show = true),
            Detail.Email(id = "24", name = "E-mail", show = true),
            Detail.Date(id = "25", name = "Date", show = true),
            Detail.Select(
                id = "26",
                name = "Select",
                select = setOf("select1", "select2", "select3"),
                show = true
            ),
            Detail.Multiple(
                id = "27",
                name = "Multiple",
                multiSelect = setOf(
                    "multiple1",
                    "multiple2",
                    "multiple3",
                    "multiple4",
                    "multiple5"
                ),
                show = true
            ),
            Detail.Person(
                id = "38",
                name = "Account",
                accounts = setOf(
                    Value(name = "Gennadiy Gusarov"),
                    Value(name = "Eduard Streltsov")
                ),
                show = false
            ),
            Detail.File(id = "49", name = "File", show = true),
            Detail.Bool(id = "510", name = "Bool", show = true),
            Detail.Link(id = "711", name = "Link", show = true),
            Detail.Phone(id = "3312", name = "Phone", show = true),
            Detail.Text(id = "213", name = "Phone", show = true),
            Detail.Text(id = "214", name = "Phone", show = true),
            Detail.Text(id = "215", name = "Phone", show = true),
            Detail.Text(id = "216", name = "Phone", show = true),
            Detail.Text(id = "217", name = "Phone", show = true),
            Detail.Text(id = "218", name = "Phone", show = true),
            Detail.Text(id = "219", name = "Phone", show = true),
            Detail.Text(id = "220", name = "Phone", show = true)
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

        val views = mutableListOf(
            Display(
                id = "1",
                type = ViewType.LIST,
                name = "Нобелевские лауреаты 2019",
                filters = listOf(
                    Filter(
                        detailId = "1",
                        value = "",
                        condition = FilterTypeCondition.NONE,
                        equality = FilterTypeEquality.EQUAL
                    )
                ),
                sorts = listOf(
                    Sort(detailId = "1", type = SortType.ASC),
                    Sort(detailId = "2", type = SortType.DESC)
                )
            )
        )

        val contentDatabaseView = ContentDatabaseView(
            data = data,
            details = details,
            databaseId = ID,
            displays = views
        )

        if (id == contentDatabaseView.databaseId) {
            return DatabaseView(content = contentDatabaseView)
        } else {
            throw RuntimeException("Wrong Id")
        }
    }
}