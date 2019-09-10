package ru.rpuxa.messengerserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.answers.ErrorAnswer
import ru.rpuxa.messengerserver.answers.TextErrorAnswer

abstract class Request(val path: String) {

    fun execute(exchange: HttpExchange): String {
        val map = HashMap<String, String>()

        (exchange.requestURI.query ?: "").split('&').forEach {
            if ('=' in it) {
                val (name, value) = it.split('=')
                if (name.isNotBlank())
                    map[name] = value
            }
        }

        var onExecute: RequestAnswer = onExecute(map)
        if (onExecute is Error) {
            val text = onExecute.text
            val code = onExecute.code.toString()
            onExecute = if (text == null) {
                ErrorAnswer(code)
            } else {
                TextErrorAnswer(code, text)
            }
        }
        return gson.toJson(onExecute)
    }

    abstract fun onExecute(query: Map<String, String>): RequestAnswer

    companion object {
        private val gson: Gson = GsonBuilder().create()
    }
    }