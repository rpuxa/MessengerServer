package ru.rpuxa.messengerserver

import ru.rpuxa.messengerserver.answers.PrivateProfileInfo
import ru.rpuxa.messengerserver.answers.PublicProfileInfo
import ru.rpuxa.messengerserver.answers.TokenAnswer
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.*

object DataBase {

    const val PATH = "messenger.db"

    private const val USERS_TABLE = "users"

    private const val ID = "id"
    private const val TOKEN = "token"
    private const val LOGIN = "login"
    private const val PASSWORD = "pass"
    private const val NAME = "name"
    private const val SURNAME = "surname"
    private const val BIRTHDAY = "birthday"

    private lateinit var connection: Connection
    private lateinit var statement: Statement

    fun connect() {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$PATH")
        statement = connection.createStatement()

        statement.execute(
            """CREATE TABLE IF NOT EXISTS $USERS_TABLE
(
    '$ID'      INTEGER PRIMARY KEY AUTOINCREMENT,
    '$TOKEN'   TEXT,
    '$LOGIN'   TEXT,
    '$PASSWORD'    BLOB,
    '$NAME'    TEXT,
    '$SURNAME' TEXT,
    '$BIRTHDAY' INTEGER
);"""
        )
    }

    fun createNewUser(login: String, pass: String, name: String, surname: String): RequestAnswer {
        if (userByLogin(login) != null) return Error.LOGIN_ALREADY_EXISTS

        val encryptedPass = encrypt(pass)
        val token = randomToken()

        connection.prepareStatement("INSERT INTO users ($TOKEN, $LOGIN, $PASSWORD, $NAME, $SURNAME, $BIRTHDAY) VALUES (?, ?, ?, ?, ?, ?)")
            .apply {
                setString(1, token)
                setString(2, login)
                setBytes(3, encryptedPass)
                setString(4, name)
                setString(5, surname)
                setLong(6, 0)
                executeUpdate()
            }

        return TokenAnswer(token)
    }

    fun login(login: String, pass: String): RequestAnswer {
        val encryptedPass = encrypt(pass)

        val set = userByLogin(login) ?: return Error.WRONG_LOGIN_OR_PASSWORD
        val currentPass = set.getBytes(PASSWORD)

        if (!encryptedPass.contentEquals(currentPass)) return Error.WRONG_LOGIN_OR_PASSWORD

        return TokenAnswer(set.getString(TOKEN))
    }

    fun getPrivateInfo(token: String): RequestAnswer {
        val set = userByToken(token) ?: return Error.UNKNOWN_TOKEN

        return PrivateProfileInfo(
            set.getInt(ID),
            set.getString(LOGIN),
            set.getString(NAME),
            set.getString(SURNAME),
            set.getLong(BIRTHDAY)
        )
    }


    fun getPublicInfo(id: Int): RequestAnswer {
        val set = userById(id) ?: return Error.UNKNOWN_ID

        return PublicProfileInfo(
            set.getString(LOGIN),
            set.getString(NAME),
            set.getString(SURNAME),
            set.getLong(BIRTHDAY)
        )
    }

    fun setUserField(token: String, fieldName: String, value: String): Error {
        if (userByToken(token) == null) return Error.UNKNOWN_TOKEN

        fun setField() = connection.prepareStatement("UPDATE $USERS_TABLE SET $fieldName = ? WHERE $ID = ?").apply {
            setString(2, token)
        }

        when (fieldName) {
            LOGIN -> {
                UserDataConditions.checkLogin(value)?.also { return it }

                if (userByLogin(value) != null) return Error.LOGIN_ALREADY_EXISTS

                setField().setString(1, value)

            }

            PASSWORD -> {
                UserDataConditions.checkPassword(value)?.also { return it }

                setField().setBytes(1, encrypt(value))
            }

            NAME -> {
                UserDataConditions.checkName(value)?.also { return it }

                setField().setString(1, value)
            }

            SURNAME -> {
                UserDataConditions.checkSurname(value)?.also { return it }

                setField().setString(1, value)
            }

            BIRTHDAY -> {
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
        val set = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $TOKEN = ?").run {
            setString(1, token)
            executeQuery()
        }

        return if (set.next()) set else null
    }

    private fun userByLogin(login: String): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $LOGIN = ?").run {
            setString(1, login)
            executeQuery()
        }

        return if (set.next()) set else null
    }

    private fun userById(id: Int): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $ID = ?").run {
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

}