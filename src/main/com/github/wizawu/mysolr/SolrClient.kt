package com.github.wizawu.mysolr

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.json.JSONObject
import java.io.Serializable
import java.util.*

class SolrClient(host: String, port: Int) {
    private var client: HttpSolrClient

    init {
        val url = "http://$host:$port/solr"
        client = HttpSolrClient.Builder(url).build()
    }

    fun write(data: WriteRowsEventData) {
        val table = tableMetadata[data.tableId]!!
        for (row in data.rows) {
            upsertRow(table, data.includedColumns, row)
        }
    }

    fun update(data: UpdateRowsEventData) {
        val table = tableMetadata[data.tableId]!!
        for (row in data.rows) {
            upsertRow(table, data.includedColumns, row.toPair().second)
        }
    }

    fun delete(data: DeleteRowsEventData) {
        val table = tableMetadata[data.tableId]!!
        for (row in data.rows) {
            for (i in 0 until table.columns.size) {
                if (data.includedColumns.get(i) && table.columns[i].name == "id") {
                    client.deleteById(table.solrCore, String(row[i] as ByteArray))
                }
            }
        }
    }

    private fun upsertRow(table: TableMetadata, includedColumns: BitSet, row: Array<Serializable?>) {
        val json = JSONObject()
        for (i in 0 until table.columns.size) {
            if (includedColumns.get(i)) {
                val key = table.columns[i].name
                val value = row[i]
                when (value) {
                    null -> null
                    is ByteArray ->
                        when (table.columns[i].type.toUpperCase()) {
                            "JSON" -> json.put(key, JSONObject(value))
                            else -> json.put(key, String(value))
                        }
                    else -> json.put(key, value)
                }
            }
        }
        println(json.toString())
    }
}