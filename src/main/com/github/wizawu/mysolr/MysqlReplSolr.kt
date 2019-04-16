package main.com.github.wizawu.mysolr

import com.github.wizawu.mysolr.Configuration

fun main() {
    val config = Configuration()
    println(config.application.solr.core)
}
