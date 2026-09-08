package com.omnimargen.omniserv.util

import android.util.Patterns

object SecurityUtils {

    private const val MAX_NAME_LENGTH = 200
    private const val MAX_DOCUMENT_LENGTH = 40
    private const val MAX_EMAIL_LENGTH = 254
    private const val MAX_PAIS_LENGTH = 2
    private const val TIME_DRIFT_TOLERANCE_MS = 5 * 60 * 1000L // 5 minutes

    data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null
    )

    fun validateNombre(nombre: String): ValidationResult {
        val trimmed = nombre.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "El nombre es requerido")
            trimmed.length > MAX_NAME_LENGTH -> ValidationResult(false, "El nombre excede $MAX_NAME_LENGTH caracteres")
            trimmed.contains("<script", ignoreCase = true) -> ValidationResult(false, "Nombre contiene caracteres no válidos")
            trimmed.contains("INSERT", ignoreCase = true) -> ValidationResult(false, "Nombre contiene caracteres no válidos")
            trimmed.contains("DELETE", ignoreCase = true) -> ValidationResult(false, "Nombre contiene caracteres no válidos")
            trimmed.contains("DROP", ignoreCase = true) -> ValidationResult(false, "Nombre contiene caracteres no válidos")
            else -> ValidationResult(true)
        }
    }

    fun validatePais(pais: String): ValidationResult {
        val trimmed = pais.trim().uppercase()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "El país es requerido")
            trimmed.length != 2 -> ValidationResult(false, "El país debe ser un código de 2 letras (ej: VE, US)")
            !trimmed.all { it.isLetter() } -> ValidationResult(false, "El país solo debe contener letras")
            else -> ValidationResult(true)
        }
    }

    fun validateDocumento(documento: String): ValidationResult {
        val trimmed = documento.trim().uppercase()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "El documento es requerido")
            trimmed.length > MAX_DOCUMENT_LENGTH -> ValidationResult(false, "El documento excede $MAX_DOCUMENT_LENGTH caracteres")
            trimmed.contains("<script", ignoreCase = true) -> ValidationResult(false, "Documento contiene caracteres no válidos")
            else -> ValidationResult(true)
        }
    }

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim().lowercase()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "El email es requerido")
            trimmed.length > MAX_EMAIL_LENGTH -> ValidationResult(false, "El email excede $MAX_EMAIL_LENGTH caracteres")
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> ValidationResult(false, "El email no es válido")
            else -> ValidationResult(true)
        }
    }

    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun validateTimeDrift(localTimeMs: Long, serverTimeMs: Long): ValidationResult {
        val drift = Math.abs(localTimeMs - serverTimeMs)
        return if (drift > TIME_DRIFT_TOLERANCE_MS) {
            ValidationResult(
                false,
                "La hora del dispositivo no coincide con el servidor. Verifica la fecha y hora."
            )
        } else {
            ValidationResult(true)
        }
    }

    fun validateAll(nombre: String, pais: String, documento: String, email: String): ValidationResult {
        val nombreResult = validateNombre(nombre)
        if (!nombreResult.isValid) return nombreResult

        val paisResult = validatePais(pais)
        if (!paisResult.isValid) return paisResult

        val documentoResult = validateDocumento(documento)
        if (!documentoResult.isValid) return documentoResult

        val emailResult = validateEmail(email)
        if (!emailResult.isValid) return emailResult

        return ValidationResult(true)
    }
}
