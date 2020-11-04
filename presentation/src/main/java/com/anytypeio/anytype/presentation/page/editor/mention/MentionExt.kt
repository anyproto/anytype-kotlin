package com.anytypeio.anytype.presentation.page.editor.mention

fun String.getMentionName(untitled: String): String = if (this.isBlank()) untitled else this