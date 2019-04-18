package com.github.wizawu.mysolr

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData
import com.github.shyiko.mysql.binlog.event.deserialization.json.JsonBinary
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.json.JSONArray
import org.json.JSONObject
import org.pmw.tinylog.Logger
import java.io.Serializable
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

class SolrClient(host: String, port: Int) {
    private val url = "http://$host:$port/solr"
    private var client = HttpSolrClient.Builder(url).build()

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
                    null -> json.put(key, JSONObject.NULL)
                    is ByteArray ->
                        when (table.columns[i].type.toUpperCase()) {
                            "JSON" -> {
                                val text = JsonBinary.parseAsString(value)
                                json.put(key, if (text.startsWith("[")) JSONArray(text) else JSONObject(text))
                            }
                            else -> json.put(key, String(value))
                        }
                    else -> json.put(key, value)
                }
            }
        }

        val http = HttpClients.createDefault()
        val action = "upsert ${table.database}.${table.name} ${json.get("id")} to ${table.solrCore}"
        try {
            val request = HttpPost("$url/${table.solrCore}/update/json/docs?commitWithin=1000&overwrite=true&wt=json")
            request.setHeader("Content-Type", "application/json")
            request.entity = StringEntity(json.toString(), UTF_8)
            val response = http.execute(request)
            if (response.statusLine.statusCode != 200) {
                Logger.error("$action: ${String(response.entity.content.readBytes())}")
            }
        } catch (e: Exception) {
            Logger.error("$action: ${e.message}")
        } finally {
            http.close()
        }
    }
}