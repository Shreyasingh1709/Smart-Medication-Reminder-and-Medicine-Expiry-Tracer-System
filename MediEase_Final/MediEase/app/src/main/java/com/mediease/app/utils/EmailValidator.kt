package com.mediease.app.utils

object EmailValidator {
    private val EMAIL_REGEX =
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()

    fun isValid(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }
}
