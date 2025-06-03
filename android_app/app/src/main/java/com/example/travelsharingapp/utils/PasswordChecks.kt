package com.example.travelsharingapp.utils

import androidx.compose.ui.graphics.Color

data class PasswordRequirementCheck(
    val description: String,
    val isMet: Boolean
)

enum class PasswordStrength(val text: String, val color: Color, val progress: Float) {
    NONE("", Color.Transparent, 0.0f),
    WEAK("Weak", Color(0xFFDC0A0A), 0.25f),
    FAIR("Fair", Color(0xFFFFA500), 0.5f),
    GOOD("Good", Color(0xFF007BFF), 0.75f),
    STRONG("Strong", Color(0xFF28A745), 1.0f),
    VERY_STRONG("Very strong", Color(0xFF28A745), 1.0f),
    SECRET("It's over 9000!", Color(0xFF7200FF), 1.0f)
}

object PasswordRequirementsTexts {
    const val LENGTH = "Must be at least 8 characters long."
    const val UPPERCASE = "Must contain at least one uppercase letter (A-Z)."
    const val LOWERCASE = "Must contain at least one lowercase letter (a-z)."
    const val DIGIT = "Must contain at least one digit (0-9)."
    const val SPECIAL_CHAR = "Must contain at least one special character (e.g., !@#$%)."
}

fun checkPasswordRequirements(password: String): List<PasswordRequirementCheck> {
    val specialCharPattern = Regex("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]")
    return listOf(
        PasswordRequirementCheck(
            description = PasswordRequirementsTexts.LENGTH,
            isMet = password.length >= 8
        ),
        PasswordRequirementCheck(
            description = PasswordRequirementsTexts.UPPERCASE,
            isMet = password.any { it.isUpperCase() }
        ),
        PasswordRequirementCheck(
            description = PasswordRequirementsTexts.LOWERCASE,
            isMet = password.any { it.isLowerCase() }
        ),
        PasswordRequirementCheck(
            description = PasswordRequirementsTexts.DIGIT,
            isMet = password.any { it.isDigit() }
        ),
        PasswordRequirementCheck(
            description = PasswordRequirementsTexts.SPECIAL_CHAR,
            isMet = password.contains(specialCharPattern)
        )
    )
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE

    val checks = checkPasswordRequirements(password)
    var score = 0

    val hasUppercase = checks[1].isMet
    val hasLowercase = checks[2].isMet
    val hasDigit = checks[3].isMet
    val hasSpecialChar = checks[4].isMet

    score += when {
        password.length >= 32 -> 13
        password.length >= 20 -> 3
        password.length >= 16 -> 2
        password.length >= 8  -> 1
        else -> return PasswordStrength.WEAK
    }

    if (hasUppercase) score += 1
    if (hasLowercase) score += 1
    if (hasDigit) score += 1
    if (hasSpecialChar) score += 2

    return when {
        score >= 18 -> PasswordStrength.SECRET
        score >= 8 -> PasswordStrength.VERY_STRONG
        score >= 7 -> PasswordStrength.STRONG
        score >= 5 -> PasswordStrength.GOOD
        score >= 3 -> PasswordStrength.FAIR
        else -> PasswordStrength.WEAK
    }
}