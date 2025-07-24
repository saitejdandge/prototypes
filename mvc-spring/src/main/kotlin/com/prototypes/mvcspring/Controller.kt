package com.prototypes.mvcspring

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/hello")
    fun hello(): String {
        logger.info("request received ${Thread.currentThread().name}")
        return "Hello World! ${fib(40)}"
    }

    fun fib(n: Int): Int {
        return if (n <= 1) n
        else fib(n - 1) + fib(n - 2)
    }
}