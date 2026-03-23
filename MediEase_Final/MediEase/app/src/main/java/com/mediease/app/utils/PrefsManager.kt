package com.mediease.app.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mediease_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_SETUP_COMPLETE = "setup_complete"
        const val KEY_PROFILE_CODE = "profile_code"
        const val KEY_LINKED_USER_ID = "linked_user_id"
        const val ROLE_PATIENT = "PATIENT"
        const val ROLE_CAREGIVER = "CAREGIVER"
    }

    var userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var userRole: String
        get() = prefs.getString(KEY_USER_ROLE, ROLE_PATIENT) ?: ROLE_PATIENT
        set(value) = prefs.edit().putString(KEY_USER_ROLE, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var isSetupComplete: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETE, value).apply()

    var profileCode: String
        get() = prefs.getString(KEY_PROFILE_CODE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PROFILE_CODE, value).apply()

    var linkedUserId: String
        get() = prefs.getString(KEY_LINKED_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LINKED_USER_ID, value).apply()

    fun clear() = prefs.edit().clear().apply()

    fun generateUserId(): String = "user_${System.currentTimeMillis()}"

    fun generateProfileCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val code = (1..5).map { chars.random() }.joinToString("")
        return "MED-$code"
    }
}
