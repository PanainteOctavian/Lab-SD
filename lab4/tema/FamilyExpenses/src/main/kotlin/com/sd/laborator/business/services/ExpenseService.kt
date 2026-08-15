package com.sd.laborator.business.services

import com.sd.laborator.business.interfaces.IExpenseService
import com.sd.laborator.business.models.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class ExpenseService : IExpenseService {

    @Autowired
    private lateinit var memberService: MemberService

    private val expenses = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Expense>>()
    private val idCounter = AtomicInteger(1)

    private fun memberExpenses(memberId: Int) =
        expenses.getOrPut(memberId) { ConcurrentHashMap() }

    override fun addExpense(memberId: Int, req: ExpenseRequest): MyCustomResponse<Any> {
        return try {
            if (!memberService.memberExists(memberId))
                return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Membrul cu id=$memberId nu există."
                )

            if (req.amount <= 0)
                return MyCustomResponse(
                    successfulOperation = false, code = 400, data = Unit,
                    error = "Bad Request", message = "Suma trebuie să fie pozitivă."
                )

            val id = idCounter.getAndIncrement()
            val expense = Expense(
                id = id,
                memberId = memberId,
                category = req.category,
                amount = req.amount,
                description = req.description,
                date = SimpleDateFormat("yyyy-MM-dd").format(Date())
            )
            memberExpenses(memberId)[id] = expense

            MyCustomResponse(successfulOperation = true, code = 201, data = expense)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun getExpenses(memberId: Int, category: ExpenseCategory?): MyCustomResponse<Any> {
        return try {
            if (!memberService.memberExists(memberId))
                return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Membrul cu id=$memberId nu există."
                )

            val list = memberExpenses(memberId).values
                .filter { category == null || it.category == category }
                .sortedByDescending { it.date }

            if (list.isEmpty())
                MyCustomResponse(successfulOperation = true, code = 204, data = emptyList<Expense>())
            else
                MyCustomResponse(successfulOperation = true, code = 200, data = list)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun getExpense(memberId: Int, expenseId: Int): MyCustomResponse<Any> {
        return try {
            if (!memberService.memberExists(memberId))
                return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Membrul cu id=$memberId nu există."
                )

            val expense = memberExpenses(memberId)[expenseId]
                ?: return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Cheltuiala cu id=$expenseId nu există."
                )

            MyCustomResponse(successfulOperation = true, code = 200, data = expense)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun updateExpense(memberId: Int, expenseId: Int, req: ExpenseRequest): MyCustomResponse<Any> {
        return try {
            if (!memberService.memberExists(memberId))
                return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Membrul cu id=$memberId nu există."
                )

            val existing = memberExpenses(memberId)[expenseId]
                ?: return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Cheltuiala cu id=$expenseId nu există."
                )

            if (req.amount <= 0)
                return MyCustomResponse(
                    successfulOperation = false, code = 400, data = Unit,
                    error = "Bad Request", message = "Suma trebuie să fie pozitivă."
                )

            val updated = existing.copy(
                category = req.category,
                amount = req.amount,
                description = req.description
            )
            memberExpenses(memberId)[expenseId] = updated

            MyCustomResponse(successfulOperation = true, code = 202, data = updated)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun deleteExpense(memberId: Int, expenseId: Int): MyCustomResponse<Any> {
        return try {
            if (!memberService.memberExists(memberId))
                return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Membrul cu id=$memberId nu există."
                )

            if (!memberExpenses(memberId).containsKey(expenseId))
                return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Cheltuiala cu id=$expenseId nu există."
                )

            memberExpenses(memberId).remove(expenseId)
            MyCustomResponse(successfulOperation = true, code = 200, data = Unit)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun getFamilyTotal(category: ExpenseCategory?): MyCustomResponse<Any> {
        return try {
            val breakdown = mutableMapOf<String, Double>()
            var grandTotal = 0.0

            expenses.forEach { (_, memberMap) ->
                memberMap.values
                    .filter { category == null || it.category == category }
                    .forEach { expense ->
                        val key = expense.category.name
                        breakdown[key] = (breakdown[key] ?: 0.0) + expense.amount
                        grandTotal += expense.amount
                    }
            }

            MyCustomResponse(
                successfulOperation = true,
                code = 200,
                data = mapOf(
                    "grandTotal" to grandTotal,
                    "byCategory" to breakdown,
                    "filter" to (category?.name ?: "ALL")
                )
            )
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }
}