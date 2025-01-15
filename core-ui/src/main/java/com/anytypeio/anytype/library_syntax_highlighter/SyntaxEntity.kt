package com.anytypeio.anytype.library_syntax_highlighter

import kotlinx.serialization.Serializable

@Serializable
data class SyntaxEntity(val pattern: String, val color: String, val key: String)

@Serializable
data class SyntaxDescriptor(
    val keywords: List<SyntaxEntity>,
    val operators: List<SyntaxEntity>,
    val other: List<SyntaxEntity>
)