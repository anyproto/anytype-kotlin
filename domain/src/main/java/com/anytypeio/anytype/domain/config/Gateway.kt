package com.anytypeio.anytype.domain.config

interface Gateway {
    fun provide(): String
}