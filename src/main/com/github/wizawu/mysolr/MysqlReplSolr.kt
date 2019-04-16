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
    // Read configuration
    val config = Configuration()
    val mysqlClient = BinaryLogClient(
        config.mysql.host,
        config.mysql.port,
        config.mysql.username,
        config.mysql.password
    )
    // Set EventDeserializer
    val eventDeserializer = EventDeserializer()
    eventDeserializer.setCompatibilityMode(
        EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY,
        EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG
    )
    mysqlClient.setEventDeserializer(eventDeserializer)
    // Register event listener
    mysqlClient.registerEventListener { event: Event ->
        val header = event.getHeader() as EventHeaderV4
        val data = event.getData() as EventData?
        if (data != null) {
            when (data) {
                is WriteRowsEventData -> Logger.warn(data.rows.size)
                is UpdateRowsEventData -> Logger.warn(data.rows.size)
                is DeleteRowsEventData -> Logger.warn(data.rows.size)
                else -> Logger.info(header.eventType)
            }
        }
    }
    // Start listening events
    mysqlClient.connect()
}
