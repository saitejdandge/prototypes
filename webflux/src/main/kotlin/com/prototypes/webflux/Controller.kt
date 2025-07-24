package com.prototypes.webflux

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {
    private val log = LoggerFactory.getLogger(Controller::class.java)

    @RequestMapping("/hello")
    fun hello(): String {
        val fib = fibonacci(20)
        log.info("received request at ${Thread.currentThread().name} $fib")
        return "hello world$fib"
    }

    fun fibonacci(n: Int): Int {
        log.info("received request at ${Thread.currentThread().name}")
        return if (n <= 1)
            n
        else
            fibonacci(n - 1) + fibonacci(n - 2)
    }

}