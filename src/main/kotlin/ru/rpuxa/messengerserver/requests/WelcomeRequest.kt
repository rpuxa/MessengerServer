package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer

object WelcomeRequest : Request("/welcome") {
    override fun onExecute(query: Map<String, String>): RequestAnswer {
        return WelcomeAnswer("Welcome to android messenger API!")
    }

    private class WelcomeAnswer(val text: String) : RequestAnswer
}
