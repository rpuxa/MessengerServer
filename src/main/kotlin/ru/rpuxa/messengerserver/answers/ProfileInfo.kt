package ru.rpuxa.messengerserver.answers

import ru.rpuxa.messengerserver.RequestAnswer

class ProfileInfo(val id: Int, val login: String, val name: String, val surname: String) : RequestAnswer