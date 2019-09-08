package ru.rpuxa.messengerserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.answers.ErrorAnswer

abstract class Request(val path: String) {

    fun execute(exchange: HttpExchange): String {
        val map = HashMap<String, String>()

        (exchange.requestURI.query ?: "").split('&').forEach {
            if ('=' in it) {
                val (name, value) = it.split('=')
                map[name] = value
            }
        }

        var onExecute: Any = onExecute(map)
        if (onExecute is Error)
            onExecute = ErrorAnswer(onExecute.error.toString())
        return gson.toJson(onExecute)
    }

    abstract fun onExecute(query: Map<String, String>): RequestAnswer

    companion object {
        private val gson: Gson = GsonBuilder().create()
    }
}