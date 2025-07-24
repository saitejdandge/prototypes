package example

import io.gatling.javaapi.core.CoreDsl.constantUsersPerSec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.time.Duration


// 1. HTTP Protocol Configuration
// This defines common settings for all HTTP requests in the simulation.
// It includes the base URL and common headers that a typical browser would send.
val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9") // Standard Accept header
    .acceptEncodingHeader("gzip, deflate") // Standard Accept-Encoding header
    .acceptLanguageHeader("en-US,en;q=0.9") // Standard Accept-Language header
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36") // Common User-Agent header


/**
 * A basic Gatling simulation written in Kotlin.
 * This simulation demonstrates:
 * 1. HTTP protocol configuration (base URL, common headers).
 * 2. Scenario definition with a single GET request and a pause.
 * 3. Injection profile to simulate user load.
 */
class BasicSimulation : Simulation() {


    val scn = scenario("Personal app simple testing")
        .exec(
            http("home")
                .get("/hello")
                .check(status().`is`(200))
        )
    init {
        setUp(
            scn.injectOpen(
                constantUsersPerSec(200.0)
                    .during(Duration.ofSeconds(30))
            )
        ).protocols(httpProtocol)
    }
}
