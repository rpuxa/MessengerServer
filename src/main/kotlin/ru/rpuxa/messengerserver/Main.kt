package ru.rpuxa.messengerserver

import java.io.File

fun main() {
    //DataBase.connect()
   // Thread(HttpServer).start()

    while (true) {
       println(File(readLine()).exists())
    }
}