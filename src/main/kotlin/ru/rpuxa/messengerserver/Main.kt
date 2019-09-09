package ru.rpuxa.messengerserver

fun main() {
    DataBase.connect()
    Thread(HttpServer).start()

    println("Server started!")
}