package com.intuit.identity.manage.connection_pool

import connection_pool.ConnectionFactory
import connection_pool.MyConnection
import java.sql.SQLException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ConnectionPool(
    private val minConnections: Int,
    private val maxConnections: Int,
    private val connectionTimeoutMs: Long // Timeout for getting a connection
) {
    private val pool: BlockingQueue<MyConnection>
    private val connectionFactory: ConnectionFactory

    // Keep track of all connections ever created by this pool, for proper shutdown
    private val allCreatedConnections = mutableListOf<MyConnection>()

    init {
        require(minConnections > 0 && maxConnections > 0 && minConnections <= maxConnections) {
            "Invalid pool size configuration: minConnections=$minConnections, maxConnections=$maxConnections"
        }

        // Using LinkedBlockingQueue which is unbounded by default, but we'll manage size
        // Alternatively, use ArrayBlockingQueue if you want a fixed-size internal queue
        this.pool = LinkedBlockingQueue(maxConnections) // Bounded queue!
        this.connectionFactory = ConnectionFactory(pool)

        initializePool()
    }

    private fun initializePool() {
        println("Initializing connection pool with $minConnections connections...")
        repeat(minConnections) {
            val connection = connectionFactory.createConnection()
            try {
                pool.put(connection) // Add initial connections to the pool
                allCreatedConnections.add(connection)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                System.err.println("Failed to initialize connection ${connection.getId()} due to interruption.")
                connection.reallyClose() // Close the connection if it can't be put into the pool
            }
        }
        println("Connection pool initialized. Current size: ${pool.size}")
    }

    @Throws(InterruptedException::class, SQLException::class)
    fun getConnection(): MyConnection {
        var connection: MyConnection? = null

        // Try to take a connection from the pool with a timeout
        connection = pool.poll(connectionTimeoutMs, TimeUnit.MILLISECONDS)

        if (connection != null) {
            println("Got existing connection ${connection.getId()} from pool. Pool size: ${pool.size}")
            return connection
        }

        // If no connection available and pool is not at max, try to create a new one
        // Use synchronized(this) for thread-safe creation of new connections
        synchronized(this) {
            if (allCreatedConnections.size < maxConnections) { // Check total created connections
                println("Pool is empty, and max connections not reached. Creating new connection...")
                val newConnection = connectionFactory.createConnection()
                allCreatedConnections.add(newConnection) // Track for shutdown
                println("Created new connection ${newConnection.getId()}. Total created: ${allCreatedConnections.size}")
                return newConnection // Return the newly created connection
            }
        }

        // If pool is at max capacity and no connection was available, block until one is returned
        println("Pool is at max capacity ($maxConnections). Waiting for a connection...")
        connection = pool.take() // This will block indefinitely until a connection is available
        println("Got existing connection ${connection.getId()} from pool after waiting. Pool size: ${pool.size}")
        return connection
    }

    /**
     * Shuts down the connection pool, closing all underlying connections.
     * It's crucial to call this method when your application is shutting down
     * to release database resources.
     */
    fun shutdown() {
        println("\nShutting down connection pool...")
        // Clear the queue first
        pool.clear() // Remove all connections from the queue

        // Now, truly close all connections that were ever created by this pool
        allCreatedConnections.forEach { connection ->
            if (!connection.isReallyClosed()) {
                connection.reallyClose()
            }
        }
        allCreatedConnections.clear()
        println("Connection pool shut down. All connections closed.")
    }

    fun getCurrentPoolSize(): Int = pool.size
    fun getMaxPoolSize(): Int = maxConnections
}