package main.com.github.wizawu.mysolr

import java.util.concurrent.ConcurrentHashMap

data class ColumnMetadata(
    var name: String,
    var type: String,
    var size: Int
)

data class TableMetadata(
    var database: String,
    var name: String,
    var columns: List<ColumnMetadata>
)

val tableMetadata = ConcurrentHashMap<Long, TableMetadata>()
