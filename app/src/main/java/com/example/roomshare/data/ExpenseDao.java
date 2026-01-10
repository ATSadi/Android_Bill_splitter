package com.example.roomshare.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE room_id = :roomId ORDER BY date DESC, id DESC")
    List<Expense> getExpensesByRoom(long roomId);

    @Insert
    long insertExpense(Expense expense);

    @Delete
    void deleteExpense(Expense expense);
}

