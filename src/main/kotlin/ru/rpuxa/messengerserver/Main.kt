package ru.rpuxa.messengerserver

import java.io.File

fun main() {

   // Thread(HttpServer).start()

    while (true) {
        try {
            DataBase(readLine()!!).connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}