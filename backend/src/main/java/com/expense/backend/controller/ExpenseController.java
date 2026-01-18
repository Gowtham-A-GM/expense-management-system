package com.expense.backend.controller;

import com.expense.backend.dto.ExpenseRequest;
import com.expense.backend.dto.LoginRequest;
import com.expense.backend.model.Expense;
import com.expense.backend.service.ExpenseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/expenses")
public class ExpenseController { // this handles API requests from the app frontend.

    @Autowired
    private ExpenseService service;

    @PostMapping
    public String create(@RequestBody ExpenseRequest req) throws ExecutionException, InterruptedException {
        return service.createExpense(req);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable String id,
            @RequestBody ExpenseRequest req
    ) throws ExecutionException, InterruptedException {
        service.updateExpense(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws ExecutionException, InterruptedException {
        service.deleteExpense(id);
    }

    @GetMapping
    public List<Expense> list(
            @RequestParam String userId,
            @RequestParam(required = false) String category
    ) throws ExecutionException, InterruptedException {
        return service.listExpenses(userId, category);
    }
//
//    @PostMapping("/login")
//    public String adminLogin(@RequestBody LoginRequest request){
//        if(request.getUsername().equals("admin@local.com")){
//            return "admin";
//        } else{
//            return "invalid";
//        }
//    }
}
