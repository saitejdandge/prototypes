import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

// 1. Define your Connection Details (replace with your actual database config)
data class DbConfig(
    val url: String,
    val user: String,
    val password: String,
    val driverClass: String // e.g., "org.postgresql.Driver"
)

// 2. Connection Wrapper (optional but good for managing state/cleanup)
class PooledConnection(
    val connection: Connection,
    private val pool: ConnectionPool // Reference back to the pool for release
) {
    fun release() {
        pool.releaseConnection(this)
    }

    // You might add methods here to check if the connection is still valid
    fun isValid(timeout: Int): Boolean {
        return try {
            connection.isValid(timeout)
        } catch (e: Exception) {
            false
        }
    }

    fun closeUnderlyingConnection() {
        try {
            connection.close()
        } catch (e: Exception) {
            println("Error closing underlying connection: ${e.message}")
        }
    }
}

// 3. Connection Pool Implementation
class ConnectionPool(
    private val config: DbConfig,
    private val minConnections: Int,
    private val maxConnections: Int,
    private val connectionTimeoutMillis: Long = 5000, // How long to wait for a connection
    private val validationTimeoutSeconds: Int = 2 // How long to wait for isValid check
) {
    private val availableConnections: ArrayBlockingQueue<PooledConnection>
    private val currentConnectionsCount = AtomicInteger(0)
    private val lock = Any() // For synchronized blocks when creating connections

    init {
        require(minConnections >= 0) { "Min connections must be non-negative" }
        require(maxConnections >= minConnections) { "Max connections must be greater than or equal to min connections" }
        require(connectionTimeoutMillis > 0) { "Connection timeout must be positive" }

        availableConnections = ArrayBlockingQueue(maxConnections)
        initializeConnections()
    }

    private fun initializeConnections() {
        Class.forName(config.driverClass) // Ensure driver is loaded

        repeat(minConnections) {
            addConnectionToPool()
        }
    }

    private fun createNewConnection(): Connection? {
        return try {
            println("Creating new database connection...")
            DriverManager.getConnection(config.url, config.user, config.password)
        } catch (e: Exception) {
            println("Error creating new connection: ${e.message}")
            null
        }
    }

    private fun addConnectionToPool() {
        synchronized(lock) {
            if (currentConnectionsCount.get() < maxConnections) {
                val newConnection = createNewConnection()
                if (newConnection != null) {
                    val pooledConnection = PooledConnection(newConnection, this)
                    availableConnections.offer(pooledConnection) // Use offer to not block creation
                    currentConnectionsCount.incrementAndGet()
                    println("Connection added to pool. Current size: ${currentConnectionsCount.get()}")
                }
            } else {
                println("Max connections reached. Cannot add more connections.")
            }
        }
    }

    fun getConnection(): PooledConnection {
        var pooledConnection: PooledConnection? = null
        try {
            // Try to take an existing connection
            pooledConnection = availableConnections.poll(connectionTimeoutMillis, TimeUnit.MILLISECONDS)

            if (pooledConnection == null) {
                // If no connection is immediately available, try to create one if under max
                synchronized(lock) {
                    if (currentConnectionsCount.get() < maxConnections) {
                        addConnectionToPool() // This will try to add and then offer
                        // Re-attempt to take from the queue after potential creation
                        pooledConnection = availableConnections.poll(connectionTimeoutMillis, TimeUnit.MILLISECONDS)
                    }
                }
            }

            // Validate the retrieved/newly created connection
            if (pooledConnection != null && !pooledConnection.isValid(validationTimeoutSeconds)) {
                println("Invalid connection detected. Closing and trying to get another.")
                pooledConnection.closeUnderlyingConnection()
                currentConnectionsCount.decrementAndGet()
                // Recursively try to get another connection
                return getConnection()
            }

            if (pooledConnection == null) {
                throw ConnectionPoolException("Failed to get a connection within the timeout period.")
            }
            return pooledConnection
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ConnectionPoolException("Interrupted while waiting for a connection.", e)
        }
    }

    fun releaseConnection(pooledConnection: PooledConnection) {
        if (pooledConnection.isValid(validationTimeoutSeconds)) {
            availableConnections.offer(pooledConnection) // Add back to the pool
            println("Connection released back to pool. Available: ${availableConnections.size}")
        } else {
            println("Released connection is invalid. Closing it.")
            pooledConnection.closeUnderlyingConnection()
            currentConnectionsCount.decrementAndGet()
            // Optionally, create a new connection to replace the invalid one if below minConnections
            if (currentConnectionsCount.get() < minConnections) {
                addConnectionToPool()
            }
        }
    }

    fun shutdown() {
        println("Shutting down connection pool...")
        while (availableConnections.isNotEmpty()) {
            val pooledConnection = availableConnections.poll()
            pooledConnection?.closeUnderlyingConnection()
            currentConnectionsCount.decrementAndGet()
        }
        println("Connection pool shut down. Connections closed: ${currentConnectionsCount.get()}")
    }
}

class ConnectionPoolException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

// 4. Example Usage
fun main() {
    val dbConfig = DbConfig(
        url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", // H2 in-memory for testing
        user = "sa",
        password = "",
        driverClass = "org.h2.Driver"
    )

    val pool = ConnectionPool(
        config = dbConfig,
        minConnections = 2,
        maxConnections = 5,
        connectionTimeoutMillis = 3000
    )

    println("----- Getting connections -----")
    val conn1 = pool.getConnection()
    println("Got conn1: $conn1")
    val conn2 = pool.getConnection()
    println("Got conn2: $conn2")
    val conn3 = pool.getConnection()
    println("Got conn3: $conn3")

    println("\n----- Releasing connections -----")
    conn1.release()
    conn2.release()
    conn3.release()

    println("\n----- Getting more than max (will block) -----")
    try {
        val c4 = pool.getConnection()
        val c5 = pool.getConnection()
        val c6 = pool.getConnection() // This will block until one is released or timeout
        println("Got c4, c5, c6")
        c4.release()
        c5.release()
        c6.release()
    } catch (e: ConnectionPoolException) {
        println("Error getting connection: ${e.message}")
    }

    println("\n----- Simulating invalid connection -----")
    val badConn = pool.getConnection()
    // Simulate invalidation (e.g., database restart, network issue)
    badConn.connection.close() // Close the underlying connection directly
    println("Simulated bad connection: $badConn")
    badConn.release() // This will now detect it as invalid and close/replace it

    println("\n----- Shutdown pool -----")
    pool.shutdown()
}