package channel_co_routines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

fun main() = runBlocking {

    val channel = Channel<Int>(Channel.BUFFERED) // A buffered channel

    val producer = launch(Dispatchers.Default) {
        for (i in 1..10) {
            println("Sending $i")
            channel.send(i) // Suspends if the channel buffer is full
            delay(500)
        }
        channel.close() // Close the channel to signal the end of the stream
    }

    val consumer = launch(Dispatchers.Default) {
        for (element in channel) { // Iterates until the channel is closed
            println("Received $element")
            delay(1000)
        }
    }
    producer.join()
    consumer.join()

}
