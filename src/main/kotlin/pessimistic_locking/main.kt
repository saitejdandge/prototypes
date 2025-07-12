import hikaricp.Database
import java.sql.Connection
import kotlin.concurrent.thread
import kotlin.time.measureTime

// Database connection details
private const val DB_URL = "jdbc:mysql://localhost:3306/airlines?useSSL=false&serverTimezone=UTC"
private const val USER = "root" // Replace with your MySQL username
private const val PASS = "saitejmysql18A@!" // Replace with your MySQL password


// for update works but it is costly
// for update skip locked works efficiently
// for update nowait will only update few records, throws errors for others
const val selectEmptySeatSql =
    "select id, user_id from seats where user_id is null order by id limit 1 for update skip locked;"
const val updateSeatSql = "update seats set user_id = ? where id = ?"


fun main() {
    println("--- Starting Pessimistic Seat Locking Simulation ---")
    println("clearing the seats for test purpose")
    Database.getConnection().use {
        it.prepareStatement("update seats set user_id = null").executeUpdate()
    }
    val timeTook = measureTime {
        // List of users trying to claim seats
        val userIds = (1..12000).map { "user_${it}" }
        // Create and start a thread for each user
        val threads = userIds.map { userId ->
            thread(name = "Thread-$userId") {
                claimSeat(userId)
            }
        }
        // Wait for all threads to complete
        threads.forEach { it.join() }
    }
    println("time took ${timeTook.inWholeSeconds} seconds")
}


fun claimSeat(userId: String) {
    val connection = Database.getConnection()
    connection.use {
        connection.autoCommit = false
        connection.prepareStatement(selectEmptySeatSql).use { stmt ->
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    // if there is seat, keep track of seatId
                    val seatId = rs.getString("id")
                    checkinSeat(userId, seatId, connection)
                } else {
                    println("no seat is available")
                    connection.rollback()
                    return
                }
            }
        }
        connection.autoCommit = true
    }
}

fun checkinSeat(userId: String, seatId: String, connection: Connection) {
    connection.prepareStatement(updateSeatSql).use { stmt ->
        stmt.setString(1, userId)
        stmt.setString(2, seatId)
        stmt.executeUpdate()
        connection.commit()
    }
}
