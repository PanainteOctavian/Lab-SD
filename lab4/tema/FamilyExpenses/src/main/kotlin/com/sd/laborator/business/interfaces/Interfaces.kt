package com.sd.laborator.business.interfaces

import com.sd.laborator.business.models.*

interface IMemberService {
    fun register(req: RegisterRequest): MyCustomResponse<Any>
    fun login(req: LoginRequest): MyCustomResponse<Any>
    fun getMember(id: Int): MyCustomResponse<Any>
    fun getAllMembers(): MyCustomResponse<Any>
    fun deleteMember(id: Int): MyCustomResponse<Any>
}

interface IExpenseService {
    fun addExpense(memberId: Int, req: ExpenseRequest): MyCustomResponse<Any>
    fun getExpenses(memberId: Int, category: ExpenseCategory?): MyCustomResponse<Any>
    fun getExpense(memberId: Int, expenseId: Int): MyCustomResponse<Any>
    fun updateExpense(memberId: Int, expenseId: Int, req: ExpenseRequest): MyCustomResponse<Any>
    fun deleteExpense(memberId: Int, expenseId: Int): MyCustomResponse<Any>
    fun getFamilyTotal(category: ExpenseCategory?): MyCustomResponse<Any>
}

interface ICryptoService {
    fun encrypt(plaintext: String): MyCustomResponse<Any>
    fun decrypt(ciphertext: String): MyCustomResponse<Any>
}