package ru.rpuxa.messengerserver

object UserDataConditions {

    fun checkAll(login: String, pass: String, name: String, surname: String): Error? {
        return checkName(name) ?: checkSurname(surname) ?: checkLogin(login) ?: checkPassword(pass)
    }

    fun checkName(name: String): Error? {
        if (name.length !in 1..32) return Error.NAME_WRONG_LENGTH
        if (!name.all { it.isLetter() }) return Error.NAME_CONTAINS_WRONG_SYMBOLS

        return null
    }

    fun checkSurname(surname: String) = checkName(surname)

    fun checkLogin(login: String): Error? {
        if (login.length < 4) return Error.LOGIN_TOO_SHORT
        if (login.length > 16) return Error.LOGIN_TOO_LONG
        if (!login.all { it.isLetterOrDigit() || it == '_' || it == '-' }) return Error.LOGIN_CONTAINS_WRONG_SYMBOLS

        return null
    }

    fun checkPassword(pass: String) = if (pass.length < 4) Error.PASSWORD_TOO_SHORT else null

    fun checkBirthday(value: Long): Error? {
        return null
    }
}