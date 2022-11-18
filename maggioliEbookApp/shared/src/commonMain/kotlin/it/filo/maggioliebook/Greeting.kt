package it.filo.maggioliebook

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}