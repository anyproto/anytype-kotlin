package com.agileburo.anytype.domain.database.model

sealed class Database

data class Table(val rows: List<Row>): Database()
