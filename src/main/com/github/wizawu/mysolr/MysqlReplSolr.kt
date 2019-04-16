package main.com.github.wizawu.mysolr

import com.github.shyiko.mysql.binlog.BinaryLogClient
import com.github.shyiko.mysql.binlog.event.*
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer
import com.github.wizawu.mysolr.Configuration
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Logger

fun main() {
    // Set logging format
    Configurator.currentConfig().formatPattern("{date:yyyy-MM-dd HH:mm:ss} [{level}] [{thread}] {message}").activate()
    // Initialize mysql/solr clients
    val config = Configuration()
    val mysqlClient = BinaryLogClient(
        config.mysql.host,
        config.mysql.port,
        config.mysql.username,
        config.mysql.password
    )
    val solrClient = SolrClient(config.solr.host, config.solr.port, config.solr.core)
    // Set EventDeserializer
    val eventDeserializer = EventDeserializer()
    eventDeserializer.setCompatibilityMode(
        EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY,
        EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG
    )
    mysqlClient.setEventDeserializer(eventDeserializer)
    // Register event listener
    mysqlClient.registerEventListener { event: Event ->
        val databases = config.mysql.databases
        val header = event.getHeader() as EventHeaderV4
        val data = event.getData() as EventData?
        when (data) {
            null -> Logger.debug(header.eventType)
            is TableMapEventData -> tableMetadata[data.tableId] = data
            is WriteRowsEventData -> {
                if (tableMetadata.contains(data.tableId)) {
                    val database = tableMetadata[data.tableId]!!.database
                    val table = tableMetadata[data.tableId]!!.table
                    if (databases.contains(database) && databases.getValue(database).contains(table)) {
                        solrClient.write(data)
                    }
                }
            }
            is UpdateRowsEventData -> {
                if (tableMetadata.contains(data.tableId)) {
                    val database = tableMetadata[data.tableId]!!.database
                    val table = tableMetadata[data.tableId]!!.table
                    if (databases.contains(database) && databases.getValue(database).contains(table)) {
                        solrClient.update(data)
                    }
                }
            }
            is DeleteRowsEventData -> {
                if (tableMetadata.contains(data.tableId)) {
                    val database = tableMetadata[data.tableId]!!.database
                    val table = tableMetadata[data.tableId]!!.table
                    if (databases.contains(database) && databases.getValue(database).contains(table)) {
                        solrClient.delete(data)
                    }
                }
            }
            else -> Logger.debug(header.eventType)
        }
    }
    // Start listening events
    mysqlClient.connect()
}
