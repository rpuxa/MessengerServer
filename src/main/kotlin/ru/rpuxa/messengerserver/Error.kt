package ru.rpuxa.messengerserver

enum class Error(val error: Int) : RequestAnswer {
    NO_ERROR(0),
    WRONG_ARGS(1),
    UNKNOWN_TOKEN(2),
    UNKNOWN_ID(3),

    LOGIN_ALREADY_EXISTS(100),
    WRONG_LOGIN_OR_PASSWORD(101),
    ;
}