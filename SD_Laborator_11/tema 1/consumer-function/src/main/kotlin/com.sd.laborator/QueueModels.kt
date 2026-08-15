package com.sd.laborator

import io.micronaut.core.annotation.Introspected

/**
 * Cererea primita de functia consumer.
 * Contine numarul maxim pana la care se face calculul Eratostene
 * si lista de numere din fisier (preluate din coada) care trebuie verificate.
 */
@Introspected
class QueueRequest {
    // numarul maxim pana la care Eratostene face calculul
    private var maxNumber: Int = 1000000

    // lista de numere de verificat (populata de consumatorul cozii)
    private var numbersToCheck: List<Int> = emptyList()

    fun getMaxNumber(): Int = maxNumber
    fun setMaxNumber(value: Int) { maxNumber = value }

    fun getNumbersToCheck(): List<Int> = numbersToCheck
    fun setNumbersToCheck(value: List<Int>) { numbersToCheck = value }
}

/**
 * Raspunsul functiei consumer:
 * - mesaj de stare
 * - lista de numere prime gasite in lista primita din coada
 * - lista de numere ne-prime
 */
@Introspected
class QueueResponse {
    private var message: String? = null
    private var primeNumbers: List<Int> = emptyList()
    private var nonPrimeNumbers: List<Int> = emptyList()
    private var totalChecked: Int = 0

    fun getMessage(): String? = message
    fun setMessage(value: String?) { message = value }

    fun getPrimeNumbers(): List<Int> = primeNumbers
    fun setPrimeNumbers(value: List<Int>) { primeNumbers = value }

    fun getNonPrimeNumbers(): List<Int> = nonPrimeNumbers
    fun setNonPrimeNumbers(value: List<Int>) { nonPrimeNumbers = value }

    fun getTotalChecked(): Int = totalChecked
    fun setTotalChecked(value: Int) { totalChecked = value }
}