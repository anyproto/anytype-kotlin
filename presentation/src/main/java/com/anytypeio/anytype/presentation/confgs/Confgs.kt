package com.anytypeio.anytype.presentation.confgs

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.BuildConfig

object ChatConfig {

    const val MAX_ATTACHMENT_COUNT = 10
    const val MAX_USER_REACTION_COUNT = 3
    const val MAX_REACTION_COUNT = 12

    /**
     * Spaces for beta-testing space-level chats
     */
    val spacesWithSpaceLevelChat = listOf(
        "bafyreiezhzb4ggnhjwejmh67pd5grilk6jn3jt7y2rnfpbkjwekilreola.1t123w9f2lgn5",
        "bafyreifikxj75r4duzhqxqelmi66rwlzqml5jnad35dnukxwlawtfrql5a.21584urzltddb",
        "bafyreia4jsiobrq7ptpuxsv6nmpj4vis7o5p73yibjb5w4crhxl2oqocoq.9tkr2p3mb0pj",
        "bafyreibj7du7epctmeiwix7ccjiyogfew36eztgfob4mgmi6ulcwzqatcy.2t4tpsucpkt93",
        "bafyreihzeo4dd3zvw7pkoztwit6edjku6w3jfrfnserxm6slmc6qiu2sim.3rqqcnrgm797n",
        "bafyreig67rszl52id767endswgzordgg4pj6hpx7dw3cjrvoimehqonb4q.2t4tpsucpkt93"
    )

    fun isChatAllowed(space: Id): Boolean {
        return BuildConfig.DEBUG || spacesWithSpaceLevelChat.contains(space)
    }
}