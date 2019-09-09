package ru.rpuxa.messengerserver

import ru.rpuxa.messengerserver.answers.ProfileInfo
import ru.rpuxa.messengerserver.answers.TokenAnswer
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

object DataBase {

    private const val PATH = "/home/testuser/messenger.db"

    private lateinit var connection: Connection
    private lateinit var statement: Statement

    fun connect() {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$PATH")
        statement = connection.createStatement()

        statement.execute("CREATE TABLE IF NOT EXISTS users ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'token' TEXT, 'login' TEXT, 'pass' BLOB, 'name' TEXT, 'surname' TEXT);")
    }

    fun createNewUser(login: String, pass: String, name: String, surname: String): RequestAnswer {
        val encryptedPass = encrypt(pass)

        val set = statement.executeQuery("SELECT * FROM users WHERE login = $login")

        if (set.next()) return Error.LOGIN_ALREADY_EXISTS

        val token = randomToken()

        connection.prepareStatement("INSERT INTO users (token, login, pass, name, surname) VALUES (?, ?, ?, ?, ?)")
            .apply {
                setString(1, token)
                setString(2, login)
                setBytes(3, encryptedPass)
                setString(4, name)
                setString(5, surname)
                executeUpdate()
            }

        return TokenAnswer(token)
    }

    fun login(login: String, pass: String): RequestAnswer {
        val encryptedPass = encrypt(pass)

        val set = statement.executeQuery("SELECT * FROM users WHERE login = $login")
        if (!set.next()) return Error.WRONG_LOGIN_OR_PASSWORD

        val currentPass = set.getBytes("pass")

        if (!encryptedPass.contentEquals(currentPass)) return Error.WRONG_LOGIN_OR_PASSWORD

        return TokenAnswer(set.getString("token"))
    }

    fun getPrivateInfo(token: String): RequestAnswer {
        val statement = connection.prepareStatement("SELECT * FROM users WHERE token = ?")
        statement.setString(1, token)
        val set = statement.executeQuery()

        if (!set.next()) return Error.UNKNOWN_TOKEN

        return ProfileInfo(
            set.getInt("id"),
            set.getString("login"),
            set.getString("name"),
            set.getString("surname")
        )
    }


    fun disconnect() {
        connection.close()
        statement.close()
    }

    private val digest = MessageDigest.getInstance("SHA-256")
    private val random = SecureRandom()

    private fun encrypt(s: String): ByteArray = digest.digest(s.toByteArray())

    private fun randomToken() = buildString {
        repeat(32) {
            val code =
                if (random.nextBoolean()) {
                    'a'.toInt() + random.nextInt('z' - 'a')
                } else {
                    'A'.toInt() + random.nextInt('Z' - 'A')
                }

            append(code.toChar())
        }
    }
}