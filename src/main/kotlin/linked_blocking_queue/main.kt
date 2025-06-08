package linked_blocking_queue

import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

fun main() {
    val queue = LinkedBlockingQueue<Int>(5) // A bounded queue with a capacity of 5

    val producer = thread(name = "Producer") {
        for (i in 1..10) {
            println("Producing $i")
            queue.put(i) // Blocks if the queue is full
            println("Produced $i, queue size: ${queue.size}")
            Thread.sleep(500)
        }
    }

    val consumer = thread(name = "Consumer") {
        for (i in 1..10) {
            val element = queue.take() // Blocks if the queue is empty
            println("Consumed $element")
            Thread.sleep(1000)
        }
    }

    producer.join()
    consumer.join()
}
