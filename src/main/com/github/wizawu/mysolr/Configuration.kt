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
}

class Solr {
    lateinit var host: String
    var port: Int = 8983
    lateinit var core: String
}

class Application {
    lateinit var mysql: Mysql
    lateinit var solr: Solr
}

class Configuration {
    var application: Application

    init {
        val inputStream: InputStream = Files.newInputStream(Paths.get("src/main/resources/application.yml"))
        application = Yaml().loadAs(inputStream, Application::class.java)
        inputStream.close()
    }
}