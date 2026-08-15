package com.sd.laborator.business.models

import java.text.SimpleDateFormat
import java.util.Date

enum class ExpenseCategory {
    INTRETINERE,
    MANCARE,
    DISTRACTIE,
    SCOALA,
    PERSONALE
}

data class Expense(
    var id: Int = 0,
    var memberId: Int = 0,
    var category: ExpenseCategory = ExpenseCategory.PERSONALE,
    var amount: Double = 0.0,
    var description: String = "",
    var date: String = SimpleDateFormat("yyyy-MM-dd").format(Date())
)

data class FamilyMember(
    var id: Int = 0,
    var username: String = "",
    var passwordHash: String = "",          // SHA-256 hash(username + password)
    var encryptedFirstName: String = "",    // AES-criptat
    var encryptedLastName: String = ""      // AES-criptat
)

data class RegisterRequest(
    var username: String = "",
    var password: String = "",
    var firstName: String = "",
    var lastName: String = ""
)

data class LoginRequest(
    var username: String = "",
    var password: String = ""
)

data class ExpenseRequest(
    var category: ExpenseCategory = ExpenseCategory.PERSONALE,
    var amount: Double = 0.0,
    var description: String = ""
)

data class EncryptRequest(
    var plaintext: String = ""
)

data class MemberResponse(
    var id: Int = 0,
    var username: String = "",
    var firstName: String = "",
    var lastName: String = ""
)

data class MyCustomResponse<T>(
    var successfulOperation: Boolean,
    var code: Int,
    var data: T,
    var error: String? = null,
    var message: String? = null
)

data class MyCustomError(
    var status: Int,
    var error: String?,
    var message: String?,
    var timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
)