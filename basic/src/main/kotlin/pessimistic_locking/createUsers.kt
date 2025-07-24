package com.intuit.identity.manage.pessimistic_locking

import java.sql.Connection
import java.sql.DriverManager

const val database = "airlines"
const val username = "root"
const val password = "saitejmysql18A@!"

fun main() {
    val connection = getConnection()
    selectUsers(connection)
    createUsers(connection)
    selectUsers(connection)
}

fun createUsers(connection: Connection?) {
    connection?.let {
        val insertStatement = "INSERT INTO users(name) values (?)"
        for (i in 1..120) {
            val statement = connection.prepareStatement(insertStatement);
            statement.setString(1, "user_$i")
            statement.executeUpdate()
            statement.close()
        }
    }
}

fun getConnection(): Connection? {
    val url = "jdbc:mysql://localhost:3306/$database" // Replace with your database URL
    return DriverManager.getConnection(url, username, password)

}

fun selectUsers(connection: Connection?) {
    connection?.let {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT user_id, name FROM users;")
        println("\nFetched users:")
        while (resultSet.next()) {
            val userId = resultSet.getInt("user_id")
            val name = resultSet.getString("name")
            println("User ID: $userId, Name: $name")
        }
        // Close resources
        resultSet.close()
        statement.close()
    }
}
