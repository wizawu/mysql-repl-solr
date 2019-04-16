package main.com.github.wizawu.mysolr

import com.github.shyiko.mysql.binlog.event.TableMapEventData
import java.util.concurrent.ConcurrentHashMap

val tableMetadata = ConcurrentHashMap<Long, TableMapEventData>()