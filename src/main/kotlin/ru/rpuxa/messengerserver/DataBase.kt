package ru.rpuxa.messengerserver

import ru.rpuxa.messengerserver.answers.PrivateProfileInfo
import ru.rpuxa.messengerserver.answers.PublicProfileInfo
import ru.rpuxa.messengerserver.answers.TokenAnswer
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.*

object DataBase {

    const val PATH = "messenger.db"

    private lateinit var connection: Connection
    private lateinit var statement: Statement

    fun connect() {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$PATH")
        statement = connection.createStatement()

        statement.execute(
            """CREATE TABLE IF NOT EXISTS users
(
    'id'      INTEGER PRIMARY KEY AUTOINCREMENT,
    'token'   TEXT,
    'login'   TEXT,
    'pass'    BLOB,
    'name'    TEXT,
    'surname' TEXT,
    'birthday' INTEGER
);"""
        )
    }

    fun createNewUser(login: String, pass: String, name: String, surname: String): RequestAnswer {
        if (userByLogin(login) != null) return Error.LOGIN_ALREADY_EXISTS

        val encryptedPass = encrypt(pass)
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

        val set = userByLogin(login) ?: return Error.WRONG_LOGIN_OR_PASSWORD
        val currentPass = set.getBytes("pass")

        if (!encryptedPass.contentEquals(currentPass)) return Error.WRONG_LOGIN_OR_PASSWORD

        return TokenAnswer(set.getString("token"))
    }

    fun getPrivateInfo(token: String): RequestAnswer {
        val set = userByToken(token) ?: return Error.UNKNOWN_TOKEN

        return PrivateProfileInfo(
            set.getInt("id"),
            set.getString("login"),
            set.getString("name"),
            set.getString("surname")
        )
    }

    fun setUserField(token: String, fieldName: String, value: String): Error {
        if (userByToken(token) == null) return Error.UNKNOWN_TOKEN

        fun setField() = connection.prepareStatement("UPDATE users SET $fieldName = ? WHERE id = ?").apply {
            setString(2, token)
        }

        when (fieldName) {
            "login" -> {
                UserDataConditions.checkLogin(value)?.also { return it }

                if (userByLogin(value) != null) return Error.LOGIN_ALREADY_EXISTS

                setField().setString(1, value)

            }

            "pass" -> {
                UserDataConditions.checkPassword(value)?.also { return it }

                setField().setBytes(1, encrypt(value))
            }

            "name" -> {
                UserDataConditions.checkName(value)?.also { return it }

                setField().setString(1, value)
            }

            "surname" -> {
                UserDataConditions.checkSurname(value)?.also { return it }

                setField().setString(1, value)
            }

            "birthday" -> {
                val birthday = value.toLongOrNull() ?: return Error.WRONG_ARGS
                UserDataConditions.checkBirthday(birthday)?.also { return it }

                setField().setLong(1, birthday)
            }

            else -> Error.UNKNOWN_USER_FIELD
        }

        return Error.NO_ERROR
    }


    fun disconnect() {
        try {
            connection.close()
        } catch (e: SQLException) {
        }
        try {
            statement.close()
        } catch (e: SQLException) {
        }
    }

    private fun userByToken(token: String): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM users WHERE token = ?").run {
            setString(1, token)
            executeQuery()
        }

        return if (set.next()) set else null
    }

    private fun userByLogin(login: String): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM users WHERE login = ?").run {
            setString(1, login)
            executeQuery()
        }

        return if (set.next()) set else null
    }

    private fun userById(id: Int): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM users WHERE id = ?").run {
            setInt(1, id)
            executeQuery()
        }

        return if (set.next()) set else null
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

    fun getPublicInfo(id: Int): RequestAnswer {
        val statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")
        statement.setInt(1, id)
        val set = statement.executeQuery()

        if (!set.next()) return Error.UNKNOWN_ID

        return PublicProfileInfo(
            set.getString("login"),
            set.getString("name"),
            set.getString("surname")
        )
    }
}