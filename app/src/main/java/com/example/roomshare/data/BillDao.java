package com.example.roomshare.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BillDao {
    @Query("SELECT * FROM bills WHERE room_id = :roomId ORDER BY name")
    List<Bill> getBillsByRoom(long roomId);

    @Insert
    long insertBill(Bill bill);

    @Delete
    void deleteBill(Bill bill);
}

