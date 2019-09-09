package ru.rpuxa.messengerserver

import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer
import ru.rpuxa.messengerserver.requests.*
import java.lang.Exception


object HttpServer : Runnable, AutoCloseable {
    private const val IP = "176.57.217.44"
    private const val PORT = 80

    private var server: HttpServer? = null

    override fun run() {
        println("Start server at $IP:$PORT")
        val server = HttpServer.create(
            InetSocketAddress(
                IP,
                PORT
            ), 0
        )
        this.server = server
        for (request in ALL_REQUESTS) {
            server.createContext(request.path) {
                println("Request received")
                val answer = try {
                    request.execute(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    "ERROR: ${e.message}"
                }
                val bytes = answer.toByteArray()
                it.sendResponseHeaders(200, bytes.size.toLong())
                val os = it.responseBody
                os.write(bytes)
                os.close()
            }

        }
        server.executor = null
        server.start()

        println("Server started!")
    }

    override fun close() {
        server?.stop(10)
    }

    private val ALL_REQUESTS = arrayOf(
        WelcomeRequest,
        RegisterRequest,
        LoginRequest,
        PrivateInfoRequest,
        PublicInfoRequest
    )
}