package com.sd.laborator.presentation.controllers

import com.sd.laborator.business.interfaces.IExpenseService
import com.sd.laborator.business.models.ExpenseCategory
import com.sd.laborator.business.models.ExpenseRequest
import com.sd.laborator.presentation.utils.ControllerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ExpenseController {

    @Autowired
    private lateinit var expenseService: IExpenseService

    @PostMapping("/members/{memberId}/expenses")
    fun addExpense(
        @PathVariable memberId: Int,
        @RequestBody req: ExpenseRequest
    ): ResponseEntity<Any> =
        ControllerUtils.makeResponse(expenseService.addExpense(memberId, req))

    @GetMapping("/members/{memberId}/expenses")
    fun getExpenses(
        @PathVariable memberId: Int,
        @RequestParam(required = false) category: ExpenseCategory?
    ): ResponseEntity<Any> =
        ControllerUtils.makeResponse(expenseService.getExpenses(memberId, category))

    @GetMapping("/members/{memberId}/expenses/{expenseId}")
    fun getExpense(
        @PathVariable memberId: Int,
        @PathVariable expenseId: Int
    ): ResponseEntity<Any> =
        ControllerUtils.makeResponse(expenseService.getExpense(memberId, expenseId))

    @PutMapping("/members/{memberId}/expenses/{expenseId}")
    fun updateExpense(
        @PathVariable memberId: Int,
        @PathVariable expenseId: Int,
        @RequestBody req: ExpenseRequest
    ): ResponseEntity<Any> =
        ControllerUtils.makeResponse(expenseService.updateExpense(memberId, expenseId, req))

    @DeleteMapping("/members/{memberId}/expenses/{expenseId}")
    fun deleteExpense(
        @PathVariable memberId: Int,
        @PathVariable expenseId: Int
    ): ResponseEntity<Any> =
        ControllerUtils.makeResponse(expenseService.deleteExpense(memberId, expenseId))

    @GetMapping("/expenses/total")
    fun getFamilyTotal(
        @RequestParam(required = false) category: ExpenseCategory?
    ): ResponseEntity<Any> =
        ControllerUtils.makeResponse(expenseService.getFamilyTotal(category))
}