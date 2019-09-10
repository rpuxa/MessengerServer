package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer
import ru.rpuxa.messengerserver.Error

object RegisterRequest : Request("/reg") {

    override fun onExecute(query: Map<String, String>): RequestAnswer {
        val login = query["login"] ?: return Error.WRONG_ARGS
        val pass = query["pass"] ?: return Error.WRONG_ARGS
        val name = query["name"] ?: return Error.WRONG_ARGS
        val surname = query["surname"] ?: return Error.WRONG_ARGS

        if (name.length !in 1..32) return Error.NAME_WRONG_LENGTH
        if (!name.all { it.isLetter() }) return Error.NAME_CONTAINS_WRONG_SYMBOLS

        if (surname.length !in 1..32) return Error.SURNAME_WRONG_LENGTH
        if (!surname.all { it.isLetter() }) return Error.SURNAME_CONTAINS_WRONG_SYMBOLS

        if (login.length < 4) return Error.LOGIN_TOO_SHORT
        if (login.length > 16) return Error.LOGIN_TOO_LONG
        if (!login.all { it.isLetterOrDigit() || it == '_' || it == '-' }) return Error.LOGIN_CONTAINS_WRONG_SYMBOLS

        if (pass.length < 4) return Error.PASSWORD_TOO_SHORT

        return DataBase.createNewUser(login, pass, name, surname)
    }
}