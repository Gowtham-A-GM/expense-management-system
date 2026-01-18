package com.expense.backend.service;

import com.expense.backend.dto.ExpenseRequest;
import com.expense.backend.model.Expense;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ExpenseService {

    @Autowired
    private Firestore firestore;

    public String createExpense(ExpenseRequest req) throws ExecutionException, InterruptedException {
        String id = UUID.randomUUID().toString();

        Expense expense = new Expense();
        expense.setId(id);
        expense.setUserId(req.getUserId());
        expense.setAmount(req.getAmount());
        expense.setDescription(req.getDescription());
        expense.setDate(req.getDate());
        expense.setCategory(req.getCategory());

        firestore.collection("expenses")
                .document(id)
                .set(expense)
                .get();  // makes the operation wait until Firebase finishes.

        return id;
    }

    public void updateExpense(String id, ExpenseRequest req) throws ExecutionException, InterruptedException {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setUserId(req.getUserId());
        expense.setAmount(req.getAmount());
        expense.setDescription(req.getDescription());
        expense.setDate(req.getDate());
        expense.setCategory(req.getCategory());

        firestore.collection("expenses")
                .document(id)
                .set(expense)
                .get();
    }

    public void deleteExpense(String id) throws ExecutionException, InterruptedException {
        firestore.collection("expenses")
                .document(id)
                .delete()
                .get();
    }

    public List<Expense> listExpenses(String userId, String category) throws ExecutionException, InterruptedException {
        CollectionReference ref = firestore.collection("expenses");

        Query query = ref.whereEqualTo("userId", userId);

        if (category != null && !category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }

        ApiFuture<QuerySnapshot> future = query.get();

        return future.get().toObjects(Expense.class);
    }
}
