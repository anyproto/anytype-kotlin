package com.agileburo.anytype.presentation.databaseview

import com.agileburo.anytype.presentation.databaseview.models.TagView
import com.agileburo.anytype.presentation.databaseview.models.KanbanColumnView
import com.agileburo.anytype.presentation.databaseview.models.KanbanRowView
import java.util.*

typealias KanbanRows = MutableList<KanbanRowView>
typealias KanbanColumns = MutableList<KanbanColumnView>

object MockFactory {

    fun makeKanbanBoard(): KanbanColumns {
        return mutableListOf(
            KanbanColumnView(
                name = "Research",
                rows = makeRows0()
            )
            ,
            KanbanColumnView(
                name = "To Do & Hold",
                rows = makeRows1()
            ),
            KanbanColumnView(
                name = "In Progress",
                rows = makeRows2()
            ),
            KanbanColumnView(
                name = "Review",
                rows = makeRows3()
            ),
            KanbanColumnView(
                name = "Done",
                rows = makeRows4()
            )
        )
    }

    private fun makeRows0(): MutableList<KanbanRowView> {
        return mutableListOf(
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Launcher speed up",
                assign = "Tomas",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "speed"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "core"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Find new branding font",
                assign = "Anton",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "design"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "brand"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "New \uD83D\uDE80 Rocket block type",
                assign = "Anna",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "new feature"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "core"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Emoji prediction",
                assign = "Ilya",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "fun"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "core"
                    )
                )
            ),
            KanbanRowView.KanbanAddNewItemView(
                id = randomUuid()
            )
        )
    }

    private fun makeRows1(): MutableList<KanbanRowView> {
        return mutableListOf(
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Screencast demo",
                assign = "Charlotte",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "acquisition"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Scale servers",
                assign = "Greg",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "speed"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "infra"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Share to web",
                assign = "Helen",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "retention"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "infra"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "core"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "New mobile layout",
                assign = "Rayan",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "iOS"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "Android"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "design"
                    )
                )
            )
        )
    }

    private fun makeRows2(): MutableList<KanbanRowView> {
        return mutableListOf(
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "⚡⚡Public Alpha Release⚡⚡",
                assign = "Roman",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "acquisition"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "metrics"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Collaboration",
                assign = "Fred",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "new feature"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "core"
                    )
                )
            )
        )
    }

    private fun makeRows3(): MutableList<KanbanRowView> {
        return mutableListOf(
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Dark Theme",
                assign = "Dima",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "iOS"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "layout"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "design"
                    )
                )
            )
        )
    }

    private fun makeRows4(): MutableList<KanbanRowView> {
        return mutableListOf(
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Magic pin",
                assign = "Anton",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "design"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "core"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "Media blocks",
                assign = "Rayan",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "core"
                    )
                )
            ),
            KanbanRowView.KanbanDemoView(
                id = randomUuid(),
                title = "New Site",
                assign = "Rayan",
                tags = listOf(
                    TagView(
                        id = randomUuid(),
                        name = "brand"
                    ),
                    TagView(
                        id = randomUuid(),
                        name = "design"
                    )
                )
            )
        )
    }

    val OLD_LIST = listOf(
        KanbanRowView.KanbanBookmarkView(
            id = randomUuid(),
            title = "How To Evaluate Your Own Writing",
            subtitle = "What advertising taught me about recognizing good ideas",
            url = "https://medium.com/better-marketing/how-to-evaluate-your-own-writing-dd3f88a7e892",
            image = "https://miro.medium.com/max/1400/0*t9DZwIuMlP2QdAZ-",
            logo = "https://miro.medium.com/max/390/1*emiGsBgJu2KHWyjluhKXQw.png"
        ),
        KanbanRowView.KanbanPeopleView(
            icon = "https://scontent-arn2-2.xx.fbcdn.net/v/t1.0-9/185628_130377327035995_6141574_n.jpg?_nc_cat=109&_nc_oc=AQmBuuXAoyoxCd45VuoLz9wP3vpRdbbTWzYvJPmNrQxHSWoNXJHXOE9HQN1fD-_QSsU&_nc_ht=scontent-arn2-2.xx&oh=1ff5fc9e33703bb5f0cf2c50c6c95866&oe=5DDC6F39",
            id = randomUuid(),
            category = "Development",
            name = "Evgenii Kozlov"
        ),
        KanbanRowView.KanbanFileView(
            icon = "",
            id = randomUuid(),
            category = "Readings",
            title = "Sociology reader"
        ),
        KanbanRowView.KanbanTaskView(
            id = randomUuid(),
            category = "Readings",
            title = "Read about Actor-Network theory",
            checked = true
        ),
        KanbanRowView.KanbanPageView(
            id = randomUuid(),
            category = "Documents",
            title = "Interesting books",
            icon = "https://d4804za1f1gw.cloudfront.net/wp-content/uploads/sites/60/2018/08/17113033/books_and_coffee_blog.jpg"
        ),
        KanbanRowView.KanbanPeopleView(
            icon = "https://scontent-arn2-1.xx.fbcdn.net/v/t1.0-9/46768337_2261389860537871_1842699467162124288_o.jpg?_nc_cat=102&_nc_oc=AQkhaA2BQtVM9Bjt62BA2qi29_Jf03kEzXb8wktuWnLMfxDXiFylurQl7DF7BxUQoOU&_nc_ht=scontent-arn2-1.xx&oh=183d54f7a2406a0e80837e4edb83ab3e&oe=5DE56780",
            id = randomUuid(),
            category = "Development",
            name = "Konstantin Ivanov"
        ),
        KanbanRowView.KanbanFileView(
            icon = "",
            id = randomUuid(),
            category = "Readings",
            title = "Sociology reader"
        ),
        KanbanRowView.KanbanTaskView(
            id = randomUuid(),
            category = "Readings",
            title = "Read about Actor-Network theory",
            checked = true
        ),
        KanbanRowView.KanbanPageView(
            id = randomUuid(),
            category = "Documents",
            title = "Interesting books",
            icon = "https://d4804za1f1gw.cloudfront.net/wp-content/uploads/sites/60/2018/08/17113033/books_and_coffee_blog.jpg"
        ),
        KanbanRowView.KanbanFileView(
            icon = "",
            id = randomUuid(),
            category = "Readings",
            title = "Sociology reader"
        ),
        KanbanRowView.KanbanTaskView(
            id = randomUuid(),
            category = "Readings",
            title = "Read about Actor-Network theory",
            checked = true
        ),
        KanbanRowView.KanbanPageView(
            id = randomUuid(),
            category = "Documents",
            title = "Interesting books",
            icon = "https://d4804za1f1gw.cloudfront.net/wp-content/uploads/sites/60/2018/08/17113033/books_and_coffee_blog.jpg"
        ),
        KanbanRowView.KanbanAddNewItemView(id = randomUuid())
    )


    private fun randomUuid(): String {
        return UUID.randomUUID().toString()
    }

}