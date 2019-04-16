package main.com.github.wizawu.mysolr

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData
import org.apache.solr.client.solrj.impl.HttpSolrClient

class SolrClient(host: String, port: Int, core: String) {
    private var client: HttpSolrClient
    private var core: String

    init {
        val url = "http://$host:$port/solr"
        client = HttpSolrClient.Builder(url).build()
        this.core = core
    }

    fun write(data: WriteRowsEventData) {
    }

    fun update(data: UpdateRowsEventData) {
    }

    fun delete(data: DeleteRowsEventData) {
    }
}