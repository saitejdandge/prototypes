package com.prototypes.mvcspring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MvcSpringApplication

fun main(args: Array<String>) {
    runApplication<MvcSpringApplication>(*args)
}
