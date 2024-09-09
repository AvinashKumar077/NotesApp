package com.example.notesapp.utils

import android.text.TextUtils
import android.util.Patterns


fun validateCredentials(
    username: String,
    emailAddress: String,
    password: String,
    isLogin: Boolean
): Pair<Boolean, String> {
    var result = Pair(true, "")

    if ((!isLogin && TextUtils.isEmpty(username)) || TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password)) {

        result = Pair(false, "Please provide the credentials")
    } else if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
        result = Pair(false, "Please provide valid email")
    } else if (password.length < 5) {
        result = Pair(false, "Password length should be greater than 5")

    }
    return result
}