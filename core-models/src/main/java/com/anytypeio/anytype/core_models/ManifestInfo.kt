package com.anytypeio.anytype.core_models

data class ManifestInfo(
    val schema: String,
    val id: String,
    val name: String,
    val author: String,
    val license: String,
    val title: String,
    val description: String,
    val screenshots: List<String>,
    val downloadLink: String,
    val fileSize: Int,
    val categories: List<String>,
    val language: String
)