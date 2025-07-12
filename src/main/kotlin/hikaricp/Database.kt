package hikaricp

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

fun main() {
    val connection = Database.getConnection()
    connection.use {
        connection.prepareStatement("select * from seats;").use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    val username = rs.getString("id")
                    println(username)
                }
            }
        }
    }
}

object Database {
    private val dataSource: HikariDataSource

    init {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://localhost:3306/airlines?useSSL=false&serverTimezone=UTC"
        config.username = "root" // Replace with your MySQL username
        config.password = "saitejmysql18A@!" // Replace with your MySQL password
        config.addDataSourceProperty("cachePrepStmts", "true") // Recommended for performance
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        // HikariCP specific properties
        config.poolName = "MyHikariCP"
        config.maximumPoolSize = 10 // Max connections in the pool
        config.minimumIdle = 5     // Min idle connections
        config.connectionTimeout = 50000 // 50 seconds
        config.idleTimeout = 600000 // 10 minutes
        config.maxLifetime = 1800000 // 30 minutes

        dataSource = HikariDataSource(config)
        println("HikariCP initialized with pool name: ${config.poolName}")
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

    fun closeDataSource() {
        dataSource.close()
        println("HikariCP data source closed.")
    }
}