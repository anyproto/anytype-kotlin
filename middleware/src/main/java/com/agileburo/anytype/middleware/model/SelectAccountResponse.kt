package com.agileburo.anytype.middleware.model

import anytype.Models

class SelectAccountResponse(
    val id: String,
    val name: String,
    val avatar: Models.Image? = null
)