package com.anytypeio.anytype.domain.config

interface Gateway {
    fun obtain(): String
}