package com.agileburo.anytype.middleware.model

import anytype.Models

class CreateAccountResponse(
    val id: String,
    val name: String,
    val avatar: Models.Image
)