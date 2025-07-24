package connection_pool

import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * A factory to create new MyConnection instances.
 */
class ConnectionFactory(private val connectionPoolQueue: BlockingQueue<MyConnection>) {
    private val connectionCounter = AtomicInteger(0)

    fun createConnection(): MyConnection {
        return MyConnection("Conn-${connectionCounter.incrementAndGet()}", connectionPoolQueue)
    }
}