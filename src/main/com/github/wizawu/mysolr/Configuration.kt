package com.github.wizawu.mysolr

import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

class Mysql {
    lateinit var host: String
    var port: Int = 3306
    lateinit var username: String
    lateinit var password: String
    lateinit var databases: Map<String, Map<String, String>>
}

class Solr {
    lateinit var host: String
    var port: Int = 8983
}

open class Application {
    lateinit var mysql: Mysql
    lateinit var solr: Solr
}

class Configuration : Application() {
    init {
        val configName = System.getenv().getOrDefault("CONFIG_NAME", "application")
        val inputStream: InputStream = Files.newInputStream(Paths.get("src/main/resources/$configName.yml"))
        val application = Yaml().loadAs(inputStream, Application::class.java)
        inputStream.close()
        mysql = application.mysql
        solr = application.solr
    }
}