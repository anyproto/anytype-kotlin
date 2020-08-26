package com.agileburo.anytype.domain.config

interface Gateway {
    fun obtain(): String
}