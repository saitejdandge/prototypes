package com.intuit.identity.manage.load_balancers

import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

fun main() {
    val loadBalancer = LoadBalancer(
        listOf(
            Server("server_1"),
            Server("server_2"),
            Server("server_3"),
            Server("server_4"),
        )
    )
    for (i in 1..1000) {
        thread {
            val client = "client_${i}"
            val server = loadBalancer.getServer()
            server.someApi(client)
            Thread.sleep(1000) // simulating work
        }
    }
}

data class Server(val url: String) {
    fun someApi(client: String) {
        println("$client calling ${this.url}")
    }
}

class LoadBalancer(private val servers: List<Server>) {

    private val currentIndex: AtomicInteger = AtomicInteger()
    fun getServer(): Server {
        return servers[currentIndex.getAndIncrement() % servers.size]
    }
}


