package com.anytypeio.anytype.core_models

/**
 * Anytype app configuration properties.
 * @property home id of the home dashboard
 * @property profile id of the current profile
 * @property gateway url of the gateway for fetching files.
 * @property spaceView id of space view - UI-representation of space object
 * @property workspaceObjectId used for space-level chat
 */
data class Config(
    val home: Id,
    val profile: Id,
    val gateway: Url,
    val space: Id,
    val techSpace: Id,
    val spaceView: Id,
    val widgets: Id,
    val analytics: Id,
    val device: Id,
    val network: Id,
    val workspaceObjectId: Id
)