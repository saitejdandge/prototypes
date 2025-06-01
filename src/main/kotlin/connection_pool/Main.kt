package com.intuit.identity.manage.connection_pool

import connection_pool.MyConnection
import java.sql.SQLException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val pool = ConnectionPool(2, 5, 2000L) // Min 2, Max 5, 2-second timeout

    val executor = Executors.newFixedThreadPool(7) // More threads than max connections to test blocking

    // Task to simulate using a connection
    val connectionUser = Runnable {
        var conn: MyConnection? = null
        try {
            conn = pool.getConnection()
            println("${Thread.currentThread().name} acquired connection: ${conn.getId()}")
            conn.executeQuery("SELECT * FROM users;")
            Thread.sleep((Math.random() * 1000 + 500).toLong()) // Simulate work
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            System.err.println("${Thread.currentThread().name} was interrupted while getting/using connection: ${e.message}")
        } catch (e: SQLException) {
            System.err.println("${Thread.currentThread().name} SQL error: ${e.message}")
        } finally {
            conn?.close() // This returns the connection to the pool
            conn?.let { println("${Thread.currentThread().name} released connection: ${it.getId()}") }
        }
    }

    println("\n--- Starting connection requests ---")
    repeat(7) {
        executor.submit(connectionUser)
    }

    executor.shutdown()
    try {
        // Wait for all tasks to complete, or timeout after 10 seconds
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            println("\nSome tasks did not complete within the timeout.")
            executor.shutdownNow() // Force shutdown if tasks are still running
        }
    } catch (e: InterruptedException) {
        executor.shutdownNow()
        Thread.currentThread().interrupt()
    }

    // Clean up the pool
    pool.shutdown()
}