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

        return DataBase("").createNewUser(login, pass, name, surname)
    }
}