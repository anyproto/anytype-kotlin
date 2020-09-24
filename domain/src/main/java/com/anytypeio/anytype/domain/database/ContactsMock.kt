package com.anytypeio.anytype.domain.database

import com.anytypeio.anytype.domain.database.model.Contact
import com.anytypeio.anytype.domain.database.model.Tag

object ContactsMock {

    val CONTACTS = listOf(
        Contact(
            id = "kfgonk;lmkmvkld8",
            name = "Fred Scott",
            date = 1571328513000,
            tags = listOf(
                Tag(
                    "777",
                    "Family"
                )
            ),
            icon = ""
        ),
        Contact(
            id = "bjcbdjdfb",
            name = "Tomas Hale",
            date = 1492387200000,
            tags = listOf(
                Tag("333", "Team"),
                Tag("111", "Core")
            ),
            icon = ""
        ),
        Contact(
            id = "sadfsdas",
            name = "Anton Lurie",
            date = 1574351513000,
            tags = listOf(
                Tag("333", "Team"),
                Tag("222", "Design")
            ),
            icon = ""
        ),
        Contact(
            id = "hfjhfgjkshjdh",
            name = "Anna Lee",
            date = 1571328513000,
            tags = listOf(
                Tag("333", "Team"),
                Tag("555", "Product")
            ),
            icon = ""
        ),
        Contact(
            id = "zfjh3hjdh",
            name = "Neil Keaton",
            date = 1571328513000,
            tags = listOf(
                Tag("777", "Family")
            ),
            icon = ""
        ),
        Contact(
            id = "dshjfhskdh",
            name = "Greg Phillips",
            date = 1571328513000,
            tags = listOf(
                Tag("333", "Team"),
                Tag(
                    "444",
                    "Programming"
                )
            ),
            icon = ""
        ),
        Contact(
            id = "sadwcdweew",
            name = "Roman Blum",
            date = 1571328513000,
            tags = listOf(
                Tag("333", "Team"),
                Tag(
                    "444",
                    "Programming"
                )
            ),
            icon = ""
        )
    )
}