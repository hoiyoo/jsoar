# Introduction #

With JSoar 0.10.0, a port of CSoar 9.3.0's semantic memory is included with JSoar. For the most part, the implementation is identical to CSoar's so the information in the [SMem Manual](http://soar.googlecode.com/svn/tags/Soar-Suite-9.3.0/Core/Documentation/Soar-SMem%20Manual.docx) is valid. This document covers any differences or changes in the JSoar implementation.

Like CSoar, JSoar's default database configuration uses Sqlite. In particular, it uses Xerial's [SQLiteJDBC](http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC) JDBC driver.

# Database Configuration #

Unlike CSoar which uses sqlite's C API, JSoar's SMem implementation is built on the JDBC API. This allows other databases to be used. JSoar has been tested with SQLite (default) and MySQL 5.x.  See below for DBMS-specific configuration.

JDBC database connections are configured with a driver class JDBC URL which are split across three SMem parameters

| Name | Default Value | Description |
|:-----|:--------------|:------------|
| driver | `org.sqlite.JDBC` | The JDBC driver class. The driver must be on the class path. Sqlite is provided in the JSoar distribution. |
| protocol | `jdbc:sqlite` | The protocol of the URL |
| path | `:memory:` | The rest of the JDBC URL. The content is dependent on the driver and protocol. |

Some JDBC driver and URL examples are given [here](http://www.petefreitag.com/articles/jdbc_urls/).

When SMem is initialized, the class specified by `driver` will be loaded in the usual way using `Class.forName()` and the URL will be constructed by joining `protocol` and `path` with a colon, e.g. with the defaults given above, the resulting URL will be `jdbc:sqlite::memory:`.


## sqlite ##

In terms of the CSoar implementation, to get an in-memory sqlite database (the default):

```
smem --set driver org.sqlite.JDBC
smem --set protocol jdbc:sqlite
smem --set path :memory:
```

To store to a sqlite file named `foo.db`:

```
smem --set driver org.sqlite.JDBC
smem --set protocol jdbc:sqlite
smem --set path foo.db
```

_Note that in both examples, driver and protocol can be omitted since they are the defaults._

## MySQL ##
To use MySQL, you first need to install and configure MySQL. You'll also need to get the appropriate [MySQL JDBC driver jar](http://dev.mysql.com/downloads/connector/j/) and add it to your classpath. If you're running JSoar standalone, just drop the jar in the lib directory.

Now, here's how you'd point it at an existing MySQL database on the local host named `smem`:

```
smem --set driver com.mysql.jdbc.Driver
smem --set protocol jdbc:mysql
smem --set path //localhost:3306/smem?user=userName&password=pass
```

## PostgreSQL ##
To use PostgreSQL, you first need to install and configure PostgreSQL. You'll also need to get the appropriate [PostgreSQL JDBC driver jar](http://jdbc.postgresql.org/download.html) (get the JDBC 4 driver) and add it to your classpath. If you're running JSoar standalone, just drop the jar in the lib directory. PostgreSQL 8.4 or newer is required.

Now, here's how you'd point it at an existing PostgreSQL database on the local host named `smem`:

```
smem --set driver org.postgresql.Driver
smem --set protocol jdbc:postgresql
smem --set path //localhost:5432/smem?user=userName&password=pass
```

# Additional Commands #
JSoar provides a few extra SMem-related commands.

There's the `--sql` command which allows arbitrary SQL to be executed for debugging:

```
smem --sql select * from smem2_lti
```

There's the `--commit` command which forces a write to the database when in lazy commit mode:

```
smem --commit
```