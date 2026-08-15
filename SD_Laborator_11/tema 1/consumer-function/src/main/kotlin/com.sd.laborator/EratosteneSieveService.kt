package com.sd.laborator

import jakarta.inject.Singleton
import java.util.*

/**
 * Serviciu Singleton care implementeaza ciurul lui Eratostene optimizat O(n).
 * Implementare preluata de la:
 * https://www.geeksforgeeks.org/sieve-eratosthenes-0n-time-complexity/
 *
 * Vectorii isPrime si SPF sunt initializati o singura data la pornirea aplicatiei
 * (blocul init), astfel ca apelurile ulterioare la findPrimesLessThan sunt rapide.
 */
@Singleton
class EratosteneSieveService {

    val MAX_SIZE = 1000001

    /*
     isPrime[i] = true daca i este numar prim
     SPF[i]     = cel mai mic factor prim al lui i
                  (Smallest Prime Factor)
     Exemplu: SPF[8] = 2, SPF[15] = 3
    */
    private val isPrime = Vector<Boolean>(MAX_SIZE)
    private val SPF = Vector<Int>(MAX_SIZE)

    /**
     * Returneaza lista tuturor numerelor prime strict mai mici decat n.
     * Complexitate: O(n) - fiecare numar compus este eliminat exact o data.
     */
    fun findPrimesLessThan(n: Int): List<Int> {
        val prime: MutableList<Int> = ArrayList()

        for (i in 2 until n) {
            if (isPrime[i]) {
                prime.add(i)
                // un numar prim este propriul sau cel mai mic factor prim
                SPF[i] = i
            }

            /*
             Se elimina toti multiplii de forma i * prime[j]:
             - se seteaza isPrime[i * prime[j]] = false
             - se inregistreaza SPF[i * prime[j]] = prime[j]
             Bucla se opreste cand prime[j] > SPF[i] pentru a garanta
             ca fiecare compus este eliminat exact o data.
            */
            var j = 0
            while (j < prime.size && i * prime[j] < n && prime[j] <= SPF[i]) {
                isPrime[i * prime[j]] = false
                SPF[i * prime[j]] = prime[j]
                j++
            }
        }
        return prime
    }

    init {
        // Initializare: toate numerele presupuse prime, SPF initial = 2
        for (i in 0 until MAX_SIZE) {
            isPrime.add(true)
            SPF.add(2)
        }
        // 0 si 1 nu sunt prime
        isPrime[0] = false
        isPrime[1] = false
    }
}