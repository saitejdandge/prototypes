package mysql_connection_jdbc

import mysql_connection_jdbc.Constants.DATABASE_NAME
import mysql_connection_jdbc.Constants.PASSWORD
import mysql_connection_jdbc.Constants.USER_NAME
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


object Constants {
    const val DATABASE_NAME = "prototypes_db"
    const val USER_NAME = "root"
    const val PASSWORD = "saitejmysql18A@!"
}


fun main() {
    // --- Database Configuration ---
    // Replace these placeholders with your actual MySQL database details
    val jdbcUrl = "jdbc:mysql://localhost:3306/$DATABASE_NAME?useSSL=false&serverTimezone=UTC"

    var connection: Connection? = null // Declare connection outside try block for finally access

    try {
        // 1. Register the JDBC Driver (Optional for modern JDBC, but good practice)
        // This line explicitly loads the MySQL JDBC driver class.
        // For JDBC 4.0 and later, drivers are often automatically discovered via ServiceLoader,
        // but explicit loading ensures it's available.
        Class.forName("com.mysql.cj.jdbc.Driver")
        println("MySQL JDBC Driver Registered!")

        // 2. Establish the Connection
        // DriverManager attempts to establish a connection to the given database URL.
        connection = DriverManager.getConnection(jdbcUrl, USER_NAME, PASSWORD)
        println("Connection established successfully!")

        // 3. Perform Database Operations (Example: Executing a SELECT query)
        // Create a Statement object to execute SQL queries.
        val statement = connection.createStatement()

        // Execute a query and get the ResultSet.
        // IMPORTANT: Replace 'your_table_name' with an actual table in your database.
        // Ensure this table exists and has 'id' (INT) and 'name' (VARCHAR) columns.
        val resultSet = statement.executeQuery("SELECT name FROM users")

        println("\n--- Query Results ---")
        // Iterate through the rows returned by the query
        while (resultSet.next()) {
            // Retrieve data by column name or index
//            val id = resultSet.getInt("id")
            val name = resultSet.getString("name")
            println("ID:  Name: $name")
        }
        println("--- End of Results ---\n")

        // It's good practice to close ResultSet and Statement immediately after use
        resultSet.close()
        statement.close()

    } catch (e: ClassNotFoundException) {
        // This exception occurs if the MySQL JDBC driver is not found in the classpath.
        // Double-check your build.gradle dependency.
        System.err.println("Error: MySQL JDBC Driver not found. Please add 'mysql:mysql-connector-java' to your dependencies.")
        e.printStackTrace()
    } catch (e: SQLException) {
        // This exception catches any SQL-related errors (e.g., wrong credentials, database not running, table not found).
        System.err.println("Error: Connection Failed or SQL Exception occurred!")
        e.printStackTrace()
    } finally {
        // 4. Close Resources in the 'finally' block
        // This ensures that the connection is closed even if an exception occurs.
        // Always close resources to prevent leaks.
        try {
            connection?.close() // Use safe call operator '?' to avoid NullPointerException
            println("Connection closed.")
        } catch (e: SQLException) {
            System.err.println("Error closing connection:")
            e.printStackTrace()
        }
    }
}
