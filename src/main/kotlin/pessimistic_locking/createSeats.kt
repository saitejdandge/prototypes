package com.intuit.identity.manage.pessimistic_locking

import java.sql.Connection

fun main() {
    val connection = getConnection()
//    createSeats("AIR_INDIA_HYD_TO_BLR", connection)
    showSeats(connection)
//    println(generateSeatIds())
}

fun createSeats(tripId: String, connection: Connection?) {
    connection?.let {
        val sqlStatement = "insert into seats (id,trip_id) values (?,?);"
        val seatIds = generateSeatIds()
        seatIds.forEach { seatId ->
            val statement = connection.prepareStatement(sqlStatement)
            statement.setString(1, seatId)
            statement.setString(2, tripId)
            statement.executeUpdate()
            statement.close()
        }
    }
}

fun generateSeatIds(): List<String> {
    val output = mutableListOf<String>()
    for (row in 1..20) {
        output.addAll(listOf("${row}A", "${row}B", "${row}C", "${row}D", "${row}E", "${row}F"))
    }
    return output
}

fun showSeats(connection: Connection?) {
    connection?.let {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("""
            SELECT *
            FROM seats
            ORDER BY CAST(SUBSTRING(id, 1, LENGTH(id) - 1) AS UNSIGNED),
                     SUBSTRING(id, -1);
        """.trimIndent())
        while (resultSet.next()) {
            val userId = resultSet.getInt("user_id")
            val name = resultSet.getString("id")
            val trip = resultSet.getString("trip_id")
            println("User ID: $userId, Name: $name, Trip: $trip")
        }
        // Close resources
        resultSet.close()
        statement.close()
    }
}
