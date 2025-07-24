package connection_pool

import java.sql.SQLException
import java.util.concurrent.BlockingQueue

/**
 * Represents a simplified database connection for pooling.
 * In a real application, this would wrap a `java.sql.Connection`.
 */
class MyConnection(
    private val id: String, // Just an ID to distinguish connections
    private val connectionPoolQueue: BlockingQueue<MyConnection> // Reference to the pool's queue
) : AutoCloseable {

    @Volatile // Ensures visibility of changes across threads
    private var reallyClosed: Boolean = false

    init {
        println("MyConnection $id created.")
    }

    // Simulate some database operation
    fun executeQuery(query: String) {
        if (reallyClosed) {
            throw IllegalStateException("Connection $id is closed.")
        }
        println("Connection $id executing query: $query")
        // In a real scenario, this would interact with the JDBC Connection
    }

    override fun close() {
        if (reallyClosed) {
            println("Connection $id already truly closed. Not returning to pool.")
            return
        }
        try {
            // Instead of truly closing, return it to the pool
            connectionPoolQueue.put(this)
            println("Connection $id returned to pool. Pool size: ${connectionPoolQueue.size}")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            System.err.println("Failed to return connection $id to pool due to interruption: ${e.message}")
            // If interruption occurs, the connection might be "lost" from the pool's perspective.
            // In a real scenario, you might log this heavily and potentially truly close the connection.
            reallyClose() // Attempt to truly close if can't return to pool
        }
    }

    // This method would be called by the pool shutdown or if an error occurs
    fun reallyClose() {
        if (!reallyClosed) {
            println("MyConnection $id truly closed.")
            reallyClosed = true
            // In a real scenario, this would call connection.close() on the underlying JDBC connection
        }
    }

    fun getId(): String = id

    fun isReallyClosed(): Boolean = reallyClosed
}