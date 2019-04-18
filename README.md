# mysql-repl-solr

Supported MySQL column types:

* Numeric
* Char/Text
* JSON

### Preparation

* Enable MySQL binlog
  ```
  [mysqld]
  log-bin = mysql-bin
  server_id = 1
  ```
* Create a MySQL user with `REPLICATION` privilege
* Create MySQL tables
* Create Solr `managed-schema`

### Get Started

Clone this repository.

```
git clone git@github.com:wizawu/mysql-repl-solr.git
```

Edit `src/main/resources/application.yml` and start the program with

```
make server
```
