package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.RequestAnswer

object SetInfoRequest : TokenRequest("/profile/setInfo") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>): Error {
        query.forEach { (name, value) ->
            if (name != "token") {
                val error = DataBase.setUserField(token, name, value)
                if (error != Error.NO_ERROR)
                    return error
            }
        }

        return Error.NO_ERROR
    }
}