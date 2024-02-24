package com.anytypeio.anytype.core_models.multiplayer

data class SpaceInviteLink(
    val fileKey: String,
    val contentId: String
) {
    val scheme = "anytype://invite/?cid=$contentId&key=$fileKey"
}