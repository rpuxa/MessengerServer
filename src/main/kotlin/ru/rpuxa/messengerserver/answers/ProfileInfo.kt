package ru.rpuxa.messengerserver.answers

import ru.rpuxa.messengerserver.RequestAnswer

open class PublicProfileInfo(val login: String, val name: String, val surname: String) : RequestAnswer

class PrivateProfileInfo(val id: Int, login: String, name: String, surname: String) :
    PublicProfileInfo(login, name, surname)